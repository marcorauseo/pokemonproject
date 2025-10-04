package com.example.pokedex.client;

import com.example.pokedex.util.Either;
import com.example.pokedex.util.HttpError;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;

public class FunTranslationsClient {

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String apiKey;

    public FunTranslationsClient(OkHttpClient http, ObjectMapper mapper, String baseUrl, String apiKey) {
        this.http = http;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public static FunTranslationsClient defaultClient() {
        var http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(10))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        var mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var key = System.getenv("FUN_TRANSLATIONS_API_KEY");
        return new FunTranslationsClient(http, mapper, "https://api.funtranslations.com/translate", key);
    }

    public Either<HttpError, String> yoda(String text) {
        return translate("yoda", text);
    }
    public Either<HttpError, String> shakespeare(String text) {
        return translate("shakespeare", text);
    }

    private Either<HttpError, String> translate(String path, String text) {
        HttpUrl url = HttpUrl.parse(baseUrl + "/" + path + ".json").newBuilder().build();
        RequestBody rb = new FormBody.Builder().add("text", text).build();
        Request.Builder rbld = new Request.Builder().url(url).post(rb);
        if (apiKey != null && !apiKey.isBlank()) {
            rbld.addHeader("X-FunTranslations-Api-Secret", apiKey);
        }
        Request req = rbld.build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                // Treat any HTTP error (429 common) as translation failure
                return Either.left(new HttpError(res.code(), "FunTranslations error: HTTP " + res.code()));
            }
            var body = res.body();
            if (body == null) return Either.left(new HttpError(502, "Empty translation body"));
            var dto = mapper.readValue(body.byteStream(), TranslationResponse.class);
            if (dto.contents == null || dto.contents.translated == null) {
                return Either.left(new HttpError(502, "Malformed translation response"));
            }
            return Either.right(dto.contents.translated);
        } catch (IOException e) {
            return Either.left(new HttpError(502, "FunTranslations I/O error: " + e.getMessage()));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TranslationResponse {
        public Contents contents;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Contents {
        public String translated;
        @JsonProperty("text")
        public String original;
        public String translation;
    }
}
