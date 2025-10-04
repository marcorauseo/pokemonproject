
package com.example.pokedex.client;

import com.example.pokedex.model.PokeApiSpeciesResponse;
import com.example.pokedex.util.Either;
import com.example.pokedex.util.HttpError;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PokeApiClientTest {
    private MockWebServer server;
    private PokeApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        OkHttpClient http = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(2)).build();
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        client = new PokeApiClient(http, mapper, server.url("/api/v2").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void testSuccessResponse() {
        String body = "{\"name\":\"pikachu\",\"is_legendary\":false,\"flavor_text_entries\":[{\"flavor_text\":\"hello\",\"language\":{\"name\":\"en\"}}]}";
        server.enqueue(new MockResponse().setResponseCode(200).setBody(body));
        Either<HttpError,PokeApiSpeciesResponse> res = client.fetchSpecies("pikachu");
        assertTrue(res.isRight());
        assertEquals("pikachu", res.getRight().name());
    }

    @Test
    void test404Response() {
        server.enqueue(new MockResponse().setResponseCode(404));
        Either<HttpError,PokeApiSpeciesResponse> res = client.fetchSpecies("missingno");
        assertTrue(res.isLeft());
        assertEquals(404, res.getLeft().status());
    }
}
