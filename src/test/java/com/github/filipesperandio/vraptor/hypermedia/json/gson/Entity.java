package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import br.com.caelum.vraptor.restfulie.hypermedia.HypermediaResource;

@MappedSuperclass
public abstract class Entity implements HypermediaResource, Serializable {
	@Id
	@GeneratedValue
	private Long id;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

}
