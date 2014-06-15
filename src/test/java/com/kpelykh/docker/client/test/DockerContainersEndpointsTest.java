package com.kpelykh.docker.client.test;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.selectUnique;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.testinfected.hamcrest.jpa.HasFieldWithValue.hasField;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.NotFoundException;
import com.kpelykh.docker.client.model.ChangeLog;
import com.kpelykh.docker.client.model.Container;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.ContainerTopResponse;

// https://docs.docker.com/reference/api/docker_remote_api_v1.12/#21-containers
public class DockerContainersEndpointsTest extends AbstractDockerClientTest {

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void shouldListContainers() throws DockerException {

		List<Container> containers = dockerClient.listContainers(true);
		assertThat(containers, notNullValue());
		LOG.info("Container List: {}", containers);

		int size = containers.size();

		ContainerCreateResponse busyboxContainer = createBusybox("echo");

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

		ContainerCreateResponse busyboxContainer = createBusybox();

		assertThat(busyboxContainer.getId(), not(isEmptyString()));

	}

	@Test(expected = NotFoundException.class)
	public void shouldNotBeAbleToCreateNewContainerForNonExistingImage() throws DockerException {
		ContainerConfig containerConfig = new ContainerConfig();
		containerConfig.setImage("does_ont_exist");
		containerConfig.setCmd(new String[] { "true" });

		dockerClient.createContainer(containerConfig);
	}

	@Test
	public void shouldBeAbleToInspectFreshlyStartedContainer() throws DockerException {

		ContainerCreateResponse busyboxContainer = createBusybox("sleep", "60");

		dockerClient.startContainer(busyboxContainer.getId());

		ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(busyboxContainer.getId());
		LOG.info("Container Inspect: {}", containerInspectResponse);

		assertThat(containerInspectResponse.getConfig(), is(notNullValue()));
		assertThat(containerInspectResponse.getId(), not(isEmptyString()));
		assertThat(containerInspectResponse.getId(), startsWith(busyboxContainer.getId()));
		assertThat(containerInspectResponse.getImageId(), not(isEmptyString()));
		assertThat(containerInspectResponse.getState(), is(notNullValue()));
		assertThat(containerInspectResponse.getState().running, is(true));
	}

	@Test
	public void shouldBeAbleInspectStoppedContainer() throws DockerException {

		ContainerCreateResponse busyboxContainer = createBusybox();

		dockerClient.startContainer(busyboxContainer.getId());
		int exitCode = dockerClient.waitContainer(busyboxContainer.getId()).getStatusCode();
		LOG.info("Container exit code: {}", exitCode);

		assertThat(exitCode, equalTo(0));

		ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(busyboxContainer.getId());
		LOG.info("Container Inspect: {}", containerInspectResponse);

		assertThat(containerInspectResponse.getState().running, is(equalTo(false)));
		assertThat(containerInspectResponse.getState().exitCode, is(equalTo(exitCode)));

	}

	@Test
	public void shouldBeAbleToListProcessesInRunningContainer() throws Exception {
		ContainerCreateResponse busyboxContainer = createBusybox("sleep 60");
		dockerClient.startContainer(busyboxContainer.getId());

		ContainerTopResponse containerTopResponse = dockerClient.top(busyboxContainer.getId());
		LOG.info("Container Top:  {}", containerTopResponse);

		assertThat(containerTopResponse.getProcesses().length, is(1));
	}

	@Test(expected = DockerException.class)
	public void shouldNotBeAbleToListProcessesWhenContainerIsNotStarted() throws Exception {
		ContainerCreateResponse busyboxContainer = createBusybox("sleep 60");

		dockerClient.top(busyboxContainer.getId());
	}

	@Test(expected = NotFoundException.class)
	public void shouldNotBeAbleToListProcessesInNonExistingContainer() throws Exception {
		dockerClient.top("id_does_not_exist");
	}

