package com.kpelykh.docker.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.HostConfig;
import com.kpelykh.docker.client.model.HostPortBinding;

public class DockerTemplate implements DockerOperations {

	private static final Logger LOG = LoggerFactory.getLogger(DockerTemplate.class);

	private DockerClient dockerClient;

	public DockerTemplate(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	@Override
	public String build(String tag, String dockerFolderName) {
		LOG.info("Building new image '{}' from direcotry '{}'", tag, dockerFolderName);
		File dockerFolder = new File(dockerFolderName);
		try {
			InputStream buildOutput = this.dockerClient.build(dockerFolder, tag);
			try {
				String buildLog = IOUtils.toString(buildOutput);
				LOG.info("New image '{}' successfully build from direcotry '{}'", tag, dockerFolderName);
				LOG.info("Build Log:\n" + buildLog);
				return buildLog;
			} catch (IOException e) {
				LOG.error("Failed to capture the build log.", e);
			}
		} catch (DockerException e) {
			throw new RuntimeException("Failed to build new image.", e);
		}
		return "Failed to capture the build log. Please consult the logfiles for more information.";
	}

	@Override
	public String create(String imageTag, int containerPort) {
		LOG.info("Creating new container from image '{}'...", imageTag);
		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setHostName("localhost");
		containerConfig.setImage(imageTag);
		containerConfig.setPortSpecs(new String[] {"127.0.0.1:80:8080"});
		// HashMap<String, String> hashMap = new HashMap<String, String>();
		// hashMap.put("18001", "8080");
		// containerConfig.getExposedPorts().put(Integer.toString(containerPort), hashMap);
		try {
			ContainerCreateResponse response = dockerClient.createContainer(containerConfig);
			LOG.info("Create container finished with: {}", response);
			return response.getId();
		} catch (DockerException e) {
			throw new RuntimeException("Failed to run new container.", e);
		}
	}

	@Override
	public void start(String containerId, int hostPort, int containerPort) {
		LOG.info("Starting container '{}' with portmapping {}:{}", containerId, hostPort, containerPort);
//		ContainerConfig containerConfig = new ContainerConfig();
//		containerConfig.setHostName("localhost");
		try {
			HostConfig hostConfig = new HostConfig();
//			Map<String, HostPortBinding[]> portBindings = hostConfig.getPortBindings();
//			HostPortBinding[] portBindingForContainerPort = new HostPortBinding[1];
//			portBindingForContainerPort[0] = new HostPortBinding("0.0.0.0", Integer.toString(hostPort));
//			portBindings.put(Integer.toString(containerPort) + "/tcp", portBindingForContainerPort);
//			LOG.info("Using host config: {}", hostConfig);
//			try {
//				new ObjectMapper().writeValue(new PrintWriter(System.out), hostConfig);
//			} catch (JsonGenerationException e) {
//				e.printStackTrace();
//			} catch (JsonMappingException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			dockerClient.startContainer(containerId, hostConfig);
			LOG.info("Container start successfully triggered.");
		} catch (DockerException e) {
			throw new RuntimeException("Failed to start container '" + containerId + "'.", e);
		}
	}

	@Override
	public boolean containerExists(String containerName) {
		try {
			dockerClient.inspectContainer(containerName);
			return true;
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.");
		} catch (HttpClientErrorException e) {
			return false;
		}
	}

	@Override
	public boolean isRunning(String containerName) {
		ContainerInspectResponse response;
		try {
			response = dockerClient.inspectContainer(containerName);
			return response.state.running;
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.");
		} catch (HttpClientErrorException e) {
			throw new IllegalArgumentException("Container '" + containerName + "' does not exist.");
		}
	}

}
