package com.example.pokedex.service;

import com.example.pokedex.client.FunTranslationsClient;
import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.model.PokeApiSpeciesResponse;
import com.example.pokedex.model.PokemonInfo;
import com.example.pokedex.util.Either;
import com.example.pokedex.util.HttpError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PokemonServiceTest {

    static class FakePokeApiClient extends PokeApiClient {
        private final Either<HttpError, PokeApiSpeciesResponse> result;

        public FakePokeApiClient(Either<HttpError, PokeApiSpeciesResponse> result) {
            super(null, null, null); // non usato
            this.result = result;
        }

        @Override
        public Either<HttpError, PokeApiSpeciesResponse> fetchSpecies(String name) {
            return result;
        }
    }

    static class FakeFunTranslationsClient extends FunTranslationsClient {
        private final Either<HttpError, String> yodaResult;
        private final Either<HttpError, String> shakespeareResult;

        public FakeFunTranslationsClient(Either<HttpError, String> yodaResult,
                                         Either<HttpError, String> shakespeareResult) {
            super(null, null, null, null); // non usato
            this.yodaResult = yodaResult;
            this.shakespeareResult = shakespeareResult;
        }

        @Override
        public Either<HttpError, String> yoda(String text) {
            return yodaResult;
        }

        @Override
        public Either<HttpError, String> shakespeare(String text) {
            return shakespeareResult;
        }
    }

    @Test
    void basicInfo_ok() {
        var species = new PokeApiSpeciesResponse(
                "mewtwo",
                true,
                new PokeApiSpeciesResponse.Habitat("rare"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("A test\\ntext",
                        new PokeApiSpeciesResponse.NamedResource("en")))
        );
        var pokeApi = new FakePokeApiClient(Either.right(species));
        var fun = new FakeFunTranslationsClient(null, null);
        var service = new PokemonService(pokeApi, fun);

        var result = service.getBasicInfo("mewtwo");
        assertTrue(result.isRight());
        PokemonInfo info = result.getRight();
        assertEquals("mewtwo", info.getName());
        assertTrue(info.isLegendary());
        assertEquals("rare", info.getHabitat());
        assertEquals("A test\\ntext", info.getDescription());
    }

    @Test
    void translated_yoda_for_legendary() {
        var species = new PokeApiSpeciesResponse(
                "mewtwo",
                true,
                new PokeApiSpeciesResponse.Habitat("rare"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("Created by a scientist.",
                        new PokeApiSpeciesResponse.NamedResource("en")))
        );
        var pokeApi = new FakePokeApiClient(Either.right(species));
        var fun = new FakeFunTranslationsClient(Either.right("Created by a scientist, it was."), null);
        var service = new PokemonService(pokeApi, fun);

        var result = service.getTranslatedInfo("mewtwo");
        assertTrue(result.isRight());
        assertEquals("Created by a scientist, it was.", result.getRight().getDescription());
    }

    @Test
    void translated_shakespeare_for_non_legendary_non_cave() {
        var species = new PokeApiSpeciesResponse(
                "pikachu",
                false,
                new PokeApiSpeciesResponse.Habitat("forest"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("Electric mouse.",
                        new PokeApiSpeciesResponse.NamedResource("en")))
        );
        var pokeApi = new FakePokeApiClient(Either.right(species));
        var fun = new FakeFunTranslationsClient(null, Either.right("Electric mouse, verily."));
        var service = new PokemonService(pokeApi, fun);

        var result = service.getTranslatedInfo("pikachu");
        assertTrue(result.isRight());
        assertEquals("Electric mouse, verily.", result.getRight().getDescription());
    }

    @Test
    void translated_fallback_on_translation_error() {
        var species = new PokeApiSpeciesResponse(
                "pikachu",
                false,
                new PokeApiSpeciesResponse.Habitat("forest"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("Electric mouse.",
                        new PokeApiSpeciesResponse.NamedResource("en")))
        );
        var pokeApi = new FakePokeApiClient(Either.right(species));
        var fun = new FakeFunTranslationsClient(null, Either.left(new HttpError(429, "Rate limited")));
        var service = new PokemonService(pokeApi, fun);

        var result = service.getTranslatedInfo("pikachu");
        assertTrue(result.isRight());
        assertEquals("Electric mouse.", result.getRight().getDescription());
    }

    @Test
    void not_found_propagates() {
        var pokeApi = new FakePokeApiClient(Either.left(new HttpError(404, "Pokemon not found: missingno")));
        var fun = new FakeFunTranslationsClient(null, null);
        var service = new PokemonService(pokeApi, fun);

        var result = service.getBasicInfo("missingno");
        assertTrue(result.isLeft());
        assertEquals(404, result.getLeft().status());
    }
}
