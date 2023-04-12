package com.armedia.alfresco.extension.contentseeder.patch;

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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.armedia.alfresco.extension.contentseeder.Log;
import com.armedia.alfresco.extension.contentseeder.SeedData;
import com.armedia.alfresco.extension.contentseeder.SiteData;
import com.armedia.alfresco.extension.contentseeder.patch.AuthenticationWrapper.CheckedRunnable;

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

	private static final String PATCH_ID = "patch.armedia.contentSeeder";
	private static final String MSG_SUCCESS = ContentSeeder.PATCH_ID + ".success";

	private static class DefaultAuthenticationWrapper<T> implements AuthenticationWrapper<T> {

		@Override
		public T runAsAdministrator(CheckedRunnable<T> task) throws Exception {
			AuthenticationUtil.pushAuthentication();
			try {
				AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
				return task.run();
			} finally {
				AuthenticationUtil.popAuthentication();
			}
		}

	}

	private final Logger log = Log.LOG;

	protected static String getSeedContentSource() {
		String v = null;

		v = System.getProperty(ContentSeeder.SYSPROP);
		if (StringUtils.isNotBlank(v)) { return v; }

		v = System.getenv(ContentSeeder.ENVVAR);
		if (StringUtils.isNotBlank(v)) { return v; }

		return ContentSeeder.DEFAULT;
	}

	// This date pattern matches the one from the Ansible installer: %Y-%m%d%H%M%S%N
	private static final DateTimeFormatter RMA_DATE_FORMAT = DateTimeFormatter //
		.ofPattern("yyyy-MMddHHmmssnnnnnnnnn") //
		.withZone(ZoneId.of("UTC")) //
	;

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

	public SiteInfo create(SiteData data) throws Exception {

		SiteInfo site = this.siteService.getSite(data.name);
		if (site == null) {
			this.log.info("Site [{}] does not exist - it will be created (rm={})", data.name, data.rm);
			site = this.siteService.createSite(data.preset, data.name, data.title, data.description, data.visibility,
				data.type);
		} else {
			this.log.info("Site [{}] already exists, will use the existing site (nodeRef={})", data.name,
				site.getNodeRef());
		}

		NodeRef rootNode = this.siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
		if (rootNode == null) {
			rootNode = this.siteService.createContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY, null, null);
		}
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

		return site;
	}

	@Autowired(required = true)
	private SiteService siteService;

	@Autowired(required = true)
	private FileFolderService fileFolderService;

	@Autowired(required = true)
	private NamespaceService namespaceService;

	@Autowired(required = true)
	private FilePlanService filePlanService;

	private AuthenticationWrapper<String> authenticationWrapper = new DefaultAuthenticationWrapper<>();

	protected AuthenticationWrapper<String> getAuthenticationWrapper() {
		return this.authenticationWrapper;
	}

	protected void setAuthenticationWrapper(AuthenticationWrapper<String> wrapper) {
		this.authenticationWrapper = (wrapper != null ? wrapper : new DefaultAuthenticationWrapper<>());
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
		Yaml yaml = new Yaml();
		SeedData seedData = yaml.loadAs(r, SeedData.class);
		if (this.log.isTraceEnabled()) {
			this.log.trace("Seed content loaded:{}{}", System.lineSeparator(), yaml.dump(seedData));
		}
		return seedData;
	}

	// TODO: figure out how to disable this for unit tests, b/c AuthenticationUtil won't
	// be initialized and thus will break everything
	protected <T> T runAsAdministrator(CheckedRunnable<T> task) throws Exception {
		AuthenticationUtil.pushAuthentication();
		try {
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
			return task.run();
		} finally {
			AuthenticationUtil.popAuthentication();
		}
	}

	@Override
	protected String applyInternal() throws Exception {

		this.log.info("Starting execution of patch: {}", I18NUtil.getMessage(ContentSeeder.PATCH_ID));
		final String contentSource = StringSubstitutor.replaceSystemProperties(ContentSeeder.getSeedContentSource());
		try {
			SeedData seedData = loadSeedContent(contentSource);
			this.log.info("Loaded the seeding data:{}{}", System.lineSeparator(), new Yaml().dump(seedData));

			final SeedData.RmInfo rmInfo = seedData.getRecordsManagement();
			final String rmSite = rmInfo.getSite();

			this.authenticationWrapper.runAsAdministrator(() -> {
				final Map<String, SeedData.SiteDef> sites = seedData.getSites();
				for (String siteName : sites.keySet()) {
					final SeedData.SiteDef siteDef = sites.get(siteName);
					final boolean rm = StringUtils.equals(rmSite, siteName);

					// If RM is disabled, and this is the RM site, we skip it
					if (rm && !rmInfo.isEnabled()) {
						continue;
					}

					SiteData siteData = new SiteData(siteName, siteDef, StringUtils.equals(rmSite, siteName),
						this.namespaceService);
					this.log.info("Seeding the site information for [{}] (rm={})", siteData.name, siteData.rm);
					SiteInfo siteInfo = create(siteData);
					if (this.log.isDebugEnabled()) {
						this.log.debug("Successfully seeded the site information for [{}] (nodeRef={})",
							siteInfo.getShortName(), siteInfo.getNodeRef());
					}
				}
				return null;
			});
			return I18NUtil.getMessage(ContentSeeder.MSG_SUCCESS);
		} catch (Exception e) {
			throw new Exception(String.format("Failed to seed the initial content: %s", e.getMessage()), e);
		}
	}
}