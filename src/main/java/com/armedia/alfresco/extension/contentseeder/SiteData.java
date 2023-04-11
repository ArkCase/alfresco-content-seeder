package com.armedia.alfresco.extension.contentseeder;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;

public class SiteData implements Serializable {
	private static final long serialVersionUID = 1L;

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

	public SiteData(String name, SiteDef seed, Boolean rm, NamespacePrefixResolver nspr) {
		this.rm = ((rm != null) && rm.booleanValue());
		this.description = seed.getDescription();
		this.name = name;
		this.title = StringUtils.defaultIfBlank(seed.getTitle(), StringUtils.upperCase(this.name));

		this.preset = StringUtils.defaultIfBlank(seed.getPreset(),
			(this.rm ? SeedData.DEFAULT_RM_SITE_PRESET : SeedData.DEFAULT_SITE_PRESET));

		this.root = StringUtils.defaultIfBlank(seed.getRoot(),
			(this.rm ? SeedData.DEFAULT_RM_SITE_ROOT : SeedData.DEFAULT_SITE_ROOT));

		SiteVisibility visibility = SeedData.DEFAULT_VISIBILITY;
		if (StringUtils.isNotBlank(seed.getVisibility())) {
			visibility = SiteVisibility.valueOf(StringUtils.upperCase(seed.getVisibility()));
		}
		this.visibility = visibility;

		this.type = QName.resolveToQName(nspr, StringUtils.defaultIfBlank(seed.getType(),
			(this.rm ? SeedData.DEFAULT_RM_SITE_TYPE : SeedData.DEFAULT_SITE_TYPE)));

		this.compliance = QName.resolveToQName(nspr, StringUtils.defaultIfBlank(seed.getCompliance(),
			(this.rm ? SeedData.DEFAULT_RM_SITE_COMPLIANCE : SeedData.DEFAULT_SITE_COMPLIANCE)));

		Map<String, Object> contents = seed.getContents();
		this.contents = (((contents != null) && !contents.isEmpty()) //
			? Collections.unmodifiableMap(contents) //
			: Collections.emptyMap() //
		);
	}
}