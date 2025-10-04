package com.example.pokedex.controller;

import com.example.pokedex.model.PokemonInfo;
import com.example.pokedex.service.PokemonService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;

public class PokemonController implements EndpointGroup {

    private final PokemonService service;

    public PokemonController(PokemonService service) {
        this.service = service;
    }

    @Override
    public void addEndpoints() {
        get("/pokemon/{name}", this::getBasic);
        get("/pokemon/translated/{name}", this::getTranslated);
    }

    private void getBasic(Context ctx) {
        String name = ctx.pathParam("name");
        service.getBasicInfo(name).fold(
            err -> ctx.status(err.status()).json(err),
            info -> ctx.json(info)
        );
    }

    private void getTranslated(Context ctx) {
        String name = ctx.pathParam("name");
        service.getTranslatedInfo(name).fold(
            err -> ctx.status(err.status()).json(err),
            info -> ctx.json(info)
        );
    }
}
