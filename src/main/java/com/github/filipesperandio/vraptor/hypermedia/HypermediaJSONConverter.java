package com.github.filipesperandio.vraptor.hypermedia;

import java.util.Collection;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.restfulie.Restfulie;
import br.com.caelum.vraptor.restfulie.hypermedia.ConfigurableHypermediaResource;
import br.com.caelum.vraptor.restfulie.hypermedia.HypermediaResource;
import br.com.caelum.vraptor.restfulie.relation.Relation;
import br.com.caelum.vraptor.restfulie.relation.RelationBuilder;
import br.com.caelum.vraptor.restfulie.serialization.LinkConverterJSON;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class HypermediaJSONConverter extends LinkConverterJSON {

	private final Converter base;
	private final Router router;
	private final Proxifier proxifier;

	public HypermediaJSONConverter(Converter base, Restfulie restfulie,
			Configuration config, Router router, Proxifier proxifier) {
		super(base, restfulie, config);
		this.base = base;
		this.router = router;
		this.proxifier = proxifier;
	}

	public void marshal(Object root, HierarchicalStreamWriter writer,
			MarshallingContext context) {

		if (root instanceof ConfigurableHypermediaResource) {
			context.convertAnother(((ConfigurableHypermediaResource) root)
					.getModel());
		} else {
			base.marshal(root, writer, context);
		}

		HypermediaResource resource = (HypermediaResource) root;
		RelationBuilder builder = new HypermediaRelationBuilder(router,
				proxifier);
		resource.configureRelations(builder);

		if (!builder.getRelations().isEmpty()) {
			ExtendedHierarchicalStreamWriterHelper.startNode(writer, "links",
					Object.class);
			for (Relation t : builder.getRelations()) {
				UrlAndHttpMethodRelation h = (UrlAndHttpMethodRelation) t;
				ExtendedHierarchicalStreamWriterHelper.startNode(writer,
						h.getName(), String.class);
				HypermediaLink hypermediaTransaction = new HypermediaLink(
						h.getMethod(), h.getUri());
				context.convertAnother(hypermediaTransaction);
				writer.endNode();
			}
			writer.endNode();
		}
	}
}
