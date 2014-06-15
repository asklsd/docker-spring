package com.kpelykh.docker.client.test;

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.testinfected.hamcrest.jpa.HasFieldWithValue.hasField;

import java.io.IOException;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.NotFoundException;
import com.kpelykh.docker.client.model.CommitConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.Image;
import com.kpelykh.docker.client.model.ImageInspectResponse;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.SearchItem;

// https://docs.docker.com/reference/api/docker_remote_api_v1.12/#22-images
public class DockerImagesEndpointsTest extends AbstractDockerClientTest {

	@Test
	public void shouldBeAbleToFindAllImages() throws DockerException {
		List<Image> images = dockerClient.getImages(true);
		assertThat(images, notNullValue());
		LOG.info("Images List: " + images);
		Info info = dockerClient.info();

		assertThat(images.size(), equalTo(info.getImages()));
	}

	@Test
	public void shouldBeAbleToFindAndReadFirstImage() throws DockerException {
		List<Image> images = dockerClient.getImages(true);

		Image img = images.get(0);
		assertThat(img.getCreated(), is(greaterThan(0L)));
		assertThat(img.getVirtualSize(), is(greaterThan(0L)));
		assertThat(img.getId(), not(isEmptyString()));
		assertThat(img.getTag(), not(isEmptyString()));
		assertThat(img.getRepository(), not(isEmptyString()));
	}

    @Test
    public void shouldBeAbleToPullImage() throws DockerException, IOException {

		// This should be an image that is not used by other repositories already
		// pulled down, preferably small in size. If tag is not used pull will
		// download all images in that repository but tmpImgs will only
		// deleted 'latest' image but not images with other tags
		String testImage = "hackmann/empty";

        LOG.info("Removing image: {}", testImage);
        try {
			dockerClient.removeImage(testImage);
		} catch (NotFoundException ignore) {
		}

        Info info = dockerClient.info();
        LOG.info("Client info: {}", info.toString());
        int imgCount= info.getImages();

        LOG.info("Pulling image: {}", testImage);
        dockerClient.pull(testImage);

        tmpImgs.add(testImage);

        info = dockerClient.info();
        LOG.info("Client info after pull, {}", info.toString());
        assertThat(imgCount, lessThan(info.getImages()));

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(testImage);
        LOG.info("Image Inspect: {}", imageInspectResponse.toString());
        assertThat(imageInspectResponse, notNullValue());
    }

    @Test
	public void shouldBeAbleToInspectAnImage() throws Exception {

		ImageInspectResponse imageInspectResponse = dockerClient.inspectImage("busybox");
		LOG.info("Image Inspect: {}", imageInspectResponse.toString());

		assertThat(imageInspectResponse.getAuthor(), containsString("Jérôme Petazzoni"));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldBeAbleToRemoveCommittedImage() throws DockerException, InterruptedException {

		ContainerCreateResponse busyboxContainer = createBusybox("touch", "/test");
        dockerClient.startContainer(busyboxContainer.getId());

        LOG.info("Committing container {}", busyboxContainer.toString());
        String imageId = dockerClient.commit(new CommitConfig(busyboxContainer.getId()));
        tmpImgs.add(imageId);

        dockerClient.stopContainer(busyboxContainer.getId());
        dockerClient.kill(busyboxContainer.getId());
        dockerClient.removeContainer(busyboxContainer.getId());
        tmpContainers.remove(busyboxContainer.getId());

        dockerClient.removeImage(imageId);
        tmpImgs.remove(imageId);

        List containers = dockerClient.listContainers(true);
        Matcher matcher = not(hasItem(hasField("id", startsWith(imageId))));
        assertThat(containers, matcher);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void shouldFindBusyBoxViaSearch() throws DockerException {

    	List<SearchItem> dockerSearch = dockerClient.search("busybox");

        LOG.info("Search returned {}", dockerSearch.toString());

        @SuppressWarnings("rawtypes")
		Matcher matcher = hasItem(hasField("name", equalTo("busybox")));
        assertThat(dockerSearch, matcher);

        assertThat(filter(hasField("name", is("busybox")), dockerSearch).size(), equalTo(1));
    }

}
