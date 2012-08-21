package com.github.filipesperandio.vraptor.hypermedia.json;

import java.lang.reflect.Method;

import br.com.caelum.vraptor.resource.HttpMethod;
import br.com.caelum.vraptor.restfulie.relation.UriBasedRelation;

public class UrlAndHttpMethodRelation extends UriBasedRelation {

	private final HttpMethod method;

	public UrlAndHttpMethodRelation(String name, String uri,
			HttpMethod httpMethod) {
		super(name, uri);
		this.method = httpMethod;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public boolean matches(Method method) {
		return false;
	}
}
