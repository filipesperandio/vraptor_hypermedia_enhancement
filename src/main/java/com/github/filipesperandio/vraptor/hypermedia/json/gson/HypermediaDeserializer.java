package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import java.io.InputStream;
import java.util.Scanner;

import br.com.caelum.vraptor.deserialization.Deserializer;
import br.com.caelum.vraptor.deserialization.Deserializes;
import br.com.caelum.vraptor.deserialization.JsonDeserializer;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.serialization.xstream.XStreamBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
@ApplicationScoped
@Deserializes({ "application/json", "json" })
public class HypermediaDeserializer implements Deserializer {

	private Gson gson;

	public HypermediaDeserializer() {
		this.gson = new GsonBuilder().create();
	}

	@Override
	public Object[] deserialize(InputStream inputStream, ResourceMethod method) {
		String json = readToTheEnd(inputStream);
		Class<?> firstMethodParam = method.getMethod().getParameterTypes()[0];

		Object[] objects = new Object[1];
		objects[0] = gson.fromJson(json, firstMethodParam);
		return objects;
	}

	private String readToTheEnd(InputStream inputStream) {
		Scanner s = new Scanner(inputStream);
		return s.useDelimiter("\\A").next();
	}
}
