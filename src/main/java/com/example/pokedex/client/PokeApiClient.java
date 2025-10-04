package com.example.pokedex.client;

import com.example.pokedex.model.PokeApiSpeciesResponse;
import com.example.pokedex.util.Either;
import com.example.pokedex.util.HttpError;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;

public class PokeApiClient {

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public PokeApiClient(OkHttpClient http, ObjectMapper mapper, String baseUrl) {
        this.http = http;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

    public static PokeApiClient defaultClient() {
        var http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(10))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        var mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new PokeApiClient(http, mapper, "https://pokeapi.co/api/v2");
    }

    public Either<HttpError, PokeApiSpeciesResponse> fetchSpecies(String name) {
        HttpUrl url = HttpUrl.parse(baseUrl + "/pokemon-species/" + name).newBuilder().build();
        Request req = new Request.Builder().url(url).get().build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                int code = res.code();
                if (code == 404) {
                    return Either.left(new HttpError(404, "Pokemon not found: " + name));
                }
                return Either.left(new HttpError(code, "PokeAPI error: HTTP " + code));
            }
            var body = res.body();
            if (body == null) return Either.left(new HttpError(502, "Empty response body"));
            var species = mapper.readValue(body.byteStream(), PokeApiSpeciesResponse.class);
            return Either.right(species);
        } catch (IOException e) {
            return Either.left(new HttpError(502, "PokeAPI I/O error: " + e.getMessage()));
        }
    }
}
