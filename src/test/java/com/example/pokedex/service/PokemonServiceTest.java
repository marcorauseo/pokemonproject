package com.example.pokedex.service;

import com.example.pokedex.client.FunTranslationsClient;
import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.model.PokeApiSpeciesResponse;
import com.example.pokedex.model.PokemonInfo;
import com.example.pokedex.util.Either;
import com.example.pokedex.util.HttpError;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PokemonServiceTest {

    @Test
    void basicInfo_ok() {
        var pokeApi = Mockito.mock(PokeApiClient.class);
        var fun = Mockito.mock(FunTranslationsClient.class);
        var service = new PokemonService(pokeApi, fun);

        var species = new PokeApiSpeciesResponse(
                "mewtwo",
                true,
                new PokeApiSpeciesResponse.Habitat("rare"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("A test\\ntext", new PokeApiSpeciesResponse.NamedResource("en")))
        );
        when(pokeApi.fetchSpecies("mewtwo")).thenReturn(Either.right(species));

        var result = service.getBasicInfo("mewtwo");
        assertTrue(result.isRight());
        PokemonInfo info = result.getRight();
        assertEquals("mewtwo", info.getName());
        assertTrue(info.isLegendary());
        assertEquals("rare", info.getHabitat());
        assertEquals("A test text", info.getDescription());
    }

    @Test
    void translated_yoda_for_legendary() {
        var pokeApi = Mockito.mock(PokeApiClient.class);
        var fun = Mockito.mock(FunTranslationsClient.class);
        var service = new PokemonService(pokeApi, fun);

        var species = new PokeApiSpeciesResponse(
                "mewtwo",
                true,
                new PokeApiSpeciesResponse.Habitat("rare"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("Created by a scientist.", new PokeApiSpeciesResponse.NamedResource("en")))
        );
        when(pokeApi.fetchSpecies("mewtwo")).thenReturn(Either.right(species));
        when(fun.yoda(anyString())).thenReturn(Either.right("Created by a scientist, it was."));

        var result = service.getTranslatedInfo("mewtwo");
        assertTrue(result.isRight());
        assertEquals("Created by a scientist, it was.", result.getRight().getDescription());
    }

    @Test
    void translated_shakespeare_for_non_legendary_non_cave() {
        var pokeApi = Mockito.mock(PokeApiClient.class);
        var fun = Mockito.mock(FunTranslationsClient.class);
        var service = new PokemonService(pokeApi, fun);

        var species = new PokeApiSpeciesResponse(
                "pikachu",
                false,
                new PokeApiSpeciesResponse.Habitat("forest"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("Electric mouse.", new PokeApiSpeciesResponse.NamedResource("en")))
        );
        when(pokeApi.fetchSpecies("pikachu")).thenReturn(Either.right(species));
        when(fun.shakespeare(anyString())).thenReturn(Either.right("Electric mouse, verily."));

        var result = service.getTranslatedInfo("pikachu");
        assertTrue(result.isRight());
        assertEquals("Electric mouse, verily.", result.getRight().getDescription());
    }

    @Test
    void translated_fallback_on_translation_error() {
        var pokeApi = Mockito.mock(PokeApiClient.class);
        var fun = Mockito.mock(FunTranslationsClient.class);
        var service = new PokemonService(pokeApi, fun);

        var species = new PokeApiSpeciesResponse(
                "pikachu",
                false,
                new PokeApiSpeciesResponse.Habitat("forest"),
                List.of(new PokeApiSpeciesResponse.FlavorTextEntry("Electric mouse.", new PokeApiSpeciesResponse.NamedResource("en")))
        );
        when(pokeApi.fetchSpecies("pikachu")).thenReturn(Either.right(species));
        when(fun.shakespeare(anyString())).thenReturn(Either.left(new HttpError(429, "Rate limited")));

        var result = service.getTranslatedInfo("pikachu");
        assertTrue(result.isRight());
        assertEquals("Electric mouse.", result.getRight().getDescription());
    }

    @Test
    void not_found_propagates() {
        var pokeApi = Mockito.mock(PokeApiClient.class);
        var fun = Mockito.mock(FunTranslationsClient.class);
        var service = new PokemonService(pokeApi, fun);

        when(pokeApi.fetchSpecies("missingno")).thenReturn(Either.left(new HttpError(404, "Pokemon not found: missingno")));

        var result = service.getBasicInfo("missingno");
        assertTrue(result.isLeft());
        assertEquals(404, result.getLeft().status());
    }
}
