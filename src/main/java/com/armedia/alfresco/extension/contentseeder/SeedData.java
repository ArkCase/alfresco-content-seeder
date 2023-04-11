package com.armedia.alfresco.extension.contentseeder;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class SeedData {

	public static class RmInfo {
		protected static final String DEFAULT_RM_SITE = "rm";
		protected static final Boolean DEFAULT_RM_ENABLED = Boolean.FALSE;

		private String site = RmInfo.DEFAULT_RM_SITE;
		private boolean enabled = RmInfo.DEFAULT_RM_ENABLED.booleanValue();

		public String getSite() {
			return this.site;
		}

		public void setSite(String site) {
			this.site = StringUtils.defaultIfBlank(site, RmInfo.DEFAULT_RM_SITE);
		}

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(Boolean enabled) {
			this.enabled = ObjectUtils.defaultIfNull(enabled, RmInfo.DEFAULT_RM_ENABLED).booleanValue();
		}
	}

	public static class SiteDef {
		private String compliance;
		private String description;
		private String preset;
		private String root;
		private String title;
		private String type;
		private String visibility;

		private Map<String, Object> contents;

		public String getCompliance() {
			return this.compliance;
		}

		public void setCompliance(String compliance) {
			this.compliance = compliance;
		}

		public String getDescription() {
			return this.description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getPreset() {
			return this.preset;
		}

		public void setPreset(String preset) {
			this.preset = preset;
		}

		public String getRoot() {
			return this.root;
		}

		public void setRoot(String root) {
			this.root = root;
		}

		public String getTitle() {
			return this.title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getVisibility() {
			return this.visibility;
		}

		public void setVisibility(String visibility) {
			this.visibility = visibility;
		}

		public Map<String, Object> getContents() {
			return this.contents;
		}

		public void setContents(Map<String, Object> contents) {
			this.contents = contents;
		}
	}

	private static final RmInfo DEFAULT_RMINFO = new RmInfo();

	private RmInfo recordsManagement = SeedData.DEFAULT_RMINFO;
	private Map<String, SiteDef> sites = Collections.emptyMap();

	public RmInfo getRecordsManagement() {
		return this.recordsManagement;
	}

	public void setRecordsManagement(RmInfo recordsManagement) {
		this.recordsManagement = ObjectUtils.defaultIfNull(recordsManagement, SeedData.DEFAULT_RMINFO);
	}

	public Map<String, SiteDef> getSites() {
		return this.sites;
	}

	public void setSites(Map<String, SiteDef> sites) {
		this.sites = ObjectUtils.defaultIfNull(sites, Collections.emptyMap());
	}

}