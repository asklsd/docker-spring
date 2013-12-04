package com.kpelykh.docker.client.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 * 
 */
public class SearchItem {

	@JsonProperty("star_count")
	public int starCount;

	@JsonProperty("is_official")
	public boolean isOfficial;

	public String name;

	@JsonProperty("is_trusted")
	public boolean isTrusted;

	public String description;

	@Override
	public String toString() {
		return "star_count='" + starCount + '\'' + ", is_official='" + isOfficial + '\'' + ", name='" + name + '\''
				+ ", is_trusted='" + isTrusted + '\'' + ", description='" + description + '\'' + '}';
	}
}
