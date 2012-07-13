package com.github.filipesperandio.vraptor.hypermedia;

import java.io.InputStream;
import java.util.Scanner;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.deserialization.Deserializes;
import br.com.caelum.vraptor.deserialization.JsonDeserializer;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.interceptor.DefaultTypeNameExtractor;
import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.restfulie.Restfulie;
import br.com.caelum.vraptor.restfulie.serialization.MethodValueSupportConverter;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.xstream.XStreamBuilder;

import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;

@Component
@RequestScoped
@Deserializes({ "application/json", "json" })
public class HypermediaJSONDeserializer extends JsonDeserializer {

	private final XStreamBuilder builder;
	private final Restfulie restfulie;
	private final Router router;
	private final Proxifier proxifier;
	private final Configuration config;

	public HypermediaJSONDeserializer(ParameterNameProvider provider,
			TypeNameExtractor extractor, XStreamBuilder builder,
			Restfulie restfulie, Configuration config,
			ProxyInitializer initializer, Router router, Proxifier proxifier) {
		super(provider, extractor, builder);
		this.builder = builder;
		this.restfulie = restfulie;
		this.config = config;
		this.router = router;
		this.proxifier = proxifier;
	}

	@Override
	protected XStream getXStream() {
		XStream xStream = builder.configure(new HypermediaXStream(
				new DefaultTypeNameExtractor(), getHierarchicalStreamDriver()));
		xStream.registerConverter(hypermediaConverter(xStream));
		return xStream;
	}

	private HypermediaJSONConverter hypermediaConverter(XStream xStream) {
		MethodValueSupportConverter converter = new MethodValueSupportConverter(
				new ReflectionConverter(xStream.getMapper(),
						xStream.getReflectionProvider()));
		HypermediaJSONConverter hypermediaConverted = new HypermediaJSONConverter(
				converter, restfulie, config, router, proxifier);
		return hypermediaConverted;
	}

	@Override
	public Object[] deserialize(InputStream inputStream, ResourceMethod method) {
		Scanner s = new Scanner(inputStream);
		String json = s.useDelimiter("\\A").next();
		Object[] objects = new Object[1];
		objects[0] = new GsonBuilder().create().fromJson(json,
				method.getMethod().getParameterTypes()[0]);
		return objects;
	}
}
