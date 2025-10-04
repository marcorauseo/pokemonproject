package com.example.pokedex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PokemonInfo {

    private final String name;
    private final String description;
    private final String habitat;
    @JsonProperty("isLegendary")
    private final boolean isLegendary;

    public PokemonInfo(String name, String description, String habitat, boolean isLegendary) {
        this.name = name;
        this.description = description;
        this.habitat = habitat;
        this.isLegendary = isLegendary;
    }

    public static PokemonInfo fromSpecies(PokeApiSpeciesResponse species) {
        String description = species.getFirstEnglishFlavorText();
        return new PokemonInfo(
                species.name(),
                description,
                species.habitatName(),
                species.isLegendary()
        );
    }

    public PokemonInfo withDescription(String newDesc) {
        return new PokemonInfo(this.name, newDesc, this.habitat, this.isLegendary);
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getHabitat() { return habitat; }
    public boolean isLegendary() { return isLegendary; }
}
