package com.kpelykh.docker.client;

import org.springframework.web.client.HttpClientErrorException;

/**
 * Indicates that the given entity does not exist.
 *
 * @author Ryan Campbell ryan.campbell@gmail.com
 */
public class NotFoundException extends DockerException {

	private static final long serialVersionUID = 3728919812979795069L;

	public NotFoundException(String message) {
        super(message);
    }

	public NotFoundException(String message, HttpClientErrorException cause) {
		super(message, cause);
	}

}
