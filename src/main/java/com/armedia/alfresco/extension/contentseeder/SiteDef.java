package com.armedia.alfresco.extension.contentseeder;

import java.util.Map;

public class SiteDef {
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