package com.ukiuni.spring.noInjectorPackage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

@RunWith(SpringRunner.class)
public class InjectorSeveralTests {
	private static ConfigurableApplicationContext context;

	@Autowired
	public void putContext(ConfigurableApplicationContext context) {
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
		System.out.println(src);
		Assert.assertEquals(StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream("expects/severalResult.html"), Charset.forName("UTF-8")), src);
	}

}
