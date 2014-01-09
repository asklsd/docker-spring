package com.kpelykh.docker.client.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerTemplate;

public class DockerContainerOperationsTest {

	private DockerTemplate uut = new DockerTemplate(new DockerClient());

	@Test
	public void shouldBeAbleToCreateAndStartContainer() throws Exception {
		String containerId = uut.create("eclipsesource/tabris-farm", 8080);
		uut.start(containerId, 18090, 8080);
		// uut.stop(containerId);
		// uut.remove(containerId);
	}

	@Test
	public void shouldBeAbleToInspectCreatedContainer() {
		String containerId = uut.create("eclipsesource/tabris-farm", 8080);

		assertFalse(uut.isRunning(containerId));
	}

	@Test
	public void shouldBeAbleToInspectRunningContainer() {
		String containerId = uut.create("eclipsesource/tabris-farm", 8080);

		uut.start(containerId, 18091, 8080);

		assertTrue(uut.isRunning(containerId));
	}

	@Test
	public void shouldBeAbleToCreateNamedContainer() throws Exception {
		String containerName = UUID.randomUUID().toString();
		String actual = uut.create("eclipsesource/tabris-farm", containerName, 8080);
		
		assertThat(actual, is(containerName));
	}

	@Test
	public void shouldBeAbleToCreateAndStartNamedContainer() throws Exception {
		String containerName = UUID.randomUUID().toString();
		uut.create("eclipsesource/tabris-farm", containerName, 8080);

		uut.start(containerName, 18092, 8080);

		assertTrue(uut.isRunning(containerName));

		// uut.stop(containerName);
		// uut.remove(containerName);
	}

}
