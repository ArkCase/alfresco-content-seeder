package com.armedia.alfresco.extension.contentseeder;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.armedia.alfresco.extension.contentseeder.ContentSeeder;

class ContentSeederTest {

	protected static GenericXmlApplicationContext CONTEXT = null;

	@BeforeAll
	static void beforeAll() {
		ContentSeederTest.CONTEXT = new GenericXmlApplicationContext("classpath:test-context.xml");
	}

	@Test
	void testApplyInternal() throws Exception {
		ContentSeeder patch = new ContentSeeder();

		patch.applyInternal();
	}

	@AfterAll
	static void afterAll() {
		ContentSeederTest.CONTEXT.close();
		ContentSeederTest.CONTEXT = null;
	}
}
