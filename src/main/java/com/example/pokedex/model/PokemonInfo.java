package com.example.pokedex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PokemonInfo {

    private final String name;
    private final String description;
    private final String habitat;

    @JsonIgnore // evita la doppia proprietà nel JSON
    private final boolean legendary;

    public PokemonInfo(String name, String description, String habitat, boolean legendary) {
        this.name = name;
        this.description = description;
        this.habitat = habitat;
        this.legendary = legendary;
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
        return new PokemonInfo(this.name, newDesc, this.habitat, this.legendary);
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getHabitat() { return habitat; }


    @JsonProperty("legendary")
    public boolean isLegendary() { return legendary; }
}
