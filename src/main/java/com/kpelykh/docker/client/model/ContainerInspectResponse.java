package com.kpelykh.docker.client.model;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */
public class ContainerInspectResponse {

    @JsonProperty("ID") public String id;
    @JsonProperty("Name") public String name;
    @JsonProperty("Created") public String created;
    @JsonProperty("Path") public String path;
    @JsonProperty("HostnamePath") public String hostnamePath;
    @JsonProperty("HostConfig") public HostConfig hostConfig;
    @JsonProperty("HostsPath") public String hostsPath;
    @JsonProperty("Args") public String[] args;
    @JsonProperty("Config") public ContainerConfig config;
    @JsonProperty("State") public ContainerState state;
    @JsonProperty("Image") public String image;
    @JsonProperty("NetworkSettings") public NetworkSettings networkSettings;
    @JsonProperty("SysInitPath") public String sysInitPath;
    @JsonProperty("ResolvConfPath") public String resolvConfPath;
    @JsonProperty("Volumes") public Map<String, String> volumes;
    @JsonProperty("VolumesRW") public Map<String, String> volumesRW;
    @JsonProperty("Driver") public String driver;

    @Override
    public String toString() {
        return "ContainerInspectResponse{" +
                "volumes=" + volumes +
                ", volumesRW=" + volumesRW + '\'' +
                ", resolvConfPath='" + resolvConfPath + '\'' +
                ", sysInitPath='" + sysInitPath + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", created='" + created + '\'' +
                ", path='" + path + '\'' +
                ", hostnamePath='" + hostnamePath + '\'' +
                ", hostsPath='" + hostsPath + '\'' +
                ", args=" + Arrays.toString(args) +
                ", config=" + config +
                ", hostConfig=" + hostConfig +
                ", state=" + state +
                ", image='" + image + '\'' +
                ", networkSettings=" + networkSettings +
                ", driver='" + driver + '\'' +
                '}';
    }

    public class NetworkSettings {

        @JsonProperty("IPAddress") public String ipAddress;
        @JsonProperty("IPPrefixLen") public int ipPrefixLen;
        @JsonProperty("Gateway") public String gateway;
        @JsonProperty("Bridge") public String bridge;
        @JsonProperty("PortMapping") public Map<String,Map<String, String>> portMapping;
        @JsonProperty("Ports") public Map<String, HostPortBinding[]> ports;

		@Override
		public String toString() {
			return "NetworkSettings [ipAddress=" + ipAddress + ", ipPrefixLen=" + ipPrefixLen + ", gateway=" + gateway
					+ ", bridge=" + bridge + ", portMapping=" + portMapping + ", ports=" + ports + "]";
		}
    }

    public class ContainerState {

        @JsonProperty("Running") public boolean running;
        @JsonProperty("Pid") public int pid;
        @JsonProperty("ExitCode") public int exitCode;
        @JsonProperty("StartedAt") public String startedAt;
        @JsonProperty("FinishedAt") public String finishedAt;
        @JsonProperty("Ghost") public boolean ghost;

        @Override
        public String toString() {
            return "ContainerState{" +
                    "running=" + running +
                    ", pid=" + pid +
                    ", exitCode=" + exitCode +
                    ", startedAt='" + startedAt + '\'' +
                    ", finishedAt='" + finishedAt + '\'' +
                    ", ghost=" + ghost +
                    '}';
        }
    }

}
