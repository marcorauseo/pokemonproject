
package com.example.pokedex.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PokemonInfoTest {

    @Test
    void testWithDescriptionChangesDescription() {
        PokemonInfo info = new PokemonInfo("pikachu", "old", "forest", false);
        PokemonInfo updated = info.withDescription("new");
        assertEquals("new", updated.getDescription());
        assertEquals(info.getName(), updated.getName());
    }

    @Test
    void testJsonHasOnlyLegendary() throws Exception {
        PokemonInfo info = new PokemonInfo("squirtle", "desc", "waters-edge", false);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(info);
        assertTrue(json.contains("\"legendary\":false" ));

    }
}
