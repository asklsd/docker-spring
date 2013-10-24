package com.kpelykh.docker.client;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */

@SuppressWarnings("serial")
public class DockerException extends Exception {

    public DockerException() {
    }

    public DockerException(String message) {
        super(message);
    }

    public DockerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerException(Throwable cause) {
        super(cause);
    }
}
