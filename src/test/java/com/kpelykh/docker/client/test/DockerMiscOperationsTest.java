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
public class DockerMiscOperationsTest {

	public static final Logger LOG = LoggerFactory.getLogger(DockerClientTest.class);

	@Autowired
	private DockerClient dockerClient;

	@Test
	public void shouldBeAbleToDisplaySystemWideInformation() throws DockerException {
		Info dockerInfo = dockerClient.info();

		assertTrue(dockerInfo.toString().contains("containers"));
		assertTrue(dockerInfo.toString().contains("images"));
		assertTrue(dockerInfo.toString().contains("debug"));

		assertTrue(dockerInfo.images > 0);
		assertTrue(dockerInfo.NFd > 0);
		assertTrue(dockerInfo.NGoroutines > 0);
		assertTrue(dockerInfo.memoryLimit);
	}

	@Test
	public void shouldBeAbleToShowTheDockerVersionInformation() throws DockerException {
		Version version = dockerClient.version();
		LOG.info(version.toString());

		assertTrue(version.goVersion.length() > 0);
		assertTrue(version.version.length() > 0);
		assertTrue(version.gitCommit.length() > 0);

		assertEquals(StringUtils.split(version.version, ".").length, 3);
	}

}
