package com.kpelykh.docker.client.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.testinfected.hamcrest.jpa.HasFieldWithValue.hasField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.CommitConfig;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ImageInspectResponse;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.Version;

// https://docs.docker.com/reference/api/docker_remote_api_v1.12/#23-misc
public class DockerMiscEndpointsTest extends AbstractDockerClientTest {

	public static final Logger LOG = LoggerFactory.getLogger(DockerClientTest.class);

    @Test
    public void shouldBeAbleToAddFileViaBuild() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("testAddFile").getFile());
        dockerfileBuild(baseDir, "Successfully executed testrun.sh");
    }

    @Test
    public void shouldBeAbleToAddFolderViaBuild() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("testAddFolder").getFile());
        dockerfileBuild(baseDir, "Successfully executed testAddFolder.sh");
    }

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

    @Test
    public void shouldBeAbleToCommitModifiedContainer() throws DockerException {

    	ContainerCreateResponse busybox = createBusybox("touch", "/test");
        dockerClient.startContainer(busybox.getId());

        LOG.info("Committing container: {}", busybox.toString());
        String committed = dockerClient.commit(new CommitConfig(busybox.getId()));
        tmpImgs.add(committed);

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(committed);
        LOG.info("Image Inspect: {}", imageInspectResponse.toString());

        assertThat(imageInspectResponse, hasField("container", startsWith(busybox.getId())));
        assertThat(imageInspectResponse.getContainerConfig().getImage(), equalTo("busybox"));

        ImageInspectResponse busyboxImg = dockerClient.inspectImage("busybox");
        assertThat(imageInspectResponse.getParent(), equalTo(busyboxImg.getId()));
    }

    private void dockerfileBuild(File baseDir, String expectedText) throws DockerException, IOException {

        InputStream response = dockerClient.build(baseDir);

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
        assertThat(fullLog, containsString("Successfully built"));

        String imageId = extractImageId(fullLog);

        //Create container based on image
        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage(imageId);
        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));

        dockerClient.startContainer(container.getId());
        dockerClient.waitContainer(container.getId());

        tmpContainers.add(container.getId());

        //Log container
        InputStream logResponse = dockerClient.logContainer(container.getId());

        StringWriter logwriter2 = new StringWriter();

        try {
            LineIterator itr = IOUtils.lineIterator(logResponse, "UTF-8");
            while (itr.hasNext()) {
                String line = (String) itr.next();
                logwriter2.write(line + (itr.hasNext() ? "\n" : ""));
                LOG.info(line);
            }
        } finally {
            IOUtils.closeQuietly(logResponse);
        }

        assertThat(logwriter2.toString(), containsString(expectedText));
//        assertThat(logwriter2.toString(), endsWith(expectedText));
    }

}
