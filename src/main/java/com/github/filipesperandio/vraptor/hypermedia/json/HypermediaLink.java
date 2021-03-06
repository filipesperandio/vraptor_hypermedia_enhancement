package com.github.filipesperandio.vraptor.hypermedia.json;

import br.com.caelum.vraptor.resource.HttpMethod;

public class HypermediaLink {
	private final HttpMethod method;
	private final String url;

	public HypermediaLink(HttpMethod method, String url) {
		this.method = method;
		this.url = url;
	}
	
	public HypermediaLink(UrlAndHttpMethodRelation h) {
		this(h.getMethod(), h.getUri());
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}
}
