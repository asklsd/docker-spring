package com.kpelykh.docker.client.test;

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.testinfected.hamcrest.jpa.HasFieldWithValue.hasField;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.NotFoundException;
import com.kpelykh.docker.client.model.Container;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;

// https://docs.docker.com/reference/api/docker_remote_api_v1.12/#21-containers
public class DockerContainersEndpointsTest extends AbstractDockerClientTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void shouldListContainers() throws DockerException {

		List<Container> containers = dockerClient.listContainers(true);
		assertThat(containers, notNullValue());
		LOG.info("Container List: {}", containers);

		int size = containers.size();

		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setImage("busybox");
		containerConfig.setCmd(new String[] { "echo" });

		ContainerCreateResponse busyboxContainer = dockerClient.createContainer(containerConfig);
		dockerClient.startContainer(busyboxContainer.getId());
		tmpContainers.add(busyboxContainer.getId());

		List<Container> containers2 = dockerClient.listContainers(true);
		assertThat(size + 1, is(equalTo(containers2.size())));

		Matcher matcher = hasItem(hasField("id", startsWith(busyboxContainer.getId())));
		assertThat(containers2, matcher);

		List<Container> filteredContainers = filter(hasField("id", startsWith(busyboxContainer.getId())), containers2);
		assertThat(filteredContainers.size(), is(equalTo(1)));

		Container container2 = filteredContainers.get(0);
		assertThat(container2.getCommand(), not(isEmptyString()));
		assertThat(container2.getImage(), startsWith("busybox"));
	}

	@Test
	public void shouldBeAbleToCreateNewContainerForExistingImage() throws DockerException {
		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setImage("busybox");
		containerConfig.setCmd(new String[] { "true" });

		ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
		LOG.info("Created container {} with id {}", container.toString(), container.getId());

		assertThat(container.getId(), not(isEmptyString()));

		tmpContainers.add(container.getId());
	}

	@Test(expected = NotFoundException.class)
	public void shouldNotBeAbleToCreateNewContainerForNonExistingImage() throws DockerException {
		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setImage("does_ont_exist");
		containerConfig.setCmd(new String[] { "true" });

		dockerClient.createContainer(containerConfig);
	}

}
