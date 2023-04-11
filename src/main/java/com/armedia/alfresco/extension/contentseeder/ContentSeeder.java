package com.armedia.alfresco.extension.contentseeder;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ContentSeeder extends AbstractPatch {

	private static final String ENVVAR = "ARMEDIA_SEED_CONTENT";
	private static final String SYSPROP = "armedia.seed.content";
	private static final String DEFAULT = "${user.dir}/armedia-seed-content";

	private final Logger log = Log.LOG;

	private static String getContentSource() {
		String v = null;

		v = System.getProperty(ContentSeeder.SYSPROP);
		if (StringUtils.isNotBlank(v)) { return v; }

		v = System.getenv(ContentSeeder.ENVVAR);
		if (StringUtils.isNotBlank(v)) { return v; }

		return ContentSeeder.DEFAULT;
	}

	@Autowired
	private SiteService siteService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private FilePlanService filePlanService;

	protected void createSite(Object siteData) throws Exception {

	}

	protected void createContentFolder(SiteInfo site, Object folderData) throws Exception {

	}

	protected void createRmCategory(SiteInfo site, Object folderData) throws Exception {

	}

	@Override
	protected String applyInternal() throws Exception {

		final String contentSource = StringSubstitutor.replaceSystemProperties(ContentSeeder.getContentSource());

		this.log.debug("Seeding the intial content from [{}]", contentSource);
		try {
			// TODO: Seed the content ... read the information, execute the work

			return "Seeded the initial content";
		} catch (Exception e) {
			throw new Exception(String.format("Failed to seed the initial content: %s", e.getMessage()), e);
		}
	}

}