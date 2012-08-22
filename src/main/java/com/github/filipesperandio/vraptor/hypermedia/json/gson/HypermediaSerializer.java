package com.github.filipesperandio.vraptor.hypermedia.json.gson;

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

import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.restfulie.hypermedia.HypermediaResource;
import br.com.caelum.vraptor.restfulie.relation.Relation;
import br.com.caelum.vraptor.restfulie.relation.RelationBuilder;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.serialization.xstream.Serializee;

import com.github.filipesperandio.vraptor.hypermedia.json.HypermediaLink;
import com.github.filipesperandio.vraptor.hypermedia.json.HypermediaRelationBuilder;
import com.github.filipesperandio.vraptor.hypermedia.json.UrlAndHttpMethodRelation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HypermediaSerializer implements SerializerBuilder {

	private final Writer writer;
	private final ProxyInitializer initializer;

	private final Serializee serializee = new Serializee();
	private final Router router;
	private final Proxifier proxifier;
	private Gson gson;

	public HypermediaSerializer(Writer writer, ProxyInitializer initializer,
			Router router, Proxifier proxifier) {
		this.writer = writer;
		this.initializer = initializer;
		this.router = router;
		this.proxifier = proxifier;
		this.gson = new GsonBuilder().create();
	}

	public Serializer exclude(String... names) {
		serializee.excludeAll(names);
		return this;
	}

	private void preConfigure(Object obj, String alias) {
		checkNotNull(obj, "You can't serialize null objects");
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
		from(object, null);
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
		JsonElement element = gson.toJsonTree(serializee.getRoot());
		addHypermediaLinks(element, serializee.getRoot());
		write(gson.toJson(element));

	}

	private void addHypermediaLinks(JsonElement element, Object root) {
		if (root instanceof Collection) {
			List collection = new ArrayList((Collection) root);
			JsonArray array = (JsonArray) element;
			for (int i = 0; i < collection.size(); i++) {
				addHypermediaLinks(array.get(i), collection.get(i));
			}
		} else {
			JsonObject resource = (JsonObject) element;
			resource.add("links", hypermediaRelation(root));
		}
	}

	private JsonObject hypermediaRelation(Object obj) {
		JsonObject linkRelation = new JsonObject();

		if (obj instanceof HypermediaResource) {

			HypermediaResource resource = (HypermediaResource) obj;
			RelationBuilder builder = new HypermediaRelationBuilder(router,
					proxifier);

			resource.configureRelations(builder);

			if (!builder.getRelations().isEmpty()) {
				for (Relation t : builder.getRelations()) {
					UrlAndHttpMethodRelation h = (UrlAndHttpMethodRelation) t;
					JsonElement link = gson.toJsonTree(new HypermediaLink(h));
					linkRelation.add(h.getName(), link);
				}
			}
		}

		return linkRelation;
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
