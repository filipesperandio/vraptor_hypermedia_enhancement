package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import java.util.List;

import javax.persistence.ElementCollection;

import br.com.caelum.vraptor.restfulie.relation.RelationBuilder;

@javax.persistence.Entity
public class EntityModel extends Entity {

	@ElementCollection
	private List<Embedded> embeddeds;

	@Override
	public void configureRelations(RelationBuilder builder) {
		builder.relation("location").uses(Controller.class).load(this);
	}

	public List<Embedded> getEmbeddeds() {
		return embeddeds;
	}

	public void setEmbeddeds(List<Embedded> embeddeds) {
		this.embeddeds = embeddeds;
	}

}
