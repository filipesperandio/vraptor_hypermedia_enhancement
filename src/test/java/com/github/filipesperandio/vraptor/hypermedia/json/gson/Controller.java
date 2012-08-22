package com.github.filipesperandio.vraptor.hypermedia.json.gson;

import br.com.caelum.vraptor.Get;

public class Controller {

	@Get("/get/{entity.id}")
	public void load(Entity entity) {
	}
}
