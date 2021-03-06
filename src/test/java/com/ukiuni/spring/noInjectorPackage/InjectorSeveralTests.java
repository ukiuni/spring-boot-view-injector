package com.ukiuni.spring.noInjectorPackage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StreamUtils;

public class InjectorSeveralTests {
	private static ConfigurableApplicationContext context;

	@BeforeClass
	public static void init() {
		InjectorSeveralTests.context = SpringApplication.run(DummyApplication.class);
	}

	@AfterClass
	public static void stopServer() {
		context.close();
	}

	@Test
	public void injected() throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/several.html").openConnection();
		String src = StreamUtils.copyToString(connection.getInputStream(), Charset.forName("UTF-8"));
		Assert.assertEquals(StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream("expects/severalResult.html"), Charset.forName("UTF-8")), src);
	}

}
