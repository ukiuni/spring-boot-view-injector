package com.ukiuni.spring.injector;

public class InjectDependenciesResourceOperations {
	public final boolean complessJS;
	public final boolean complessCss;

	public InjectDependenciesResourceOperations() {
		this(true, true);
	}

	public InjectDependenciesResourceOperations(boolean complessJS, boolean complessCss) {
		this.complessJS = complessJS;
		this.complessCss = complessCss;
	}
}
