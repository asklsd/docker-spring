package com.kpelykh.docker.client.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerTemplate;

public class DockerContainerOperationsTest {

	private DockerTemplate uut = new DockerTemplate(new DockerClient());

	@Test
	public void shouldBeAbleToCreateAndStartContainer() throws Exception {
		String containerId = uut.create("eclipsesource/tabris-farm", 8080);
		uut.start(containerId, 18088, 8080);
//		uut.stop(containerId);
//		uut.remove(containerId);
	}

	@Test
	public void shouldBeAbleToInspectCreatedContainer() {
		String containerId = uut.create("eclipsesource/tabris-farm", 8080);

		assertFalse(uut.isRunning(containerId));
	}
	
	@Test
	public void shouldBeAbleToInspectRunningContainer() {
		String containerId = uut.create("eclipsesource/tabris-farm", 8080);

		uut.start(containerId, 18088, 8080);

		assertTrue(uut.isRunning(containerId));
	}
	
}
