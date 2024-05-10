package com.example.films.movie;

public record MovieResponseDTO(Integer id, String title, Integer release_year, String description, String image) {
    public MovieResponseDTO(Movie movie){
        this(movie.getId(), movie.getTitle(), movie.getRelease_year(), movie.getDescription(), movie.getImage());
    }
}
