package com.kpelykh.docker.client;


public interface DockerOperations {

	/**
	 * Build new Docker image.
	 * @param imageTag to give the new Docker image.
	 * @param dockerFolder where the Dockerfile is located.
	 * @return plain output from the build command.
	 */
	String build(String imageTag, String dockerFolder);

	/**
	 * Create a new container.
	 * @param imageTag to use.
	 * @param containerName to use.
	 * @param containerPort to expose.
	 * @return The id of the created container.
	 */
	String createContainer(String imageTag, String containerName, int containerPort);

	/**
	 * Start a container.
	 * @param containerId or container name of the container to start.
	 * @param hostPort to map.
	 * @param containerPort to map.
	 * @return The hash of the created container.
	 */
	void start(String containerId, int hostPort, int containerPort);

	boolean isRunning(String containerName);

	boolean containerExists(String containerName);

}
