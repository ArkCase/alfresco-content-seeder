package com.armedia.alfresco.extension.contentseeder;

import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

class ContentSeederTest {

	protected static GenericXmlApplicationContext CONTEXT = null;

	@BeforeAll
	static void beforeAll() {
		ContentSeederTest.CONTEXT = new GenericXmlApplicationContext("classpath:test-context.xml");
	}

	@Test
	void testApplyInternal() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("armedia-seed-content.yaml");
		System.setProperty("armedia.seed.content", url.toExternalForm());
		ContentSeeder patch = ContentSeederTest.CONTEXT.getBean(ContentSeeder.class);
		patch.applyInternal();
	}

	@AfterAll
	static void afterAll() {
		ContentSeederTest.CONTEXT.close();
		ContentSeederTest.CONTEXT = null;
	}
}