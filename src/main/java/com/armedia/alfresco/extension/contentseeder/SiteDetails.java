package com.armedia.alfresco.extension.contentseeder;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class SiteDetails {

	private String preset;
	private String name;
	private String title;
	private String description;
	private String visibility;
	private String type;
	private String compliance;

	public String getPreset() {
		return this.preset;
	}

	public SiteDetails setPreset(String preset) {
		this.preset = preset;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public SiteDetails setName(String name) {
		this.name = name;
		return this;
	}

	public String getTitle() {
		return this.title;
	}

	public SiteDetails setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return this.description;
	}

	public SiteDetails setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getVisibility() {
		return this.visibility;
	}

	public SiteDetails setVisibility(String visibility) {
		this.visibility = visibility;
		return this;
	}

	public String getType() {
		return this.type;
	}

	public SiteDetails setType(String type) {
		this.type = type;
		return this;
	}

	public String getCompliance() {
		return this.compliance;
	}

	public SiteDetails setCompliance(String compliance) {
		this.compliance = compliance;
		return this;
	}

	public Pair<SiteInfo, NodeRef> create(SiteService siteService, FilePlanService fps, NamespacePrefixResolver nspr)
		throws Exception {
		QName type = null;
		if ((nspr != null) && StringUtils.isNotBlank(this.type)) {
			type = QName.resolveToQName(nspr, this.type);
		}

		SiteVisibility visibility = SiteVisibility.PUBLIC;
		if (StringUtils.isNotBlank(this.visibility)) {
			visibility = SiteVisibility.valueOf(StringUtils.upperCase(this.visibility));
		}

		QName compliance = null;
		if ((nspr != null) && StringUtils.isNotBlank(this.compliance)) {
			compliance = QName.resolveToQName(nspr, this.compliance);
		}

		SiteInfo site = siteService.createSite(this.preset, this.name, this.title, this.description, visibility, type);
		NodeRef docLib = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
		return Pair.of(site, docLib);
	}
}