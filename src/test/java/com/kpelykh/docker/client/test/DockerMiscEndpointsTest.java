package com.kpelykh.docker.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.Version;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "SimpleServiceTest-context.xml" })
public class DockerMiscEndpointsTest {

	public static final Logger LOG = LoggerFactory.getLogger(DockerClientTest.class);

	@Autowired
	private DockerClient dockerClient;

	@Test
	public void shouldBeAbleToDisplaySystemWideInformation() throws DockerException {
		Info dockerInfo = dockerClient.info();

		assertTrue(dockerInfo.toString().contains("containers"));
		assertTrue(dockerInfo.toString().contains("images"));
		assertTrue(dockerInfo.toString().contains("debug"));

		assertTrue(dockerInfo.getImages() > 0);
		assertTrue(dockerInfo.getNFd() > 0);
		assertTrue(dockerInfo.getNGoroutines() > 0);
		assertTrue(dockerInfo.isMemoryLimit());
	}

	@Test
	public void shouldBeAbleToShowTheDockerVersionInformation() throws DockerException {
		Version version = dockerClient.version();
		LOG.info(version.toString());

		assertTrue(version.getGoVersion().length() > 0);
		assertTrue(version.getVersion().length() > 0);
		assertTrue(version.getGitCommit().length() > 0);

		assertEquals(StringUtils.split(version.getVersion(), ".").length, 3);
	}

	@Test
	public void shouldBeAbleToPingTheDockerDeamon() throws Exception {
		int pingResult = dockerClient.ping();

		assertEquals(200, pingResult);
	}
}
