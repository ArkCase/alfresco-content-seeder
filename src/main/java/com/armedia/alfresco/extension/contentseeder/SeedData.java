package com.armedia.alfresco.extension.contentseeder;

import java.util.Map;

import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;

public class SeedData {

	protected static final String DEFAULT_RM_SITE = "rm";
	protected static final Boolean DEFAULT_RM_ENABLED = Boolean.FALSE;

	protected static final String DEFAULT_SITE_COMPLIANCE = "{http://www.alfresco.org/model/recordsmanagement/1.0}rmsite";
	protected static final String DEFAULT_SITE_PRESET = "site-dashboard";
	protected static final String DEFAULT_SITE_ROOT = SiteService.DOCUMENT_LIBRARY;
	protected static final String DEFAULT_SITE_TYPE = "{http://www.alfresco.org/model/site/1.0}site";

	protected static final String DEFAULT_RM_SITE_COMPLIANCE = "{http://www.alfresco.org/model/dod5015/1.0}site";
	protected static final String DEFAULT_RM_SITE_PRESET = "rm-site-dashboard";
	protected static final String DEFAULT_RM_SITE_ROOT = "ACM";
	protected static final String DEFAULT_RM_SITE_TYPE = SeedData.DEFAULT_RM_SITE_COMPLIANCE;

	protected static final SiteVisibility DEFAULT_VISIBILITY = SiteVisibility.PUBLIC;

	private RmInfo recordsManagement;
	private Map<String, SiteDef> sites;

	public RmInfo getRecordsManagement() {
		return this.recordsManagement;
	}

	public void setRecordsManagement(RmInfo recordsManagement) {
		this.recordsManagement = recordsManagement;
	}

	public Map<String, SiteDef> getSites() {
		return this.sites;
	}

	public void setSites(Map<String, SiteDef> sites) {
		this.sites = sites;
	}

}