    @Test
    public void shouldBeAbleGetLogsFromStoppedContainer() throws DockerException, IOException {

        String snippet = "hello world";

        ContainerCreateResponse busyboxContainer = createBusybox(new String[] {"/bin/echo", snippet});

        dockerClient.startContainer(busyboxContainer.getId());
        tmpContainers.add(busyboxContainer.getId());

        int exitCode = dockerClient.waitContainer(busyboxContainer.getId()).getStatusCode();

        assertThat(exitCode, equalTo(0));

        InputStream response = dockerClient.logContainer(busyboxContainer.getId());

        String fullLog = IOUtils.toString(response);

        LOG.info("Container log: {}", fullLog);
        assertThat(fullLog, containsString(snippet));
    }

    @Test
    public void shouldBeAbleToDetectCreatedTestFileInStoppedContainer() throws DockerException {

    	ContainerCreateResponse busyboxContainer = createBusybox(new String[] {"touch", "/test"});

        dockerClient.startContainer(busyboxContainer.getId());
        dockerClient.waitContainer(busyboxContainer.getId()).getStatusCode();

        List<ChangeLog> filesystemDiff = dockerClient.containterDiff(busyboxContainer.getId());
        LOG.info("Container diff: {}", filesystemDiff.toString());

        assertThat(filesystemDiff.size(), equalTo(1));
        ChangeLog testChangeLog = selectUnique(filesystemDiff, hasField("path", equalTo("/test")));
        assertThat(testChangeLog, hasField("path", equalTo("/test")));
        assertThat(testChangeLog, hasField("kind", equalTo(1)));
    }

    @Test
    public void testStopContainer() throws DockerException {

    	ContainerCreateResponse busyboxContainer = createBusybox(new String[] {"sleep", "60"});
        dockerClient.startContainer(busyboxContainer.getId());

        LOG.info("Stopping container: {}", busyboxContainer.getId());
        dockerClient.stopContainer(busyboxContainer.getId(), 2); // 2 seconds timeout

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(busyboxContainer.getId());

        assertThat(containerInspectResponse.getState().running, is(false));
        assertThat(containerInspectResponse.getState().exitCode, not(equalTo(0)));
    }

    @Test
    public void restartContainer() throws DockerException {

    	ContainerCreateResponse busyboxContainer = createBusybox(new String[] {"sleep", "60"});
        dockerClient.startContainer(busyboxContainer.getId());

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(busyboxContainer.getId());
        String startTime = containerInspectResponse.getState().startedAt;

        dockerClient.restart(busyboxContainer.getId(), 2);

        ContainerInspectResponse containerInspectResponse2 = dockerClient.inspectContainer(busyboxContainer.getId());
        String startTime2 = containerInspectResponse2.getState().startedAt;

        assertThat(startTime, not(equalTo(startTime2)));

        assertThat(containerInspectResponse.getState().running, is(equalTo(true)));
    }

    @Test
    public void testKillContainer() throws DockerException {

    	ContainerCreateResponse busyboxContainer = createBusybox(new String[] {"sleep", "60"});
    	dockerClient.startContainer(busyboxContainer.getId());

        LOG.info("Killing container: {}", busyboxContainer.getId());
        dockerClient.kill(busyboxContainer.getId());

        ContainerInspectResponse containerInspectResponse = dockerClient.inspectContainer(busyboxContainer.getId());
        LOG.info("Container Inspect: {}", containerInspectResponse.toString());

        assertThat(containerInspectResponse.getState().running, is(false));
        assertThat(containerInspectResponse.getState().exitCode, not(equalTo(0)));
    }

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void removeContainer() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"true"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);

        dockerClient.startContainer(container.getId());
        dockerClient.waitContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Removing container: {}", container.getId());
        dockerClient.removeContainer(container.getId());
        tmpContainers.remove(container.getId());

        List containers2 = dockerClient.listContainers(true);
        Matcher matcher = not(hasItem(hasField("id", startsWith(container.getId()))));
        assertThat(containers2, matcher);
    }

}
