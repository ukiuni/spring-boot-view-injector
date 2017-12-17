package com.ukiuni.spring.injector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@ToString
public class InjectDependenciesResourceOperations {
	private boolean complessJS;
	private boolean complessCss;
	private boolean injectJSToHTML;
	private boolean injectCssToHTML;
	private boolean injectImageToHTML;
	private boolean injectToJS;
}
