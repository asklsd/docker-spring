package com.kpelykh.docker.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContainerTopResponse {

	@JsonProperty("Titles")
	private String[] titles;

	@JsonProperty("Processes")
	private String[][] processes;

	public String[] getTitles() {
		return titles;
	}

	public void setTitles(String[] titles) {
		this.titles = titles;
	}

	public String[][] getProcesses() {
		return processes;
	}

	public void setProcesses(String[][] processes) {
		this.processes = processes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Processes:\n");
		for (String[] eachProcess : processes) {
			for (int i = 0; i < eachProcess.length; i++) {
				sb.append(titles[i] + ": " + eachProcess[i] + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
