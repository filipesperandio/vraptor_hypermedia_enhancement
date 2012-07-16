package com.github.filipesperandio.vraptor.hypermedia;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.xstream.OldAndProbablyBuggyConfigurer;
import br.com.caelum.vraptor.serialization.xstream.ProxyConverter;
import br.com.caelum.vraptor.serialization.xstream.Serializee;
import br.com.caelum.vraptor.serialization.xstream.VRaptorClassMapper;
import br.com.caelum.vraptor.serialization.xstream.XStreamSerializer;

import com.thoughtworks.xstream.XStream;

public class HypermediaXStreamSerializer extends XStreamSerializer {

	private final XStream xstream;
	private final Writer writer;
	private final TypeNameExtractor extractor;
	private final ProxyInitializer initializer;
	private final Serializee serializee = new Serializee();

	public HypermediaXStreamSerializer(XStream xstream, Writer writer,
			TypeNameExtractor extractor, ProxyInitializer initializer) {
		super(xstream, writer, extractor, initializer);
		this.xstream = xstream;
		this.writer = writer;
		this.extractor = extractor;
		this.initializer = initializer;
	}

	public Serializer exclude(String... names) {
		serializee.excludeAll(names);
		return this;
	}

	private void preConfigure(Object obj, String alias) {
		checkNotNull(obj, "You can't serialize null objects");

		xstream.processAnnotations(obj.getClass());

		serializee.setRootClass(initializer.getActualClass(obj));
		if (alias == null && initializer.isProxy(obj.getClass())) {
			alias = extractor.nameFor(serializee.getRootClass());
		}

		setRoot(obj);

		setAlias(obj, alias);
	}

	private void setRoot(Object obj) {
		if (Collection.class.isInstance(obj)) {
			this.serializee.setRoot(normalizeList(obj));
		} else {
			this.serializee.setRoot(obj);
		}
	}

	private Collection<Object> normalizeList(Object obj) {
		Collection<Object> list;
		if (hasDefaultConverter()) {
			list = new ArrayList<Object>((Collection<?>) obj);
		} else {
			list = (Collection<Object>) obj;
		}
		serializee.setElementTypes(findElementTypes(list));
		return list;
	}

	private boolean hasDefaultConverter() {
		return xstream
				.getConverterLookup()
				.lookupConverterForType(serializee.getRootClass())
				.equals(xstream.getConverterLookup().lookupConverterForType(
						Object.class));
	}

	private void setAlias(Object obj, String alias) {
		if (alias != null) {
			if (Collection.class.isInstance(obj)
					&& (List.class.isInstance(obj) || hasDefaultConverter())) {
				xstream.alias(alias, List.class);
			}
			xstream.alias(alias, obj.getClass());
		}
	}

	public <T> Serializer from(T object, String alias) {
		preConfigure(object, alias);
		return this;
	}

	public <T> Serializer from(T object) {
		preConfigure(object, null);
		return this;
	}

	private Set<Class<?>> findElementTypes(Collection<Object> list) {
		Set<Class<?>> set = new HashSet<Class<?>>();
		for (Object element : list) {
			if (element != null && !isPrimitive(element.getClass())) {
				set.add(initializer.getActualClass(element));
			}
		}
		return set;
	}

	public Serializer include(String... fields) {
		serializee.includeAll(fields);
		return this;
	}

	public void serialize() {
		if (xstream instanceof HypermediaXStream) {
			VRaptorClassMapper mapper = ((HypermediaXStream) xstream)
					.getVRaptorMapper();
			mapper.setSerializee(serializee);
		} else {
			new OldAndProbablyBuggyConfigurer(xstream).configure(serializee);
		}

		registerProxyInitializer();
		Object rootObj = serializee.getRoot();
		if (rootObj instanceof Collection && ((Collection) rootObj).isEmpty()) {
			try {
				writer.append("[]");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			xstream.toXML(rootObj, writer);
		}
	}

	public Serializer recursive() {
		this.serializee.setRecursive(true);
		return this;
	}

	private void registerProxyInitializer() {
		xstream.registerConverter(new ProxyConverter(initializer, xstream));
	}

	static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive() || type.isEnum()
				|| Number.class.isAssignableFrom(type)
				|| type.equals(String.class)
				|| Date.class.isAssignableFrom(type)
				|| Calendar.class.isAssignableFrom(type)
				|| Boolean.class.equals(type) || Character.class.equals(type);
	}

}
