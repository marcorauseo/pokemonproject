package com.example.pokedex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiSpeciesResponse(
        String name,
        @JsonProperty("is_legendary") boolean isLegendary,
        Habitat habitat,
        @JsonProperty("flavor_text_entries") List<FlavorTextEntry> flavorTextEntries
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Habitat(String name) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NamedResource(String name) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FlavorTextEntry(
            @JsonProperty("flavor_text") String flavorText,
            NamedResource language
    ) {}

    public String habitatName() {
        return habitat != null ? habitat.name() : null;
    }

    public String getFirstEnglishFlavorText() {
        if (flavorTextEntries == null) return "";
        for (FlavorTextEntry e : flavorTextEntries) {
            if (e != null && e.language() != null && Objects.equals(e.language().name(), "en")) {
                return sanitize(e.flavorText());
            }
        }
        // fallback to first available
        if (!flavorTextEntries.isEmpty() && flavorTextEntries.get(0) != null) {
            return sanitize(flavorTextEntries.get(0).flavorText());
        }
        return "";
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        // PokeAPI includes newlines and form-feeds in flavor_text; replace with spaces
        return s.replace("\n", " ").replace("\f", " ").trim();
    }
}
