
package com.example.pokedex.client;

import com.example.pokedex.util.Either;
import com.example.pokedex.util.HttpError;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class FunTranslationsClientTest {
    private MockWebServer server;
    private FunTranslationsClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        OkHttpClient http = new OkHttpClient.Builder().callTimeout(Duration.ofSeconds(2)).build();
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        client = new FunTranslationsClient(http, mapper, server.url("/translate").toString(), null);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void testYodaSuccess() {
        String body = "{\"contents\":{\"translated\":\"Speak like Yoda, you will.\"}}";
        server.enqueue(new MockResponse().setResponseCode(200).setBody(body));
        Either<HttpError,String> res = client.yoda("Hello");
        assertTrue(res.isRight());
        assertEquals("Speak like Yoda, you will.", res.getRight());
    }

    @Test
    void testErrorResponse() {
        server.enqueue(new MockResponse().setResponseCode(429));
        Either<HttpError,String> res = client.shakespeare("Hello");
        assertTrue(res.isLeft());
        assertEquals(429, res.getLeft().status());
    }
}
