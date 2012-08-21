package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.restfulie.serialization.RestfulSerializationJSON;
import br.com.caelum.vraptor.serialization.JSONSerialization;
import br.com.caelum.vraptor.serialization.NoRootSerialization;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.view.ResultException;

import com.github.filipesperandio.vraptor.hypermedia.json.HypermediaProvider;

/**
 * It replaces {@link RestfulSerializationJSON} generating link artifacts as
 * current javascript libraries expect
 * 
 * @author filipesperandio
 * @see HypermediaProvider
 */
@Component
@RequestScoped
public class HypermediaSerialization implements JSONSerialization {

	private final HttpServletResponse response;
	private final ProxyInitializer initializer;

	public HypermediaSerialization(HttpServletResponse response,
			ProxyInitializer initializer) {
		this.response = response;
		this.initializer = initializer;

	}

	protected SerializerBuilder getSerializer() {
		try {
			return new HypermediaSerializer(response.getWriter(), initializer);
		} catch (IOException e) {
			throw new ResultException("Unable to serialize data", e);
		}
	}

	@Override
	public <T> Serializer from(T object, String alias) {
		return getSerializer().from(object, alias);
	}

	@Override
	public boolean accepts(String format) {
		return "json".equalsIgnoreCase(format);
	}

	@Override
	public <T> Serializer from(T object) {
		return getSerializer().from(object);
	}

	@Override
	public <T> NoRootSerialization withoutRoot() {
		return this;
	}

	@Override
	public JSONSerialization indented() {
		return this;
	}

}
