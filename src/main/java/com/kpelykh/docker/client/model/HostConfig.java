package com.kpelykh.docker.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 * 
 */
public class HostConfig {

	public class LxcConf {

		@JsonProperty("Key")
		private String key;

		@JsonProperty("Value")
		private String value;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "LxcConf [key=" + key + ", value=" + value + "]";
		}

	}

	@JsonProperty("PublishAllPorts")
	private boolean publishAllPorts;

	@JsonProperty("Privileged")
	private boolean privileged;

	@JsonProperty("Binds")
	private String[] binds;

	@JsonProperty("Links")
	private String[] links;

	@JsonProperty("ContainerIDFile")
	private String containerIDFile;

	@JsonProperty("LxcConf")
	private LxcConf[] lxcConf;

	@JsonProperty("PortBindings")
	private Map<String, HostPortBinding[]> portBindings = new HashMap<String, HostPortBinding[]>();

	public HostConfig() {
	}

	public HostConfig(String[] binds) {
		this.binds = binds;
	}

	public boolean isPublishAllPorts() {
		return publishAllPorts;
	}

	public void setPublishAllPorts(boolean publishAllPorts) {
		this.publishAllPorts = publishAllPorts;
	}

	public boolean isPrivileged() {
		return privileged;
	}

	public void setPrivileged(boolean privileged) {
		this.privileged = privileged;
	}

	public String[] getBinds() {
		return binds;
	}

	public void setBinds(String[] binds) {
		this.binds = binds;
	}

	public String[] getLinks() {
		return links;
	}

	public void setLinks(String[] links) {
		this.links = links;
	}

	public String getContainerIDFile() {
		return containerIDFile;
	}

	public void setContainerIDFile(String containerIDFile) {
		this.containerIDFile = containerIDFile;
	}

	public LxcConf[] getLxcConf() {
		return lxcConf;
	}

	public void setLxcConf(LxcConf[] lxcConf) {
		this.lxcConf = lxcConf;
	}

	public Map<String, HostPortBinding[]> getPortBindings() {
		return portBindings;
	}

	public void setPortBindings(Map<String, HostPortBinding[]> portBindings) {
		this.portBindings = portBindings;
	}

	@Override
	public String toString() {
		return "HostConfig [publishAllPorts=" + publishAllPorts + ", privileged=" + privileged + ", binds="
				+ Arrays.toString(binds) + ", links=" + Arrays.toString(links) + ", containerIDFile=" + containerIDFile
				+ ", lxcConf=" + Arrays.toString(lxcConf) + ", portBindings=" + portBindings + "]";
	}

}
