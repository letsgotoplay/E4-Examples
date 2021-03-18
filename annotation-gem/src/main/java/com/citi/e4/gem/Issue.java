package com.citi.e4.gem;

public class Issue {

	private String dependencyName;
	private String version;
	private String description;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDependencyName() {
		return dependencyName;
	}
	public void setDependencyName(String dependencyName) {
		this.dependencyName = dependencyName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
