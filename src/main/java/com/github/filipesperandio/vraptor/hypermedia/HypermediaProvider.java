package com.github.filipesperandio.vraptor.hypermedia;

import br.com.caelum.vraptor.ComponentRegistry;
import br.com.caelum.vraptor.ioc.spring.SpringProvider;
import br.com.caelum.vraptor.restfulie.DefaultRestfulie;
import br.com.caelum.vraptor.restfulie.Restfulie;
import br.com.caelum.vraptor.serialization.JSONSerialization;

/**
 * Add this provider to you app by setting up web.xml with the following:
 * <pre> 
 * {@code
 *  <context-param>
 *    <param-name>br.com.caelum.vraptor.provider</param-name>
 *    <param-value>com.github.filipesperandio.vraptor.hypermedia.HypermediaProvider</param-value>
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
		registry.register(JSONSerialization.class,
				HypermediaSerializationJSON.class);
		registry.register(Restfulie.class, DefaultRestfulie.class);
	}
}
