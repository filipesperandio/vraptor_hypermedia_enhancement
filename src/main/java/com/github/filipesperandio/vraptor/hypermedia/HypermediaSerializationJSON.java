package com.github.filipesperandio.vraptor.hypermedia;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.interceptor.DefaultTypeNameExtractor;
import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.restfulie.Restfulie;
import br.com.caelum.vraptor.restfulie.serialization.MethodValueSupportConverter;
import br.com.caelum.vraptor.restfulie.serialization.RestfulSerializationJSON;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.serialization.xstream.XStreamBuilder;
import br.com.caelum.vraptor.view.ResultException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.json.JsonWriter.Format;

/**
 * It replaces {@link RestfulSerializationJSON} generating link artifacts as
 * current javascript libraries expect
 * 
 * @author filipesperandio
 * @see HypermediaProvider
 */
@Component
@RequestScoped
public class HypermediaSerializationJSON extends RestfulSerializationJSON {

	protected final XStreamBuilder builder;
	protected final Restfulie restfulie;
	protected final Configuration config;
	protected final Proxifier proxifier;
	protected final Router router;
	private XStream xstream;

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
		xstream = builder.configure(new HypermediaXStream(
				new DefaultTypeNameExtractor(), getHierarchicalStreamDriver()));
		xstream.registerConverter(hypermediaConverter(xstream));
		return xstream;
	}

	public String toJson(Object o) {
		try {
			return getXStream().toXML(o);
		} catch (Exception e) {
			return null;
		}
	}

	private HypermediaJSONConverter hypermediaConverter(XStream xStream) {
		MethodValueSupportConverter converter = new MethodValueSupportConverter(
				new ReflectionConverter(xStream.getMapper(),
						xStream.getReflectionProvider()));
		HypermediaJSONConverter hypermediaConverted = new HypermediaJSONConverter(
				converter, restfulie, config, router, proxifier);
		return hypermediaConverted;
	}

	protected HierarchicalStreamDriver getHierarchicalStreamDriver() {
		final String newLine = "\n";
		final char[] lineIndenter = { ' ', ' ' };

		return new JsonHierarchicalStreamDriver() {
			public HierarchicalStreamWriter createWriter(Writer writer) {
				return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE,
						new Format(lineIndenter, newLine.toCharArray(),
								Format.SPACE_AFTER_LABEL
										| Format.COMPACT_EMPTY_ELEMENT));
			}
		};
	}

	@Override
	protected SerializerBuilder getSerializer() {
		try {
			return new HypermediaXStreamSerializer(getXStream(),
					response.getWriter(), extractor, initializer);
		} catch (IOException e) {
			throw new ResultException("Unable to serialize data", e);
		}
	}

}
