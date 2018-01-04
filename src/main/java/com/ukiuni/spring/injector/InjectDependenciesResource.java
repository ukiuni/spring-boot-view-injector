package com.ukiuni.spring.injector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningLevel;
import com.ukiuni.spring.injector.replacer.Replacer;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class InjectDependenciesResource implements Resource {
	private final Resource resource;
	private static final Map<String, byte[]> cache = new WeakHashMap<>();
	private static final Pattern jsReplacePatternParam;
	static {
		String blockPattern = "(\\{(%s[^\\}]*?)*?\\})*?";
		for (int i = 0; i < 5; i++) {
			blockPattern = String.format(blockPattern, blockPattern + "|");
		}
		blockPattern = String.format(blockPattern, "");
		String arrayPattern = "(\\[(%s[^\\]]*?)*?\\])*?";
		for (int i = 0; i < 5; i++) {
			arrayPattern = String.format(arrayPattern, arrayPattern + "|");
		}
		arrayPattern = String.format(arrayPattern, "");
		String patternSrc = "\\/\\*\\*\\s*@(?<command>inject|injectAsString){1}\\(\"(?<resource>.*)\"\\)\\s*\\*\\/\\n\\s*(?<def>((var|const|let)\\s*.*?\\s*=\\s*)|(.*?\\s*?:\\s*?)){1}?(((?<function>(function)?\\s*\\(\\s*\\)\\s*(=>)?\\s*(\\{(" + blockPattern + "|[^\\}]*?)*\\})*?)|(?<be>(?<!function)([^\\{\\[]|" + blockPattern + "|" + arrayPattern + ")*?))\\s*(?<end>;|\\n|,|\\}))";
		jsReplacePatternParam = Pattern.compile(patternSrc);
	}
	private static final Pattern jsTagReplacePattern = Pattern.compile("<\\s*script\\s+.*src=\"(.*)\".*>.*<\\s*/script\\s*>");
	private static final Pattern cssTagReplacePattern = Pattern.compile("<\\s*link\\s+.*href=\"(.*)\".*>");
	private static final Pattern imgTagReplacePattern = Pattern.compile("<\\s*img\\s+.*src=\"(?<resource>.*(\\.png|\\.jpg|\\.jpeg))\".*>");
	private final long contentsLength;
	private final InputStream resourceInputStream;
	private InjectDependenciesResourceOperations operations;
	private BooleanWrapper currentResourceCacheable = new BooleanWrapper(true);

	private static class BooleanWrapper {
		private boolean bool;

		public BooleanWrapper(boolean bool) {
			this.bool = bool;
		}

		public boolean isTrue() {
			return this.bool;
		}

		public void setToFalse() {
			this.bool = false;
		}
	}

	public InjectDependenciesResource(InjectDependenciesResourceOperations operations, HttpServletRequest request, Resource resource, ResourceHttpRequestHandler handler) {
		this(operations, request, resource, handler, new BooleanWrapper(true));
	}

	private InjectDependenciesResource(InjectDependenciesResourceOperations operations, HttpServletRequest request, Resource resource, ResourceHttpRequestHandler handler, BooleanWrapper isCacheable) {
		this.operations = null != operations ? operations : InjectDependenciesResourceOperations.of(true, true, true, true, true, true, true);
		this.resource = resource;
		if (null == resource || !resource.exists()) {
			contentsLength = -1;
			resourceInputStream = null;
			return;
		}
		try {
			byte[] cached = cache.get(request.getRequestURI());
			if (null != cached) {
				contentsLength = cached.length;
				resourceInputStream = new ByteArrayInputStream(cached);
				return;
			}
			String body = StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
			List<Replacer> replacers = new ArrayList<>();
			if (this.operations.isInjectToJS()) {
				replacers.add(new Replacer() {
					public Pattern getPattern() {
						return jsReplacePatternParam;
					}

					@Override
					public String getResource(Matcher m) {
						return m.group("resource");
					}

					public Function<String, String> getReplaceFunction(Matcher m) {
						if ("injectAsString".equals(m.group("command"))) {
							return appendsParts -> m.group("def") + Matcher.quoteReplacement("\"" + appendsParts.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"") + m.group("end");
						} else {
							return appendsParts -> m.group("def") + Matcher.quoteReplacement(appendsParts) + m.group("end");
						}
					}
				});
			}
			if (this.operations.isInjectJSToHTML()) {
				replacers.add(new Replacer() {
					public Pattern getPattern() {
						return jsTagReplacePattern;
					}

					public Function<String, String> getReplaceFunction(Matcher m) {
						return appendsParts -> "<script>" + Matcher.quoteReplacement(appendsParts) + "</script>";
					}

					public boolean target(Matcher m) {
						return !(m.group(1).startsWith("http://") || m.group(1).startsWith("https://"));
					};
				});
			}
			if (this.operations.isInjectCssToHTML()) {
				replacers.add(new Replacer() {
					public Pattern getPattern() {
						return cssTagReplacePattern;
					}

					public Function<String, String> getReplaceFunction(Matcher m) {
						return appendsParts -> "<style type=\"text/css\">" + Matcher.quoteReplacement(appendsParts) + "</style>";
					}

					public boolean target(Matcher m) {
						return m.group(0).contains("stylesheet");
					};
				});
			}
			if (this.operations.isInjectImageToHTML()) {
				replacers.add(new Replacer() {
					public Pattern getPattern() {
						return imgTagReplacePattern;
					}

					public Function<String, String> getReplaceFunction(Matcher m) {
						return appendsParts -> m.group(0).replaceAll("src=\".*\"", "src=\"" + Matcher.quoteReplacement(appendsParts) + "\"");
					}

					public boolean target(Matcher m) {
						return true;
					};
				});
			}
			for (Replacer replacer : replacers) {
				Matcher m = replacer.getPattern().matcher(body);
				StringBuffer sb = new StringBuffer();
				while (m.find()) {
					if (!replacer.target(m)) {
						continue;
					}
					String pathInResource = replacer.getResource(m);
					String appendsParts = createParts(request, handler, pathInResource, currentResourceCacheable);
					appendsParts = replacer.getReplaceFunction(m).apply(appendsParts);
					m.appendReplacement(sb, appendsParts);
				}
				m.appendTail(sb);
				body = sb.toString();
			}
			byte[] resourceBytes = body.getBytes(Charset.forName("UTF-8"));
			contentsLength = resourceBytes.length;
			resourceInputStream = new ByteArrayInputStream(resourceBytes);
			if (this.operations.isUseCache() && currentResourceCacheable.isTrue()) {
				cache.put(resource.getURI().toString(), resourceBytes);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String createParts(HttpServletRequest request, ResourceHttpRequestHandler handler, String pathInResource, BooleanWrapper isCachable) throws MalformedURLException, IOException {
		boolean isHttp = pathInResource.startsWith("http://") || pathInResource.startsWith("https://");
		Resource loadResource;
		if (isHttp) {
			isCachable.setToFalse();
			loadResource = new UrlResource(pathInResource) {
				@Override
				public InputStream getInputStream() throws IOException {
					URLConnection con = new URL(pathInResource).openConnection();
					Collections.list(request.getHeaderNames()).stream().forEach((k) -> con.setRequestProperty(k, request.getHeader(k)));
					try {
						return con.getInputStream();
					} catch (IOException ex) {
						if (con instanceof HttpURLConnection) {
							((HttpURLConnection) con).disconnect();
						}
						throw ex;
					}
				}
			};
		} else {
			String targetURL = new URL(new URL(request.getRequestURL().toString()), pathInResource).getPath().substring(request.getContextPath().length());
			loadResource = handler.getResourceResolvers().stream().map(r -> r.resolveResource(request, targetURL, handler.getLocations(), null)).map(r -> {
				if (null == r) {
					return new EmptyResource();
				} else if (r.getFilename().endsWith(".js") || r.getFilename().endsWith(".css") || r.getFilename().endsWith(".html") || r.getFilename().endsWith(".htm")) {
					return new InjectDependenciesResource(this.operations, request, r, handler, isCachable);
				} else {
					return r;
				}
			}).findFirst().get();
		}

		String source;
		if (this.operations.isComplessJS() && pathInResource.endsWith(".js") && !pathInResource.endsWith("min.js")) {
			SourceFile file = SourceFile.fromInputStream(new File(pathInResource).getName(), loadResource.getInputStream(), Charset.forName("UTF-8"));
			Compiler compiler = new Compiler();
			CompilerOptions options = new CompilerOptions();
			CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
			WarningLevel.VERBOSE.setOptionsForWarningLevel(options);
			options.setStrictModeInput(false);
			options.setAssumeStrictThis(false);
			options.setEmitUseStrict(false);
			Result result = compiler.compile(SourceFile.fromCode("dummy.js", ""), file, options);
			if (!result.success) {
				throw new RuntimeException("Closure Compiler returned error " + Arrays.asList(result.errors).stream().map(r -> r.toString()).collect(Collectors.joining(",")));
			}
			compiler.disableThreads();
			source = compiler.toSource();
		} else if (this.operations.isComplessCss() && pathInResource.endsWith(".css") && !pathInResource.endsWith("min.css")) {
			StringWriter writer = new StringWriter();
			new CssCompressor(new InputStreamReader(loadResource.getInputStream(), Charset.forName("UTF-8"))).compress(writer, -1);
			source = writer.toString();
		} else if (pathInResource.endsWith(".png")) {
			String data = Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(loadResource.getInputStream()));
			source = "data:image/png;base64," + data;
		} else if (pathInResource.endsWith(".jpg") || pathInResource.endsWith(".jpeg")) {
			String data = Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(loadResource.getInputStream()));
			source = "data:image/jpg;base64," + data;
		} else {
			source = StreamUtils.copyToString(loadResource.getInputStream(), Charset.forName("UTF-8"));
		}
		return source;
	}

	public File getFile() throws IOException {
		return resource.getFile();
	}

	public boolean isOpen() {
		return resource.isOpen();
	}

	public URI getURI() throws IOException {
		return resource.getURI();
	}

	public boolean isReadable() {
		return true;
	}

	public long contentLength() throws IOException {
		return this.contentsLength;
	}

	public boolean exists() {
		return resource.exists();
	}

	public long lastModified() throws IOException {
		return resource.lastModified();
	}

	public InputStream getInputStream() throws IOException {
		return this.resourceInputStream;
	}

	public String toString() {
		return resource.toString();
	}

	public URL getURL() throws IOException {
		return resource.getURL();
	}

	public String getFilename() {
		return resource.getFilename();
	}

	public String getDescription() {
		return resource.getDescription();
	}

	public boolean equals(Object obj) {
		return resource.equals(obj);
	}

	public int hashCode() {
		return resource.hashCode();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return resource.createRelative(relativePath);
	}
}
