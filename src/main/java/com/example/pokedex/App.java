package com.example.pokedex;

import com.example.pokedex.client.FunTranslationsClient;
import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.controller.PokemonController;
import com.example.pokedex.service.PokemonService;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "5000"));

        var pokeApiClient = PokeApiClient.defaultClient();
        var funTranslationsClient = FunTranslationsClient.defaultClient();
        var service = new PokemonService(pokeApiClient, funTranslationsClient);
        var controller = new PokemonController(service);

        Javalin app = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
            cfg.router.apiBuilder(controller);
        });

        app.start(port);
    }
}
