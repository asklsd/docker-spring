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

	public static class HostPortBinding {
		public HostPortBinding(int hostPort) {
			this.hostPort = hostPort;
		}

		public int hostPort;
	}

	@JsonProperty("PublishAllPorts")
	public boolean publishAllPorts;

	@JsonProperty("Privileged")
	public boolean privileged;

	@JsonProperty("Binds")
	public String[] binds;

	@JsonProperty("Links")
	public String[] links;

	@JsonProperty("ContainerIDFile")
	public String containerIDFile;

	@JsonProperty("LxcConf")
	public LxcConf[] lxcConf;

	@JsonProperty("PortBindings")
	public Map<String, HostPortBinding[]> portBindings = new HashMap<String, HostConfig.HostPortBinding[]>();

	public HostConfig() {
	}

	public HostConfig(String[] binds) {
		this.binds = binds;
	}

	public String[] getBinds() {
		return binds;
	}

	public void setBinds(String[] binds) {
		this.binds = binds;
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

	public class LxcConf {
		@JsonProperty("Key")
		public String key;

		@JsonProperty("Value")
		public String value;

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

	}

	@Override
	public String toString() {
		return "HostConfig [publishAllPorts=" + publishAllPorts + ", privileged=" + privileged + ", binds="
				+ Arrays.toString(binds) + ", links=" + Arrays.toString(links) + ", containerIDFile=" + containerIDFile
				+ ", lxcConf=" + Arrays.toString(lxcConf) + ", portBindings=" + portBindings + "]";
	}

}
