package com.github.filipesperandio.vraptor.hypermedia;

import java.lang.reflect.Method;

import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.proxy.MethodInvocation;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.proxy.SuperMethod;
import br.com.caelum.vraptor.resource.HttpMethod;
import br.com.caelum.vraptor.restfulie.relation.DefaultRelationBuilder;

public class HypermediaRelationBuilder extends DefaultRelationBuilder {

	final Proxifier proxifier;
	final Router router;

	public HypermediaRelationBuilder(Router router, Proxifier proxifier) {
		super(router, proxifier);
		this.router = router;
		this.proxifier = proxifier;
	}

	public WithName relation(String name) {
		return new HypermediaName(name);
	}

	private class HypermediaName implements WithName {
		private final String name;

		public HypermediaName(String name) {
			this.name = name;
		}

		public void at(String uri) {
			add(new UrlAndHttpMethodRelation(name, uri, HttpMethod.GET));
		}

		public <T> T uses(final Class<T> controller) {
			return proxifier.proxify(controller, new MethodInvocation<T>() {
				public Object intercept(T proxy, Method javaMethod,
						Object[] args, SuperMethod superMethod) {
					String url = router.urlFor(controller, javaMethod, args);
					HttpMethod httpMethod = extractHttpMethod(javaMethod);
					add(new UrlAndHttpMethodRelation(name, url, httpMethod));
					return null;
				}

				private HttpMethod extractHttpMethod(Method method) {
					for (HttpMethod m : HttpMethod.values()) {
						if (method.isAnnotationPresent(m.getAnnotation())) {
							return m;
						}
					}
					return HttpMethod.GET;
				}
			});
		}
	}
}
