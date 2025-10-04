package com.example.pokedex.util;

public record ErrorResponse(int status, String message) {
    public int status() { return status; }
    public String message() { return message; }
}
