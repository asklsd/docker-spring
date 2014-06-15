package com.kpelykh.docker.client.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;

import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.ImageInspectResponse;
import com.kpelykh.docker.client.model.Ports;

/**
 * Unit test for DockerClient.
 * 
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 */
public class DockerClientTest extends AbstractDockerClientTest {

	/*
	 * 
     * ################
     * ## MISC TESTS ##
     * ################
	 */

	@Test
	public void shouldBeAbleToRunQuotedCommands() throws DockerException {

		String[][] commands = new String[][] { 
				new String[] { "true" },
				new String[] { "echo", "\"The Young Descendant of Tepes & Septette for the Dead Princess\"" },
				new String[] { "echo", "-n", "'The Young Descendant of Tepes & Septette for the Dead Princess'" },
				new String[] { "/bin/sh", "-c", "echo Hello World" },
				new String[] { "/bin/sh", "-c", "echo", "'Hello World'" },
				new String[] { "echo", "'Night of Nights'" },
				new String[] { "true", "&&", "echo", "'Night of Nights'" }
		};

		for (String[] command : commands) {
			LOG.info("Running command: [{}]", Arrays.toString(command));
			ContainerCreateResponse createBusybox = createBusybox(command);
			dockerClient.startContainer(createBusybox.getId());
			int exitcode = dockerClient.waitContainer(createBusybox.getId()).getStatusCode();
			assertThat(exitcode, equalTo(0));
		}
	}

	@Test
	public void shouldBeAbleToBuildImageFromNginxDockerfile() throws DockerException, IOException {

		File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("nginx").getFile());

		InputStream response = dockerClient.build(baseDir);

		String fullLog = extractFullLog(response);
		assertThat(fullLog, containsString("Successfully built"));

		String imageId = extractImageId(fullLog);

		ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
		assertThat(imageInspectResponse, not(nullValue()));
		LOG.info("Image Inspect: {}", imageInspectResponse.toString());
		tmpImgs.add(imageInspectResponse.getId());

		assertThat(imageInspectResponse.getAuthor(), equalTo("Guillaume J. Charmes \"guillaume@dotcloud.com\""));
	}

	@Test
	public void testNetCatDockerfileBuilder() throws DockerException, IOException, InterruptedException {
		File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("netcat").getFile());

		InputStream response = dockerClient.build(baseDir);

		String fullLog = extractFullLog(response);
		assertThat(fullLog, containsString("Successfully built"));

		String imageId = extractImageId(fullLog);

		ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
		assertThat(imageInspectResponse, not(nullValue()));
		LOG.info("Image Inspect: {}", imageInspectResponse.toString());
		tmpImgs.add(imageInspectResponse.getId());

		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setImage(imageInspectResponse.getId());
		ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
		assertThat(container.getId(), not(isEmptyString()));
		dockerClient.startContainer(container.getId());
		tmpContainers.add(container.getId());

		ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(container.getId());

		assertThat(containerInspectResponse.getId(), notNullValue());
		assertThat(containerInspectResponse.getNetworkSettings().ports, notNullValue());

		// No use as such if not running on the server
		for (String portstr : containerInspectResponse.getNetworkSettings().ports.getAllPorts().keySet()) {
			Ports.Port p = containerInspectResponse.getNetworkSettings().ports.getAllPorts().get(portstr);
			int port = Integer.valueOf(p.getHostPort());
			LOG.info("Checking port {} is in use", port);
			assertThat(available(port), is(false));
		}

		dockerClient.stopContainer(container.getId(), 0);

		for (String portstr : containerInspectResponse.getNetworkSettings().ports.getAllPorts().keySet()) {
			Ports.Port p = containerInspectResponse.getNetworkSettings().ports.getAllPorts().get(portstr);
			int port = Integer.valueOf(p.getHostPort());
			LOG.info("Checking port {} is available", port);
			assertThat(available(port), is(true));
		}
	}

	private String extractFullLog(InputStream response) throws IOException {
		StringWriter logwriter = new StringWriter();
		try {
			LineIterator itr = IOUtils.lineIterator(response, "UTF-8");
			while (itr.hasNext()) {
				String line = (String) itr.next();
				logwriter.write(line + "\n");
				LOG.info(line);
			}
		} finally {
			IOUtils.closeQuietly(response);
		}

		String fullLog = logwriter.toString();
		return fullLog;
	}

	// UTIL

	/**
	 * Checks to see if a specific port is available.
	 * 
	 * @param port the port to check for availability
	 */
	private static boolean available(int port) {
		if (port < 1100 || port > 60000) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}

}