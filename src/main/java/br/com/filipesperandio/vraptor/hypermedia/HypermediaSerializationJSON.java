package br.com.filipesperandio.vraptor.hypermedia;

import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.restfulie.Restfulie;
import br.com.caelum.vraptor.restfulie.serialization.MethodValueSupportConverter;
import br.com.caelum.vraptor.restfulie.serialization.RestfulSerializationJSON;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.xstream.XStreamBuilder;

@Component
@RequestScoped
public class HypermediaSerializationJSON extends RestfulSerializationJSON {

	private final XStreamBuilder builder;
	private final Restfulie restfulie;
	private final Configuration config;
	private final Proxifier proxifier;
	private final Router router;

	public HypermediaSerializationJSON(HttpServletResponse response,
			TypeNameExtractor extractor, Restfulie restfulie,
			Configuration config, ProxyInitializer initializer,
			XStreamBuilder builder, Router router, Proxifier proxifier) {
		super(response, extractor, restfulie, config, initializer, builder);
		this.restfulie = restfulie;
		this.config = config;
		this.builder = builder;
		this.router = router;
		this.proxifier = proxifier;
	}

	@Override
	protected XStream getXStream() {
		XStream xStream = builder.jsonInstance();
		withoutRoot();
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

}
