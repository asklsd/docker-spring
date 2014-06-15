package com.kpelykh.docker.internal;

import static java.lang.System.out;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.Version;

public class DockerCommandProvider {

	private DockerClient dockerClient;

	public DockerCommandProvider(DockerClient dockerClient) {
		super();
		this.dockerClient = dockerClient;
	}

	public void info() {
		Info info = this.dockerClient.info();
		out.println("Containers: " + info.getContainers());
		out.println("Images: " + info.getImages());
		out.println("Storage Driver: " + info.getDriver());
		out.println("Execution Driver: " + info.getExecutionDriver());
		out.println("Containers: " + info.getKernelVersion());
		if (info.getSwapLimit() == 0) {
			out.println("WARNING: No swap limit support");
		} else {
			out.println("Swap Limit: " + info.getSwapLimit());
		}
	}

	public void version() {
		Version version = this.dockerClient.version();
		out.println("Server version: " + version.getVersion());
		out.println("Server API version: " + version.getApiVersion());
		out.println("Go version (server): " + version.getGoVersion());
		out.println("Git commit (server): " + version.getGitCommit());
	}

}
