package com.armedia.alfresco.extension.contentseeder;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;

public class SiteData implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_SITE_COMPLIANCE = "{http://www.alfresco.org/model/recordsmanagement/1.0}rmsite";
	private static final String DEFAULT_SITE_PRESET = "site-dashboard";
	private static final String DEFAULT_SITE_ROOT = SiteService.DOCUMENT_LIBRARY;
	private static final String DEFAULT_SITE_TYPE = "{http://www.alfresco.org/model/site/1.0}site";

	private static final String DEFAULT_RM_SITE_COMPLIANCE = "{http://www.alfresco.org/model/dod5015/1.0}site";
	private static final String DEFAULT_RM_SITE_PRESET = "rm-site-dashboard";
	private static final String DEFAULT_RM_SITE_ROOT = "ACM";
	private static final String DEFAULT_RM_SITE_TYPE = SiteData.DEFAULT_RM_SITE_COMPLIANCE;

	private static final SiteVisibility DEFAULT_VISIBILITY = SiteVisibility.PUBLIC;

	protected final QName compliance;
	protected final QName type;
	protected final SiteVisibility visibility;
	protected final String description;
	protected final String name;
	protected final String preset;
	protected final String root;
	protected final String title;
	protected final boolean rm;

	protected final Map<String, Object> contents;

	public SiteData(String name, SeedData.SiteDef seed, boolean rm, NamespacePrefixResolver nspr) {
		this.rm = rm;
		this.description = seed.getDescription();
		this.name = name;
		this.title = StringUtils.defaultIfBlank(seed.getTitle(), StringUtils.upperCase(this.name));

		this.preset = StringUtils.defaultIfBlank(seed.getPreset(),
			(this.rm ? SiteData.DEFAULT_RM_SITE_PRESET : SiteData.DEFAULT_SITE_PRESET));

		this.root = StringUtils.defaultIfBlank(seed.getRoot(),
			(this.rm ? SiteData.DEFAULT_RM_SITE_ROOT : SiteData.DEFAULT_SITE_ROOT));

		SiteVisibility visibility = SiteData.DEFAULT_VISIBILITY;
		if (StringUtils.isNotBlank(seed.getVisibility())) {
			visibility = SiteVisibility.valueOf(StringUtils.upperCase(seed.getVisibility()));
		}
		this.visibility = visibility;

		this.type = QName.resolveToQName(nspr, StringUtils.defaultIfBlank(seed.getType(),
			(this.rm ? SiteData.DEFAULT_RM_SITE_TYPE : SiteData.DEFAULT_SITE_TYPE)));

		this.compliance = QName.resolveToQName(nspr, StringUtils.defaultIfBlank(seed.getCompliance(),
			(this.rm ? SiteData.DEFAULT_RM_SITE_COMPLIANCE : SiteData.DEFAULT_SITE_COMPLIANCE)));

		Map<String, Object> contents = seed.getContents();
		this.contents = (((contents != null) && !contents.isEmpty()) //
			? Collections.unmodifiableMap(contents) //
			: Collections.emptyMap() //
		);
	}
}