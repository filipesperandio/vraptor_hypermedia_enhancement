package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import br.com.caelum.vraptor.restfulie.hypermedia.HypermediaResource;
import br.com.caelum.vraptor.restfulie.relation.RelationBuilder;
import br.com.caelum.vraptor.restfulie.relation.RelationBuilder.WithName;

public class Entity implements HypermediaResource{
		private Long id;
		
		public void setId(Long id) {
			this.id = id;
		}

		public Long getId() {
			return id;
		}

		@Override
		public void configureRelations(RelationBuilder builder) {
			WithName relation = builder.relation("location");
			Controller uses = relation.uses(Controller.class);
			uses.load(this);
		}
		

}
