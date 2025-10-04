
package com.example.pokedex.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PokeApiSpeciesResponseTest {

    @Test
    void testGetFirstEnglishFlavorTextPrefersEnglish() {
        PokeApiSpeciesResponse.NamedResource en = new PokeApiSpeciesResponse.NamedResource("en");
        PokeApiSpeciesResponse.FlavorTextEntry entry = new PokeApiSpeciesResponse.FlavorTextEntry("Hello\nWorld", en);
        PokeApiSpeciesResponse response = new PokeApiSpeciesResponse("pikachu", false, new PokeApiSpeciesResponse.Habitat("forest"), List.of(entry));
        assertEquals("Hello World", response.getFirstEnglishFlavorText());
    }

    @Test
    void testSanitizeRemovesNewlines() {
        PokeApiSpeciesResponse.NamedResource fr = new PokeApiSpeciesResponse.NamedResource("fr");
        PokeApiSpeciesResponse.FlavorTextEntry entry = new PokeApiSpeciesResponse.FlavorTextEntry("Bonjour\fLe Monde", fr);
        PokeApiSpeciesResponse response = new PokeApiSpeciesResponse("pikachu", false, null, List.of(entry));
        assertEquals("Bonjour Le Monde", response.getFirstEnglishFlavorText());
    }
}
