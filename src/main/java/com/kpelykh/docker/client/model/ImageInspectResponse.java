package com.kpelykh.docker.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageInspectResponse {

	@JsonProperty("Id")
	private String id;

	@JsonProperty("Parent")
	private String parent;

	@JsonProperty("Created")
	private String created;

	@JsonProperty("Container")
	private String container;

	@JsonProperty("ContainerConfig")
	private ContainerConfig containerConfig;

	@JsonProperty("Size")
	private long size;

	@JsonProperty("DockerVersion")
	private String dockerVersion;

	@JsonProperty("Config")
	private ContainerConfig config;

	@JsonProperty("Architecture")
	private String arch;

	@JsonProperty("Comment")
	private String comment;

	@JsonProperty("Author")
	private String author;

	@JsonProperty("Os")
	private String os;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public ContainerConfig getContainerConfig() {
		return containerConfig;
	}

	public void setContainerConfig(ContainerConfig containerConfig) {
		this.containerConfig = containerConfig;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getDockerVersion() {
		return dockerVersion;
	}

	public void setDockerVersion(String dockerVersion) {
		this.dockerVersion = dockerVersion;
	}

	public ContainerConfig getConfig() {
		return config;
	}

	public void setConfig(ContainerConfig config) {
		this.config = config;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	@Override
	public String toString() {
		return "ImageInspectResponse {"
				+ "id='" + id
				+ ", parent=" + parent
				+ ", created=" + created
				+ ", container=" + container
				+ ", containerConfig=" + containerConfig
				+ ", size=" + size
				+ ", dockerVersion=" + dockerVersion
				+ ", config=" + config
				+ ", arch='" + arch
				+ ", comment='" + comment
				+ ", author='" + author
				+ ", os='" + os
				+ "}";
	}
}
