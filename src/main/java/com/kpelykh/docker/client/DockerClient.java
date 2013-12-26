package com.kpelykh.docker.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.objenesis.instantiator.basic.NewInstanceInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;
import com.kpelykh.docker.client.model.ChangeLog;
import com.kpelykh.docker.client.model.CommitConfig;
import com.kpelykh.docker.client.model.Container;
import com.kpelykh.docker.client.model.ContainerConfig;
import com.kpelykh.docker.client.model.ContainerCreateResponse;
import com.kpelykh.docker.client.model.ContainerInspectResponse;
import com.kpelykh.docker.client.model.ContainerWaitResponse;
import com.kpelykh.docker.client.model.HostConfig;
import com.kpelykh.docker.client.model.Image;
import com.kpelykh.docker.client.model.ImageInspectResponse;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.SearchItem;
import com.kpelykh.docker.client.model.Version;
import com.kpelykh.docker.client.utils.CompressArchiveUtil;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */
public class DockerClient
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClient.class);

    private RestTemplate restTemplate;

    private String dockerDeamonUrl;

    // info and version return ContentType text/plain which is ignored by the MJHMC by default.
	private RestTemplate textRestTemplate;

    public DockerClient() {
    	this("http://localhost:4243");
    }

    public DockerClient(String serverUrl) {
        dockerDeamonUrl = serverUrl;
        restTemplate = new RestTemplate();

		textRestTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = textRestTemplate.getMessageConverters();
		messageConverters.clear();
		MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>(converter.getSupportedMediaTypes());
		supportedMediaTypes.add(new MediaType("text", "plain"));
		messageConverters.add(converter);
		converter.setSupportedMediaTypes(supportedMediaTypes);
    }

    public void setDockerDeamonUrl(String dockerDeamonUrl) {
    	LOGGER.info("Changing docker deamon URL to '{}'", dockerDeamonUrl);
		this.dockerDeamonUrl = dockerDeamonUrl;
	}

    /**
     ** MISC API
     **/

    public Info info() throws DockerException {
		return textRestTemplate.getForObject(dockerDeamonUrl + "/info", Info.class);
    }

    public Version version() throws DockerException {
    	return textRestTemplate.getForObject(dockerDeamonUrl + "/version", Version.class);
    }

    /**
     ** IMAGES API
     **/

    public void pull(String repository) throws DockerException {
        this.pull(repository, null, null);
    }

    public void pull(String repository, String tag) throws DockerException {
        this.pull(repository, tag, null);
    }

    public void pull(String repository, String tag, String registry) throws DockerException {
        Preconditions.checkNotNull(repository, "Repository was not specified");

        if (StringUtils.countMatches(repository, ":") == 1) {
            String repositoryTag[] = StringUtils.split(repository);
            repository = repositoryTag[0];
            tag = repositoryTag[1];

        }

        Map<String,String> params = new HashMap<String, String>();
        params.put("tag", tag);
        params.put("fromImage", repository);
        params.put("registry", registry);

        restTemplate.exchange(dockerDeamonUrl
				+ "/images/create?tag={tag}&fromImage={fromImage}&registry={registry}", HttpMethod.POST, null, String.class, params);
    }

    public List<SearchItem> search(String search) throws DockerException {
		SearchItem[] response = restTemplate.getForObject(dockerDeamonUrl + "/images/search?term={search}", SearchItem[].class, search);
		return Arrays.asList(response);
    }

    public void removeImage(String imageId) throws DockerException {
        Preconditions.checkState(!StringUtils.isEmpty(imageId), "Image ID can't be empty");

		try {
			restTemplate.delete(dockerDeamonUrl + "/images/{imageId}", imageId);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
	            LOGGER.warn("Ignoring deletion of non existing image {}", imageId);
	            return;
			}
			throw e;
		}
    }

    public void removeImages(List<String> images) throws DockerException {
        Preconditions.checkNotNull(images, "List of images can't be null");

        for (String imageId : images) {
            removeImage(imageId);
        }
    }

    public String getVizImages() throws DockerException {
		return restTemplate.getForObject(dockerDeamonUrl + "/images/viz", String.class);
    }

    public List<Image> getImages() throws DockerException {
        return this.getImages(null, false);
    }

    public List<Image> getImages(boolean allContainers) throws DockerException {
        return this.getImages(null, allContainers);
    }

    public List<Image> getImages(String name) throws DockerException {
        return this.getImages(name, false);
    }

    public List<Image> getImages(String name, boolean allImages) throws DockerException {
    	Map<String,String> params = new HashMap<String, String>();
    	params.put("filter", name);
    	params.put("all", allImages ? "1" : "0");

    	Image[] response = restTemplate.getForObject(dockerDeamonUrl + "/images/json?filter={filter}&all={all}",
				Image[].class, params);
		return Arrays.asList(response);
    }

    public ImageInspectResponse inspectImage(String imageId) throws DockerException {
		return restTemplate.getForObject(dockerDeamonUrl + "/images/{imageId}/json",
				ImageInspectResponse.class, imageId);
    }

    /**
     ** CONTAINERS API
     **/

    public List<Container> listContainers(boolean listAll) {
		Container[] response = restTemplate.getForObject(dockerDeamonUrl + "/containers/json?all={all}", Container[].class, listAll);
		return Arrays.asList(response);
    }

    public ContainerCreateResponse createContainer(ContainerConfig config) throws DockerException {
    	final HttpHeaders requestHeaders = new HttpHeaders();
    	requestHeaders.setContentType(MediaType.APPLICATION_JSON);
    	requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    	final HttpEntity<ContainerConfig> requestEntity = new HttpEntity<ContainerConfig>(config, requestHeaders);

    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			new ObjectMapper().writeValue(outputStream, config);
			System.out.println(new String(outputStream.toByteArray()));
		} catch (JsonGenerationException e1) {
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String response = restTemplate.postForObject(dockerDeamonUrl + "/containers/create", requestEntity,
						String.class);
		try {
			return new ObjectMapper().readValue(response, ContainerCreateResponse.class);
		} catch (JsonParseException e) {
			throw new IllegalStateException(e);
		} catch (JsonMappingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
    }

    public void startContainer(String containerId) throws DockerException {
        this.startContainer(containerId, null);
    }

    public void startContainer(String containerId, HostConfig hostConfig) throws DockerException {
		restTemplate.postForLocation(dockerDeamonUrl + "/containers/{containerId}/start",
				hostConfig, containerId);
    }

    public ContainerInspectResponse inspectContainer(String containerId) throws DockerException {
		return restTemplate.getForObject(dockerDeamonUrl + "/containers/{containerId}/json",
						ContainerInspectResponse.class, containerId );
    }

    public void removeContainer(String container) throws DockerException {
        this.removeContainer(container, false);
    }

    public void removeContainer(String containerId, boolean removeVolumes) throws DockerException {
        Preconditions.checkState(!StringUtils.isEmpty(containerId), "Container ID can't be empty");

		try {
			restTemplate.delete(dockerDeamonUrl + "/containers/{containerId}?v={removeVolumes}", 
					containerId, removeVolumes ? "1" : "0" );
		} catch (HttpClientErrorException e) {
			throw new DockerException(e);
		}
    }

    public void removeContainers(List<String> containers, boolean removeVolumes) throws DockerException {
        Preconditions.checkNotNull(containers, "List of containers can't be null");

        for (String containerId : containers) {
            removeContainer(containerId, removeVolumes);
        }
    }

    public ContainerWaitResponse waitContainer(String containerId) throws DockerException {
    	return restTemplate.postForObject(dockerDeamonUrl + "/containers/{containerId}/wait", null, ContainerWaitResponse.class, containerId);
    }

    public InputStream logContainer(String containerId) throws DockerException {
        return logContainer(containerId, false);
    }

    public InputStream logContainerStream(String containerId) throws DockerException {
        return logContainer(containerId, true);
    }

    private InputStream logContainer(String containerId, boolean stream) throws DockerException {
    	
		ResponseExtractor<InputStream> responseExtractor = new ResponseExtractor<InputStream>() {
			@Override
			public InputStream extractData(ClientHttpResponse response) throws IOException {
				String result = IOUtils.toString(response.getBody());
				return new ByteArrayInputStream(result.getBytes());
			}
		};

        Map<String,String> params = new HashMap<String, String>();
        params.put("containerId", containerId);
        params.put("logs", "1");
        params.put("stdout", "1");
        params.put("stderr", "1");
        params.put("stream", stream ? "1" : "0"); // this parameter keeps stream open indefinitely

        return restTemplate.execute(dockerDeamonUrl + "/containers/{containerId}/attach?logs={logs}&stdout={stdout}&stderr={stderr}&stream={stream}",
        		HttpMethod.POST, null, responseExtractor, params);
    }

    public List<ChangeLog> containterDiff(String containerId) throws DockerException {
    	ChangeLog[] response = restTemplate.getForObject(dockerDeamonUrl + "/containers/{containerId}/changes", ChangeLog[].class, containerId);
		return Arrays.asList(response);
    }

    public void stopContainer(String containerId) throws DockerException {
        this.stopContainer(containerId, 10);
    }

    public void stopContainer(String containerId, int timeout) throws DockerException {
    	restTemplate.postForLocation(dockerDeamonUrl + "/containers/{containerId}/stop?t={timeout}", null, containerId, timeout);
    }

    public void kill(String containerId) throws DockerException {
    	restTemplate.postForLocation(dockerDeamonUrl + "/containers/{containerId}/kill", null, containerId);
    }

    public void restart(String containerId, int timeout) throws DockerException {
    	restTemplate.postForLocation(dockerDeamonUrl + "/containers/{containerId}/restart", null, containerId);
    }

    private static class CommitResponse {

        @JsonProperty("Id")
        public String id;

        @Override
        public String toString() {
            return "CommitResponse{" +
                    "id=" + id +
                    '}';
        }

    }
    public String commit(CommitConfig commitConfig) throws DockerException {
        Preconditions.checkNotNull(commitConfig.container, "Container ID was not specified");

        Map<String,String> params = new HashMap<String, String>();
        params.put("container", commitConfig.container);
        params.put("repo", commitConfig.repo);
        params.put("tag", commitConfig.tag);
        params.put("m", commitConfig.message);
        params.put("author", commitConfig.author);
        params.put("run", commitConfig.run);

        String response = restTemplate.postForObject(dockerDeamonUrl + "/commit?container={container}&repo={repo}&tag={tag}&m={m}&author={author}&run={run}",
        		null, String.class, params);

		try {
			return new ObjectMapper().readValue(response, CommitResponse.class).id;
		} catch (JsonParseException e) {
			throw new IllegalStateException(e);
		} catch (JsonMappingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
    }

    public InputStream build(File dockerFolder) throws DockerException {
        return this.build(dockerFolder, null);
    }

    public InputStream build(File dockerFolder, String tag) throws DockerException {
        Preconditions.checkNotNull(dockerFolder, "Folder is null");
        Preconditions.checkArgument(dockerFolder.exists(), "Folder %s doesn't exist", dockerFolder);
        Preconditions.checkState(new File(dockerFolder, "Dockerfile").exists(), "Dockerfile doesn't exist in " + dockerFolder);

        // ARCHIVE TAR
        String archiveNameWithOutExtension = UUID.randomUUID().toString();

        File dockerFolderTar = null;
        File tmpDockerContextFolder = null;

        try {
            File dockerFile = new File(dockerFolder, "Dockerfile");
            List<String> dockerFileContent = FileUtils.readLines(dockerFile);

            if (dockerFileContent.size() <= 0) {
                throw new DockerException(String.format("Dockerfile %s is empty", dockerFile));
            }

            //Create tmp docker context folder
//            tmpDockerContextFolder = new File(FileUtils.getTempDirectoryPath(), "docker-java-build" + archiveNameWithOutExtension);
            tmpDockerContextFolder = new File("/tmp", "docker-java-build" + archiveNameWithOutExtension);

            FileUtils.copyFileToDirectory(dockerFile, tmpDockerContextFolder);

            for (String cmd : dockerFileContent) {
                if (StringUtils.startsWithIgnoreCase(cmd.trim(), "ADD")) {
                    String addArgs[] = StringUtils.split(cmd, " \t");
                    if (addArgs.length != 3) {
                        throw new DockerException(String.format("Wrong format on line [%s]", cmd));
                    }

                    File src = new File(addArgs[1]);
                    if (!src.isAbsolute()) {
                        src = new File(dockerFolder, addArgs[1]).getCanonicalFile();
                    }

                    if (!src.exists()) {
                        throw new DockerException(String.format("Source file %s doesnt' exist", src));
                    }
                    if (src.isDirectory()) {
                        FileUtils.copyDirectory(src, tmpDockerContextFolder);
                    } else {
                        FileUtils.copyFileToDirectory(src, tmpDockerContextFolder);
                    }
                }
            }

            dockerFolderTar = CompressArchiveUtil.archiveTARFiles(tmpDockerContextFolder, archiveNameWithOutExtension);

        } catch (IOException ex) {
            FileUtils.deleteQuietly(dockerFolderTar);
            FileUtils.deleteQuietly(tmpDockerContextFolder);
            throw new DockerException("Error occurred while preparing Docker context folder.", ex);
        }

		final HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Content-Type", "application/tar");
		HttpEntity<byte[]> requestEntity;
		try {
			FileInputStream openInputStream = FileUtils.openInputStream(dockerFolderTar);
			byte[] byteArray = IOUtils.toByteArray(openInputStream);
			requestEntity = new HttpEntity<byte[]>(byteArray, requestHeaders);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

        final ResponseEntity<String> response = restTemplate.exchange(dockerDeamonUrl + "/build?t={tag}", HttpMethod.POST, requestEntity, String.class,
        		tag);

        return new ByteArrayInputStream(response.getBody().getBytes());
    }

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

}
