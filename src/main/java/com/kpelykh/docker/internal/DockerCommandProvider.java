package com.kpelykh.docker.internal;

import static java.lang.System.out;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.kpelykh.docker.client.DockerClient;
import com.kpelykh.docker.client.DockerTemplate;
import com.kpelykh.docker.client.model.Container;
import com.kpelykh.docker.client.model.Info;
import com.kpelykh.docker.client.model.Version;

public class DockerCommandProvider {

	private DockerClient dockerClient;
	private DockerTemplate dockerTemplate;

	public DockerCommandProvider(DockerClient dockerClient, DockerTemplate dockerTemplate) {
		super();
		this.dockerClient = dockerClient;
		this.dockerTemplate = dockerTemplate;
	}

	public void info() {
		Info info = this.dockerClient.info();
		out.println("Containers: " + info.getContainers());
		out.println("Images: " + info.getImages());
		out.println("Storage Driver: " + info.getDriver());
		out.println("Execution Driver: " + info.getExecutionDriver());
		out.println("Containers: " + info.getKernelVersion());
		if (info.getSwapLimit() == 0) {
			out.println("WARNING: No swap limit support");
		} else {
			out.println("Swap Limit: " + info.getSwapLimit());
		}
	}

	public void version() {
		Version version = this.dockerClient.version();
		out.println("Server version: " + version.getVersion());
		out.println("Server API version: " + version.getApiVersion());
		out.println("Go version (server): " + version.getGoVersion());
		out.println("Git commit (server): " + version.getGitCommit());
	}

	public void build(String tag, String dockerFolderName) {
		File dockerFolder = new File(dockerFolderName);
		InputStream buildOutput = this.dockerClient.build(dockerFolder, tag);

		Pattern pattern = Pattern.compile(":\"(.*)\n\"}");

		Scanner scan = new Scanner(buildOutput, "UTF-8");
		while (scan.hasNextLine()) {
			String nextLine = scan.nextLine();
			String plain = StringEscapeUtils.unescapeJavaScript(nextLine);
			output(pattern, plain);
		}
	}

	public void create(String imageTag, String containerName) {
		this.dockerTemplate.createContainer(imageTag, containerName);
	}

	public void start(String containerId, int hostPort, int containerPort) {
		this.dockerTemplate.start(containerId, hostPort, containerPort);
	}

	private void output(Pattern pattern, String nextLine) {
		Matcher matcher = pattern.matcher(nextLine);
		if (matcher.find()) {
			out.println(matcher.group(1));
		}
	}

	public void ps() {
		final String psFormat = "%-15s  %-20s  %-50s  %-15s  %-25s  %-25s%n";
		out.format(psFormat, "CONTAINER ID", "IMAGE", "COMMAND", "STATUS", "PORTS", "NAMES");
		for (Container runningContainer : this.dockerClient.listContainers(false)) {
			String id = formatPsString(runningContainer.getId(), 15);
			String image = formatPsString(runningContainer.getImage(), 20);
			String cmd = formatPsString(runningContainer.getCommand(), 50);
			String status = formatPsString(runningContainer.getStatus(), 15);
			String ports = formatPsString(runningContainer.getPorts().toString(), 25);
			String names = formatPsString(StringUtils.join(runningContainer.getNames(), ","), 25);
			out.format(psFormat, id, image, cmd, status, ports, names);
		}
	}

	private String formatPsString(String stringToFormat, int maxLength) {
		return stringToFormat.substring(0, Math.min(stringToFormat.length(), maxLength));
	}
}
