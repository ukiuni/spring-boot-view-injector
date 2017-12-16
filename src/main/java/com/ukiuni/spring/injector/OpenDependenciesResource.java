package com.ukiuni.spring.injector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningLevel;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class OpenDependenciesResource implements Resource {
	private final Resource resource;
	private static final Pattern htmlReplacePattern = Pattern.compile("<\\s*script\\s+.*src=\"(.*)\".*>.*<\\s*/script\\s*>");
	private static final Pattern jsReplacePattern = Pattern.compile("inject\\(\\s*\"(.*)\"\\s*\\)");
	private final long contentsLength;
	private final InputStream resourceInputStream;
	private boolean compless = true;
	private boolean complessCss = true;

	public OpenDependenciesResource(HttpServletRequest request, Resource resource, ResourceHttpRequestHandler handler) {
		this.resource = resource;
		if (null == resource || !resource.exists()) {
			contentsLength = -1;
			resourceInputStream = null;
			return;
		}
		try {
			String body = StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
			boolean isJS = resource.getFilename().endsWith(".js");
			Matcher m = (isJS ? jsReplacePattern : htmlReplacePattern).matcher(body);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String pathInResource = m.group(1);
				String appendsParts = createParts(request, handler, pathInResource);
				if (isJS) {
					appendsParts = "\"" + appendsParts.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";// TODO リプレイスが足りない？
					appendsParts = Matcher.quoteReplacement(appendsParts);
				} else {
					appendsParts = "<script>" + Matcher.quoteReplacement(appendsParts) + "</script>";
				}
				m.appendReplacement(sb, appendsParts);
			}
			m.appendTail(sb);
			String joined = sb.toString();
			byte[] resourceBytes = joined.getBytes(Charset.forName("UTF-8"));
			contentsLength = resourceBytes.length;
			resourceInputStream = new ByteArrayInputStream(resourceBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String createParts(HttpServletRequest request, ResourceHttpRequestHandler handler, String pathInResource) throws MalformedURLException, IOException {
		String targetURL = pathInResource.startsWith("http://") || pathInResource.startsWith("https://") ? pathInResource : new URL(new URL(request.getRequestURL().toString()), pathInResource).getPath().substring(request.getContextPath().length());
		Resource loadResource = handler.getResourceResolvers().stream().map(r -> r.resolveResource(request, targetURL, handler.getLocations(), null)).map(r -> new OpenDependenciesResource(request, r, handler)).findFirst().get();

		String source;
		if (this.compless && pathInResource.endsWith(".js") && !pathInResource.endsWith("min.js")) {
			SourceFile file = SourceFile.fromInputStream(new File(pathInResource).getName(), loadResource.getInputStream(), Charset.forName("UTF-8"));
			Compiler compiler = new Compiler();
			CompilerOptions options = new CompilerOptions();
			CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
			WarningLevel.VERBOSE.setOptionsForWarningLevel(options);
			Result result = compiler.compile(SourceFile.fromCode("dummy.js", ""), file, options);
			if (!result.success) {
				throw new RuntimeException("Closure Compiler returned error " + Arrays.asList(result.errors).stream().map(r -> r.toString()).collect(Collectors.joining(",")));
			}
			compiler.disableThreads();
			source = compiler.toSource();
		} else if (this.complessCss && pathInResource.endsWith(".css") && !pathInResource.endsWith("min.css")) {
			StringWriter writer = new StringWriter();
			new CssCompressor(new InputStreamReader(loadResource.getInputStream(), Charset.forName("UTF-8"))).compress(writer, -1);
			source = writer.toString();
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
