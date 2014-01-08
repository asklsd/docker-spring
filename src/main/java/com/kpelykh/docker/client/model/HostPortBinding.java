package com.kpelykh.docker.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class HostPortBinding {

	@JsonProperty("HostIp")
	private String hostIp;

	@JsonProperty("HostPort")
	private String hostPort;

	public HostPortBinding() {
	}

	public HostPortBinding(String hostIp, String hostPort) {
		this.hostIp = hostIp;
		this.hostPort = hostPort;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

	@Override
	public String toString() {
		return "HostPortBinding [hostIp=" + hostIp + ", hostPort=" + hostPort + "]";
	}

}