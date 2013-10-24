package com.kpelykh.docker.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class ContainerWaitResponse {

	@JsonProperty("StatusCode")
	private int statusCode;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
