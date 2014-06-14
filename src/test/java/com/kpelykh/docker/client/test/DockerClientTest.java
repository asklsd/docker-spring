package com.kpelykh.docker.client.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.testinfected.hamcrest.jpa.HasFieldWithValue.hasField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.kpelykh.docker.client.DockerException;
import com.kpelykh.docker.client.model.CommitConfig;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.ImageInspectResponse;
import com.kpelykh.docker.client.model.Ports;

/**
 * Unit test for DockerClient.
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 */
public class DockerClientTest extends AbstractDockerClientTest {

    /*
     * ##################
     * ## IMAGES TESTS ##
     * ##################
     * */


    //This test doesn't work in Ubuntu 12.04 due to
    //Error mounting '/dev/mapper/docker-8:5-...
    //ref: https://github.com/dotcloud/docker/issues/4036

    @Test
    public void commitImage() throws DockerException {

        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"touch", "/test"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Commiting container: {}", container.toString());
        String imageId = dockerClient.commit(new CommitConfig(container.getId()));
        tmpImgs.add(imageId);

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
        LOG.info("Image Inspect: {}", imageInspectResponse.toString());

        assertThat(imageInspectResponse, hasField("container", startsWith(container.getId())));
        assertThat(imageInspectResponse.getContainerConfig().getImage(), equalTo("busybox"));

        ImageInspectResponse busyboxImg = dockerClient.inspectImage("busybox");

        assertThat(imageInspectResponse.getParent(), equalTo(busyboxImg.getId()));
    }

    @Test
    public void testRemoveImage() throws DockerException, InterruptedException {


        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.setImage("busybox");
        containerConfig.setCmd(new String[] {"touch", "/test"});

        ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
        LOG.info("Created container: {}", container.toString());
        assertThat(container.getId(), not(isEmptyString()));
        dockerClient.startContainer(container.getId());
        tmpContainers.add(container.getId());

        LOG.info("Commiting container {}", container.toString());
        String imageId = dockerClient.commit(new CommitConfig(container.getId()));
        tmpImgs.add(imageId);

        dockerClient.stopContainer(container.getId());
        dockerClient.kill(container.getId());
        dockerClient.removeContainer(container.getId());

        tmpContainers.remove(container.getId());
        LOG.info("Removing image: {}", imageId);
        dockerClient.removeImage(imageId);

        List containers = dockerClient.listContainers(true);
        Matcher matcher = not(hasItem(hasField("id", startsWith(imageId))));
        assertThat(containers, matcher);
    }


    /*
     *
     * ################
     * ## MISC TESTS ##
     * ################
     */

    @Test
    public void testRunShlex() throws DockerException {

        String[] commands = new String[] {
                "true",
                "echo \"The Young Descendant of Tepes & Septette for the Dead Princess\"",
                "echo -n 'The Young Descendant of Tepes & Septette for the Dead Princess'",
                "/bin/sh -c echo Hello World",
                "/bin/sh -c echo 'Hello World'",
                "echo 'Night of Nights'",
                "true && echo 'Night of Nights'"
        };

        for (String command : commands) {
            LOG.info("Running command: [{}]",  command);

            ContainerConfig containerConfig = new ContainerConfig();
            containerConfig.setImage("busybox");
            containerConfig.setCmd( commands );

            ContainerCreateResponse container = dockerClient.createContainer(containerConfig);
            dockerClient.startContainer(container.getId());
            tmpContainers.add(container.getId());
            int exitcode = dockerClient.waitContainer(container.getId()).getStatusCode();
            assertThat(exitcode, equalTo(0));
        }
    }

    @Test
    public void testNginxDockerfileBuilder() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("nginx").getFile());

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

        ImageInspectResponse imageInspectResponse = dockerClient.inspectImage(imageId);
        assertThat(imageInspectResponse, not(nullValue()));
        LOG.info("Image Inspect: {}", imageInspectResponse.toString());
        tmpImgs.add(imageInspectResponse.getId());

        assertThat(imageInspectResponse.getAuthor(), equalTo("Guillaume J. Charmes \"guillaume@dotcloud.com\""));
    }

    @Test
    public void testDockerBuilderAddFile() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("testAddFile").getFile());
        dockerfileBuild(baseDir, "Successfully executed testrun.sh");
    }

    @Test
    public void testDockerBuilderAddFolder() throws DockerException, IOException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("testAddFolder").getFile());
        dockerfileBuild(baseDir, "Successfully executed testAddFolder.sh");
    }

//    @Test
//    // TODO - add new API
//    public void testImportImageFromTar() throws DockerException, IOException {
//        InputStream tar = Thread.currentThread().getContextClassLoader().getResourceAsStream("testImportImageFromTar/empty.tar");
//        String imageId = dockerClient.importImage("empty", null, tar).getId();
//        assert imageId.contains(dockerClient.inspectImage("empty").getId());
//    }

    @Test
    public void testNetCatDockerfileBuilder() throws DockerException, IOException, InterruptedException {
        File baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("netcat").getFile());

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

        //No use as such if not running on the server
        for(String portstr : containerInspectResponse.getNetworkSettings().ports.getAllPorts().keySet()){

         Ports.Port p = containerInspectResponse.getNetworkSettings().ports.getAllPorts().get(portstr);
         int port = Integer.valueOf(p.getHostPort());
         LOG.info("Checking port {} is open", port);
             assertThat(available(port), is(false));
        }
        dockerClient.stopContainer(container.getId(), 0);

//        LOG.info("Checking port {} is closed", port);
//        assertThat(available(port), is(true));
    }

    // UTIL

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean available(int port) {
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

    private void dockerfileBuild(File baseDir, String expectedText) throws DockerException, IOException {

        //Build image
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

	private String extractImageId(String fullLog) {
		String imageId = StringUtils.substringAfterLast(fullLog, "Successfully built ").trim();
        System.out.println(imageId);
        imageId = org.springframework.util.StringUtils.deleteAny(imageId, "\\n\"}");
        System.out.println(imageId);
		return imageId;
	}
}