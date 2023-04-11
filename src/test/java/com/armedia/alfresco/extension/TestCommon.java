package com.armedia.alfresco.extension;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.GenericXmlApplicationContext;

public class TestCommon {
	public static final File TEMP_DIR = TempFileProvider.getSystemTempDir();
	public static final File CONTENT_ROOT = TempFileProvider.getTempDir(UUID.randomUUID().toString());

	protected static GenericXmlApplicationContext CONTEXT = null;

	public static void beforeAll(String... extraContexts) throws Exception {
		// Create initial context
		FileUtils.forceMkdir(TestCommon.CONTENT_ROOT);
		Set<String> contexts = new LinkedHashSet<>();
		contexts.add("classpath:test-context.xml");
		for (String s : extraContexts) {
			if (!StringUtils.isBlank(s)) {
				contexts.add(s);
			}
		}
		String[] allContexts = contexts.toArray(new String[0]);
		TestCommon.CONTEXT = new GenericXmlApplicationContext(allContexts);
	}

	public static void afterAll() {
		TestCommon.CONTEXT.close();
		TestCommon.CONTEXT = null;
		FileUtils.deleteQuietly(TestCommon.CONTENT_ROOT);
	}
}