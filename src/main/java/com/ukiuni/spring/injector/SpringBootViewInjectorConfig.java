package com.ukiuni.spring.injector;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;

@Configuration
@ComponentScan(basePackageClasses = SpringBootViewInjectorConfig.class)
public class SpringBootViewInjectorConfig implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	InjectDependenciesResourceOperations operations;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		ResourceHttpRequestHandler handler = (ResourceHttpRequestHandler) contextRefreshedEvent.getApplicationContext().getBean("resourceHandlerMapping", SimpleUrlHandlerMapping.class).getUrlMap().get("/**");
		handler.getResourceTransformers().add(new ResourceTransformer() {
			@Override
			public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain) throws IOException {
				String fileName = resource.getFilename();
				if (fileName.endsWith(".js") || fileName.endsWith(".html") || fileName.endsWith(".htm")) {
					return new InjectDependenciesResource(SpringBootViewInjectorConfig.this.operations, request, resource, handler);
				} else {
					return resource;
				}
			}
		});
	}
}
