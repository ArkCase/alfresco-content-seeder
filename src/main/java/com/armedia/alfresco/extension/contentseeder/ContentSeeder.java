package com.armedia.alfresco.extension.contentseeder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@Component
@Lazy
public class ContentSeeder extends AbstractPatch {

	private static final class BadSeedFormatException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private final Object result;

		private BadSeedFormatException(Object result) {
			this.result = result;
		}
	}

	private static final String ENVVAR = "ARMEDIA_SEED_CONTENT";
	private static final String SYSPROP = "armedia.seed.content";
	private static final String DEFAULT = "${user.dir}/armedia-seed-content.yaml";

	private final Logger log = Log.LOG;

	private static String getSeedContentSource() {
		String v = null;

		v = System.getProperty(ContentSeeder.SYSPROP);
		if (StringUtils.isNotBlank(v)) { return v; }

		v = System.getenv(ContentSeeder.ENVVAR);
		if (StringUtils.isNotBlank(v)) { return v; }

		return ContentSeeder.DEFAULT;
	}

	// This date pattern matches the one from the Ansible installer: %Y-%m%d%H%M%S%N
	private static final DateTimeFormatter RMA_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MMddHHmmssnnnnnnnnn");

	protected Map<QName, Serializable> createCategoryMetadata(String categoryName) {
		Map<QName, Serializable> md = new HashMap<>();
		md.put(QName.createQName("cm:description", this.namespaceService), "");
		md.put(QName.createQName("cm:name", this.namespaceService), categoryName);
		md.put(QName.createQName("cm:title", this.namespaceService), categoryName);
		md.put(QName.createQName("rma:identifier", this.namespaceService),
			ContentSeeder.RMA_DATE_FORMAT.format(Instant.now()));
		md.put(QName.createQName("rma:reviewPeriod", this.namespaceService), "none|0");
		md.put(QName.createQName("rma:vitalRecordIndicator", this.namespaceService), "false");
		return md;
	}

	public Pair<SiteInfo, NodeRef> create(SiteData data) throws Exception {

		SiteInfo site = this.siteService.createSite(data.preset, data.name, data.title, data.description,
			data.visibility, data.type);

		NodeRef rootNode = this.siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
		if (data.rm) {
			rootNode = this.filePlanService.createRecordCategory(rootNode, data.root,
				createCategoryMetadata(data.root));
		}

		final BiConsumer<NodeRef, String> folderCreator = (data.rm //
			? this.filePlanService::createRecordCategory //
			: (r, n) -> this.fileFolderService.create(r, n, ContentModel.TYPE_FOLDER) //
		);

		// Create the folders/categories within rootNode
		for (String name : data.contents.keySet()) {
			// For now, we ignore the object description. We can do fancier stuff later on ...
			// Object o = this.contents.get(name);
			try {
				folderCreator.accept(rootNode, name);
			} catch (FileExistsException e) {
				// Ignore ... not a problem
			}
		}

		return Pair.of(site, rootNode);
	}

	@Autowired
	private SiteService siteService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private FilePlanService filePlanService;

	@PostConstruct
	protected void postConstruct() {
		this.siteService.hashCode();
		"".hashCode();
	}

	protected void createSite(Map<String, String> siteData) throws Exception {

	}

	protected void createContentFolder(SiteInfo site, String folderName) throws Exception {

	}

	protected void createRmCategory(SiteInfo site, String categoryName) throws Exception {

	}

	protected SeedData loadSeedContent(final String contentSource) throws Exception {
		this.log.debug("Loading the seed content from [{}]", contentSource);

		InputStream inputStream = null;
		try {
			URL url = new URL(contentSource);
			inputStream = url.openStream();
		} catch (MalformedURLException e) {
			// Not a URL, it's a path ...
			Path p = Paths.get(contentSource);
			if (!Files.exists(p)) { throw new FileNotFoundException(contentSource); }
			if (!Files.isRegularFile(p)) { throw new IOException("The path [" + contentSource + "] is not a file"); }
			if (!Files.isReadable(p)) { throw new IOException("The path [" + contentSource + "] is not readable"); }
			inputStream = new FileInputStream(p.toRealPath().toFile());
		}

		try (InputStream in = inputStream) {
			try {
				return loadSeedContent(in);
			} catch (BadSeedFormatException e) {
				throw new Exception("The content source file [" + contentSource
					+ "] is not properly formatted, root object is of type: " + e.result.getClass().getName());
			}
		}
	}

	protected SeedData loadSeedContent(final InputStream in) throws Exception {
		try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			return loadSeedContent(r);
		}
	}

	protected SeedData loadSeedContent(final Reader r) throws Exception {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		SeedData seedData = mapper.readValue(r, SeedData.class);
		if (this.log.isTraceEnabled()) {
			this.log.trace("Seed content loaded:{}{}", System.lineSeparator(), mapper.writeValueAsString(seedData));
		}
		return seedData;
	}

	@Override
	protected String applyInternal() throws Exception {

		final String contentSource = StringSubstitutor.replaceSystemProperties(ContentSeeder.getSeedContentSource());
		try {
			SeedData seedData = loadSeedContent(contentSource);

			// First things first: make sure the RM config is consistent (if enabled, the site is
			// properly specified)
			RmInfo recordsManagement = seedData.getRecordsManagement();

			return "Seeded the initial content";
		} catch (Exception e) {
			throw new Exception(String.format("Failed to seed the initial content: %s", e.getMessage()), e);
		}
	}
}