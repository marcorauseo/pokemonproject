package com.example.pokedex.service;

import com.example.pokedex.client.FunTranslationsClient;
import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.model.PokemonInfo;
import com.example.pokedex.util.Either;
import com.example.pokedex.util.ErrorResponse;

public class PokemonService {

    private final PokeApiClient pokeApi;
    private final FunTranslationsClient fun;

    public PokemonService(PokeApiClient pokeApi, FunTranslationsClient fun) {
        this.pokeApi = pokeApi;
        this.fun = fun;
    }

    public Either<ErrorResponse, PokemonInfo> getBasicInfo(String nameRaw) {
        String name = sanitizeName(nameRaw);
        return pokeApi.fetchSpecies(name).mapLeft(err ->
            new ErrorResponse(err.status(), err.message())
        ).mapRight(species ->
            PokemonInfo.fromSpecies(species)
        );
    }

    public Either<ErrorResponse, PokemonInfo> getTranslatedInfo(String nameRaw) {
        String name = sanitizeName(nameRaw);
        var speciesEither = pokeApi.fetchSpecies(name).mapLeft(err -> new ErrorResponse(err.status(), err.message()));
        if (speciesEither.isLeft()) return Either.left(speciesEither.getLeft());
        var species = speciesEither.getRight();

        var info = PokemonInfo.fromSpecies(species);
        // Decide translator
        boolean useYoda = info.isLegendary() || "cave".equalsIgnoreCase(info.getHabitat());
        var desc = info.getDescription();
        var translatedEither = useYoda ? fun.yoda(desc) : fun.shakespeare(desc);

        if (translatedEither.isRight()) {
            info = info.withDescription(translatedEither.getRight());
        }
        // on any translation failure, fallback to original
        return Either.right(info);
    }

    private String sanitizeName(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }
}
