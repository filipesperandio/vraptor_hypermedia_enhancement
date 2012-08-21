package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.serialization.xstream.Serializee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HypermediaSerializer implements SerializerBuilder {

	private final Writer writer;
	private final ProxyInitializer initializer;

	private final Serializee serializee = new Serializee();

	public HypermediaSerializer(Writer writer, ProxyInitializer initializer) {
		this.writer = writer;
		this.initializer = initializer;
	}

	public Serializer exclude(String... names) {
		serializee.excludeAll(names);
		return this;
	}

	private void preConfigure(Object obj, String alias) {
		checkNotNull(obj, "You can't serialize null objects");
		serializee.setRootClass(initializer.getActualClass(obj));
		setRoot(obj);
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
		list = (Collection<Object>) obj;
		serializee.setElementTypes(findElementTypes(list));
		return list;
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
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(serializee.getRoot());
		write(json);
	}

	private void write(String json) {
		try {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Serializer recursive() {
		this.serializee.setRecursive(true);
		return this;
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
