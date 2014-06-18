package com.kpelykh.docker.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.HostConfig;
import com.kpelykh.docker.client.model.Ports;
import com.kpelykh.docker.client.model.Ports.Port;

public class DockerTemplate implements DockerOperations {

	private static final Logger LOG = LoggerFactory.getLogger(DockerTemplate.class);

	private DockerClient dockerClient;

	public DockerTemplate(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	@Override
	public String build(String tag, String dockerFolderName) {
		LOG.info("Building new image '{}' from directory '{}'", tag, dockerFolderName);
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

	private String createPortBindingKey(int containerPort) {
		return Integer.toString(containerPort) + "/tcp";
	}

	@Override
	public String createContainer(String imageTag, String containerName, int... exposedPorts) {
		LOG.info("Creating new container from image '{}'...", imageTag);
		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setHostName("localhost");
		containerConfig.setImage(imageTag);
		for (int exposedPort : exposedPorts) {
			containerConfig.getExposedPorts().put(createPortBindingKey(exposedPort), null);
		}
		try {
			ContainerCreateResponse response = dockerClient.createContainer(containerConfig, containerName);
			LOG.info("Create container finished with: {}", response);
			return response.getId();
		} catch (DockerException e) {
			throw new RuntimeException("Failed to create new container.", e);
		}
	}

	@Override
	public void start(String containerId, int hostPort, int containerPort) {
		LOG.info("Starting container '{}' with portmapping {}:{}", containerId, hostPort, containerPort);
		try {
			HostConfig hostConfig = new HostConfig();
			Ports ports = hostConfig.getPortBindings();
			ports.addPort(new Port("tcp", Integer.toString(containerPort), "0.0.0.0", Integer.toString(hostPort)));
			LOG.debug("Using host config: {}", hostConfig);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				new ObjectMapper().writeValue(outputStream, hostConfig);
				LOG.debug("Using host config (JSON): {}.", new String(outputStream.toByteArray()));
			} catch (JsonGenerationException e1) {
				e1.printStackTrace();
			} catch (JsonMappingException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

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
			return response.getState().running;
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Container '" + containerName + "' does not exist.");
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.", e);
		}
	}

	public void stop(String containerId) {
		try {
			dockerClient.stopContainer(containerId);
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.");
		}
	}

	public void wait(String containerId) {
		try {
			dockerClient.waitContainer(containerId);
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.");
		}
	}

	public void remove(String containerId) {
		try {
			dockerClient.removeContainer(containerId);
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.");
		}
	}

	@Override
	public String getServerVersion() {
		try {
			return dockerClient.version().getVersion();
		} catch (DockerException e) {
			throw new RuntimeException("Failed to query the docker daemon.");
		}
	}

}
