package com.kpelykh.docker.client.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerTemplate;

public class DockerContainerOperationsTest {

	private static final String IMAGE_NAME = "eclipsesource/virgo-tabris-farm";

	private DockerTemplate uut = new DockerTemplate(new DockerClient());

	@Test
	public void shouldBeAbleToCreateAndStartContainer() throws Exception {

		String containerId = uut.create(IMAGE_NAME, null, 8080);
		uut.start(containerId, 18090, 8080);

		uut.stop(containerId);
		uut.remove(containerId);
	}

	@Test
	public void shouldBeAbleToInspectCreatedContainer() {
		String containerId = uut.create(IMAGE_NAME, null, 8080);

		assertFalse(uut.isRunning(containerId));

		uut.remove(containerId);
	}

	@Test
	public void shouldBeAbleToInspectRunningContainer() {
		String containerId = uut.create(IMAGE_NAME, null, 8080);
		uut.start(containerId, 18091, 8080);

		assertTrue(uut.isRunning(containerId));

		uut.stop(containerId);
		uut.remove(containerId);
	}

	@Test
	public void shouldBeAbleToCreateNamedContainer() throws Exception {
		String containerName = UUID.randomUUID().toString();
		String containerId = uut.create(IMAGE_NAME, containerName, 8080);

		assertFalse(uut.isRunning(containerId));

		uut.remove(containerId);
	}

	@Test
	public void shouldBeAbleToCreateAndStartNamedContainer() throws Exception {
		String containerName = UUID.randomUUID().toString();
		String containerId = uut.create(IMAGE_NAME, containerName, 8080);

		uut.start(containerName, 18092, 8080);

		assertTrue(uut.isRunning(containerId));
		assertTrue(uut.isRunning(containerName));

		uut.stop(containerName);
		uut.remove(containerName);
	}

}
