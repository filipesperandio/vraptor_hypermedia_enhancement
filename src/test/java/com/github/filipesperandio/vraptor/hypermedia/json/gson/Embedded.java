package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import javax.persistence.Embeddable;

import br.com.caelum.vraptor.restfulie.relation.RelationBuilder;

@javax.persistence.Entity
@Embeddable
public class Embedded extends Entity {

	@Override
	public void configureRelations(RelationBuilder builder) {
		builder.relation("location").uses(Controller.class).load(this);
	}

}
