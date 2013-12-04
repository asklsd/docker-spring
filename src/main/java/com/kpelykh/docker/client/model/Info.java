package com.kpelykh.docker.client.model;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */
public class Info {

    @JsonProperty("Debug")
    public boolean debug;

    @JsonProperty("Containers")
    public int    containers;

    @JsonProperty("Images")
    public int    images;

    @JsonProperty("Driver")
    public String driver;

    @JsonProperty("DriverStatus")
    public String[][] driverStatus;

    @JsonProperty("NFd")
    public int    NFd;

    @JsonProperty("NGoroutines")
    public int    NGoroutines;

    @JsonProperty("MemoryLimit")
    public boolean memoryLimit;

    @JsonProperty("IPv4Forwarding")
    public String IPv4Forwarding;
    
    @JsonProperty("LXCVersion")
    public String lxcVersion;

    @JsonProperty("NEventsListener")
    public long nEventListener;

    @JsonProperty("KernelVersion")
    public String kernelVersion;

    @JsonProperty("IndexServerAddress")
    public String IndexServerAddress;

    @Override
    public String toString() {
        return "Info{" +
                "debug=" + debug +
                ", containers=" + containers +
                ", images=" + images +
                ", driver=" + driver +
                ", driverStatus=" + printDriverStatus() +
                ", NFd=" + NFd +
                ", NGoroutines=" + NGoroutines +
                ", memoryLimit=" + memoryLimit +
                ", lxcVersion='" + lxcVersion + '\'' +
                ", nEventListener=" + nEventListener +
                ", kernelVersion='" + kernelVersion + '\'' +
                ", IPv4Forwarding='" + IPv4Forwarding + '\'' +
                ", IndexServerAddress='" + IndexServerAddress + '\'' +
                '}';
    }

	private String printDriverStatus() {
		StringBuffer result = new StringBuffer();
		result.append("[");
		for (String[] entry : driverStatus) {
			result.append(Arrays.toString(entry));
			result.append(",");
		}
		result.append("]");
		return result.toString();
	}
}
