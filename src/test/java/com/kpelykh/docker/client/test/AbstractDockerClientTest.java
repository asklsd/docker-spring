package com.kpelykh.docker.client.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "SimpleServiceTest-context.xml" })
public abstract class AbstractDockerClientTest {

	public static final Logger LOG = LoggerFactory.getLogger(AbstractDockerClientTest.class);

	@Rule
	public TestName testName = new TestName();

	@Autowired
	protected DockerClient dockerClient;

	protected List<String> tmpImgs;
	protected List<String> tmpContainers;

	@Before
	public void beforeMethod() throws DockerException {
		LOG.info("======================= BEFORETEST =======================");
		// TODO - flu support system property for testing
		// String url = System.getProperty("docker.url",
		// "http://localhost:4243");
		// TODO - flu support getting the live url form dockerClient
		// LOG.info("Connecting to Docker server at " + url);
		
		// TODO - flu support logging of response stream
		// logResponseStream(dockerClient.pull("busybox"));
		dockerClient.pull("busybox");
		// TODO - flu add test that asserts autowiring of dockerClient
		LOG.info("======================= END OF BEFORETEST =======================\n\n");

		tmpContainers = new ArrayList<String>();
		tmpImgs = new ArrayList<String>();
		LOG.info("################################## STARTING {} ##################################", testName.getMethodName());
	}

	@After
	public void afterMethod() {
		for (String container : tmpContainers) {
			LOG.info("Cleaning up temporary container " + container);
			try {
				dockerClient.stopContainer(container);
				dockerClient.kill(container);
				dockerClient.removeContainer(container);
			} catch (DockerException ignore) {
				LOG.error("Error during cleanup of test", ignore);
			}
		}

		for (String image : tmpImgs) {
			LOG.info("Cleaning up temporary image {}", image);
			try {
				dockerClient.removeImage(image);
			} catch (DockerException ignore) {
				LOG.error("Error during cleanup of test", ignore);
			}
		}

		LOG.info("################################## END OF {} ##################################\n", testName.getMethodName());
	}

	// protected String logResponseStream(ClientResponse response) {
	// String responseString;
	// try {
	// responseString = DockerClient.asString(response);
	// } catch (IOException e) {
	// throw new RuntimeException(e);
	// }
	// LOG.info("Container log: {}", responseString);
	// return responseString;
	// }

}
