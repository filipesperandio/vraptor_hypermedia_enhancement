package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.gson.JsonPrimitive;

@SuppressWarnings("all")
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
		try {
			addHypermediaLinks(element, serializee.getRoot());
		} catch (Exception e) {
			e.printStackTrace();
		}
		write(gson.toJson(element));

	}

	private void addHypermediaLinks(JsonElement element, Object root)
			throws IllegalArgumentException, IllegalAccessException {
		if (root instanceof Collection) {
			addHypermediaLinksForCollections(element.getAsJsonArray(), root);
		} else {
			if (!(element instanceof JsonPrimitive)) {
				JsonObject jsonObj = element.getAsJsonObject();
				for (Field field : findFields(root)) {
					field.setAccessible(true);
					Object fieldObj = field.get(root);
					if (fieldObj != null) {
						addLinksToFields(jsonObj, field, fieldObj);
					}
				}
				addLinksNode(jsonObj, root);
			}
		}
	}

	private void addLinksToFields(JsonObject resource, Field field,
			Object fieldObj) throws IllegalAccessException {
		Class<?> type = field.getType();
		String fieldName = field.getName();
		if (isHypermedia(type)) {
			addLinksNode((JsonObject) resource.get(fieldName), fieldObj);
		} else if (isCollection(type)) {
			addHypermediaLinksForCollections(
					(JsonArray) resource.get(fieldName), fieldObj);
		}
	}

	private boolean isHypermedia(Class<?> type) {
		return HypermediaResource.class.isAssignableFrom(type);
	}

	private boolean isCollection(Class<?> type) {
		return Collection.class.isAssignableFrom(type);
	}

	private void addHypermediaLinksForCollections(JsonArray array, Object root)
			throws IllegalAccessException {
		List<Object> collection = new ArrayList<Object>((Collection) root);
		for (int i = 0; i < collection.size(); i++) {
			addHypermediaLinks(array.get(i), collection.get(i));
		}
	}

	private void addLinksNode(JsonObject resource, Object fieldObj) {
		resource.add("links", hypermediaRelation(fieldObj));
	}

	private List<Field> findFields(Object root) {
		List<Field> fields = new ArrayList<Field>();
		Class<? extends Object> clazz = root.getClass();
		fields.addAll(extractFieldsFrom(clazz));

		return fields;
	}

	private List<Field> extractFieldsFrom(Class<? extends Object> clazz) {
		List<Field> fields = new ArrayList<Field>(Arrays.asList(clazz
				.getDeclaredFields()));
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null)
			fields.addAll(extractFieldsFrom(superclass));
		return fields;
	}

	private JsonObject hypermediaRelation(Object obj) {
		JsonObject linkRelation = new JsonObject();

		if (obj instanceof HypermediaResource) {

			HypermediaResource resource = (HypermediaResource) obj;
			RelationBuilder builder = new HypermediaRelationBuilder(router,
					proxifier);

			resource.configureRelations(builder);

			if (!builder.getRelations().isEmpty()) {
				for (Relation relation : builder.getRelations()) {
					UrlAndHttpMethodRelation hypermediaRelation = (UrlAndHttpMethodRelation) relation;
					JsonElement link = gson.toJsonTree(new HypermediaLink(
							hypermediaRelation));
					linkRelation.add(hypermediaRelation.getName(), link);
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
