package com.kpelykh.docker.client.model;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */
public class Container {

    @JsonProperty("Id")
    public String id;

    @JsonProperty("Names")
    public String[] names;

    @JsonProperty("Command")
    public String command;

    @JsonProperty("Image")
    public String image;

    @JsonProperty("Created")
    public long created;

    @JsonProperty("Status")
    public String status;

    @JsonProperty("Ports")
    public String[] ports;   //Example value "49164->6900, 49165->7100"

    @JsonProperty("SizeRw")
    public int sizeRw;

    @JsonProperty("Size")
    public int size;
    
    @JsonProperty("VirtualSize")
    public int virtualSize;
    
    @JsonProperty("SizeRootFs")
    public int sizeRootFs;

    @JsonProperty("Repository")
    public String repository;

    @JsonProperty("Tag")
    public String tag;

    @Override
    public String toString() {
        return "Container{" +
                "id='" + id + '\'' +
                ", command='" + command + '\'' +
                ", image='" + image + '\'' +
                ", created=" + created +
                ", status='" + status + '\'' +
                ", ports=" + Arrays.toString(ports) +
                ", sizeRw=" + sizeRw +
                ", size=" + size +
                ", virtualSize=" + virtualSize +
                ", sizeRootFs=" + sizeRootFs +
                ", repository=" + repository +
                ", tag=" + tag +
                '}';
    }
}
