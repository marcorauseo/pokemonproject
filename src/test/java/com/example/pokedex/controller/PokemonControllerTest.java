package com.example.pokedex.controller;

import com.example.pokedex.model.PokemonInfo;
import com.example.pokedex.service.PokemonService;
import com.example.pokedex.util.Either;
import com.example.pokedex.util.ErrorResponse;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PokemonControllerTest {

    static class FakePokemonService extends PokemonService {
        private final Either<ErrorResponse, PokemonInfo> basicResult;

        public FakePokemonService(Either<ErrorResponse, PokemonInfo> basicResult) {
            super(null, null); // non usato
            this.basicResult = basicResult;
        }

        @Override
        public Either<ErrorResponse, PokemonInfo> getBasicInfo(String nameRaw) {
            return basicResult;
        }

        @Override
        public Either<ErrorResponse, PokemonInfo> getTranslatedInfo(String nameRaw) {
            return basicResult;
        }
    }

    @Test
    void testGetPokemonOk() {
        var info = new PokemonInfo("pikachu", "desc", "forest", false);
        var service = new FakePokemonService(Either.right(info));
        var controller = new PokemonController(service);

        Javalin app = Javalin.create(cfg -> cfg.router.apiBuilder(controller));

        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/pokemon/pikachu");
            assertEquals(200, res.code());
            String body = res.body().string();
            assertTrue(body.contains("pikachu"));
            assertTrue(body.contains("legendary"));
        });
    }

    @Test
    void testGetPokemonNotFound() {
        var service = new FakePokemonService(Either.left(new ErrorResponse(404, "Not found")));
        var controller = new PokemonController(service);

        Javalin app = Javalin.create(cfg -> cfg.router.apiBuilder(controller));

        JavalinTest.test(app, (server, client) -> {
            var res = client.get("/pokemon/missingno");
            assertEquals(404, res.code());
        });
    }
}
