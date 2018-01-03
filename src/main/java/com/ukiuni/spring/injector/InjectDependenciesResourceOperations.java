package com.ukiuni.spring.injector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@ConfigurationProperties(prefix = "spring.injector")
@Component
@ToString
public class InjectDependenciesResourceOperations {
	private boolean complessJS = true;
	private boolean complessCss = true;
	private boolean injectJSToHTML = true;
	private boolean injectCssToHTML = true;
	private boolean injectImageToHTML = true;
	private boolean injectToJS = true;
	private boolean useCache = true;
}
