package com.github.filipesperandio.vraptor.hypermedia.json;

import br.com.caelum.vraptor.ComponentRegistry;
import br.com.caelum.vraptor.deserialization.JsonDeserializer;
import br.com.caelum.vraptor.ioc.spring.SpringProvider;
import br.com.caelum.vraptor.restfulie.DefaultRestfulie;
import br.com.caelum.vraptor.restfulie.Restfulie;
import br.com.caelum.vraptor.serialization.JSONSerialization;

import com.github.filipesperandio.vraptor.hypermedia.json.gson.HypermediaDeserializer;
import com.github.filipesperandio.vraptor.hypermedia.json.gson.HypermediaSerialization;

/**
 * Add this provider to you app by setting up web.xml with the following:
 * 
 * <pre>
 * {@code
 *  <context-param>
 *    <param-name>br.com.caelum.vraptor.provider</param-name>
 *    <param-value>com.github.filipesperandio.vraptor.hypermedia.json.HypermediaProvider</param-value>
 *  </context-param>
 * }
 * </pre>
 * 
 * @author filipesperandio
 * 
 */
public class HypermediaProvider extends SpringProvider {
	@Override
	protected void registerCustomComponents(ComponentRegistry registry) {
		registry.register(JsonDeserializer.class, HypermediaDeserializer.class);
		registry.register(JSONSerialization.class,
				HypermediaSerialization.class);
		registry.register(Restfulie.class, DefaultRestfulie.class);

	}
}
