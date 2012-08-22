package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.proxy.JavassistProxifier;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.proxy.ReflectionInstanceCreator;
import br.com.caelum.vraptor.serialization.HibernateProxyInitializer;
import br.com.caelum.vraptor.serialization.ProxyInitializer;

@SuppressWarnings("unchecked")
public class HypermediaSerializerTest {

	private Proxifier proxifier;
	private Router router;
	private ProxyInitializer initializer;
	private HypermediaSerializer serializer;
	private StringWriter writer;

	@Before
	public void setUp() {
		writer = new StringWriter();
		initializer = new HibernateProxyInitializer() {
			@Override
			public Class<?> getActualClass(Object obj) {
				return obj.getClass();
			}
		};
		proxifier = new JavassistProxifier(new ReflectionInstanceCreator());
		router = mock(Router.class);
		when(
				router.urlFor(any(Class.class), any(Method.class),
						any(Object.class))).thenReturn("/get/10");

		serializer = new HypermediaSerializer(writer, initializer, router,
				proxifier);
	}

	@Test
	public void shouldAddLinksToSerializedObject() {

		Entity entity = new Entity();
		entity.setId(10L);

		serializer.from(entity).serialize();

		String serilizedJson = writer.getBuffer().toString();

		String expected = "{\"id\":10,\"links\":{\"location\":{\"method\":\"GET\",\"url\":\"/get/10\"}}}";

		assertThat(serilizedJson, equalTo(expected));
	}

	@Test
	public void shouldAddLinksToSerializedObjectList() {
		
		Entity entity = new Entity();
		entity.setId(10L);
		
		serializer.from(Arrays.asList(entity)).serialize();
		
		String serilizedJson = writer.getBuffer().toString();
		
		String expected = "[{\"id\":10,\"links\":{\"location\":{\"method\":\"GET\",\"url\":\"/get/10\"}}}]";
		
		assertThat(serilizedJson, equalTo(expected));
	}
	
	@Test
	public void shouldSerializeEmptyArray() {
		serializer.from(new ArrayList()).serialize();
		assertThat(writer.getBuffer().toString(), equalTo("[]"));
	}
}
