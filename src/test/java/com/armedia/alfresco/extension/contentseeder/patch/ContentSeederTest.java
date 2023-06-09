package com.armedia.alfresco.extension.contentseeder.patch;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.armedia.alfresco.extension.contentseeder.SeedData;
import com.armedia.alfresco.extension.contentseeder.SiteData;

class ContentSeederTest {

	protected static GenericXmlApplicationContext CONTEXT = null;

	private static final AuthenticationWrapper VOID_AUTH_WRAPPER = new AuthenticationWrapper() {
		@Override
		public <T> T runAsAdministrator(CheckedRunnable<T> task) throws Exception {
			return task.run();
		}
	};

	@BeforeAll
	static void beforeAll() {
		ContentSeederTest.CONTEXT = new GenericXmlApplicationContext("classpath:test-context.xml");
	}

	@Test
	void testApplyInternal() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("armedia-seed-content.yaml");
		System.setProperty("armedia.seed.content", url.toExternalForm());
		ContentSeeder patch = ContentSeederTest.CONTEXT.getBean(ContentSeeder.class);
		patch.setAuthenticationWrapper(ContentSeederTest.VOID_AUTH_WRAPPER);

		FilePlanService fps = ContentSeederTest.CONTEXT.getBean(FilePlanService.class);
		FileFolderService ffs = ContentSeederTest.CONTEXT.getBean(FileFolderService.class);
		SiteService ss = ContentSeederTest.CONTEXT.getBean(SiteService.class);
		NodeService ns = ContentSeederTest.CONTEXT.getBean(NodeService.class);
		NamespaceService nss = ContentSeederTest.CONTEXT.getBean(NamespaceService.class);
		SiteInfo si = EasyMock.createStrictMock(SiteInfo.class);
		FileInfo fi = EasyMock.createStrictMock(FileInfo.class);

		final String contentSource = StringSubstitutor.replaceSystemProperties(ContentSeeder.getSeedContentSource());
		SeedData seedData = patch.loadSeedContent(contentSource);

		final SeedData.RmInfo rmInfo = seedData.getRecordsManagement();
		final String rmSite = rmInfo.getSite();

		final Map<String, SeedData.SiteDef> sites = seedData.getSites();
		EasyMock.reset(fps, ffs, ss, ns, si, fi);
		for (String siteName : sites.keySet()) {
			final SeedData.SiteDef siteDef = sites.get(siteName);
			final boolean rm = StringUtils.equals(rmSite, siteName);

			// If RM is disabled, and this is the RM site, we skip it
			if (rm && !rmInfo.isEnabled()) {
				continue;
			}

			SiteData siteData = new SiteData(siteName, siteDef, StringUtils.equals(rmSite, siteName), nss);

			// For now, we skip the RM sites ... this functionality is disabled for now
			if (siteData.rm) {
				continue;
			}

			EasyMock.expect(ss.getSite(siteData.name)).andReturn(null).once();
			EasyMock.expect(ss.createSite(siteData.preset, siteData.name, siteData.title, siteData.description,
				siteData.visibility, siteData.type)).andReturn(si).once();
			EasyMock.expect(si.getShortName()).andReturn(siteData.name).once();
			NodeRef nr = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				String.format("Site=[%s].DocumentLibrary", siteData.name));
			EasyMock.expect(ss.getContainer(siteData.name, SiteService.DOCUMENT_LIBRARY)).andReturn(nr).once();
			if (siteData.rm) {
				NodeRef nr2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
					String.format("RMSite[%s].%s", siteData.name, siteData.root));
				Capture<Map<QName, Serializable>> metadata = EasyMock.newCapture();
				EasyMock.expect(
					fps.createRecordCategory(EasyMock.eq(nr), EasyMock.eq(siteData.root), EasyMock.capture(metadata)))
					.andReturn(nr2).once();
				nr = nr2;
			}

			for (String folderName : siteData.contents.keySet()) {
				NodeRef fnr = new NodeRef(nr.getStoreRef(),
					String.format("Site=[%s],Folder=[%s]", siteName, folderName));
				if (siteData.rm) {
					EasyMock.expect(fps.createRecordCategory(nr, folderName)).andReturn(fnr).once();
				} else {
					EasyMock.expect(ffs.create(nr, folderName, ContentModel.TYPE_FOLDER)).andReturn(fi).once();
				}
			}
		}
		EasyMock.replay(fps, ffs, ss, ns, si, fi);
		patch.applyInternal();
		EasyMock.verify(fps, ffs, ss, ns, si, fi);
	}

	@AfterAll
	static void afterAll() {
		ContentSeederTest.CONTEXT.close();
		ContentSeederTest.CONTEXT = null;
	}
}