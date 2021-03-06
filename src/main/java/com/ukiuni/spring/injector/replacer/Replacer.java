package com.ukiuni.spring.injector.replacer;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Replacer {
	public Pattern getPattern();

	public Function<String, String> getReplaceFunction(Matcher m);

	public default boolean target(Matcher m) {
		return true;
	}

	public default String getResource(Matcher m) {
		return m.group(1);
	}
}
