package com.example.films.controller;

import com.example.films.movie.Movie;
import com.example.films.movie.MovieRequestDTO;
import com.example.films.movie.MovieResponseDTO;
import com.example.films.movie.MovieRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("movie")
public class MovieController {

    @Autowired
    private MovieRepository repository;

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping
    public ResponseEntity<String> saveMovie(@RequestBody MovieRequestDTO data) {
        try {
            Movie movie = new Movie(data);
            repository.save(movie);
            return ResponseEntity.ok("Movie saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save movie");
        }
    }

    /* Old Save
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping
    public void saveMovie(@RequestBody MovieRequestDTO data){
        Movie foodData = new Movie(data);
        repository.save(foodData);
        return;
    }
    */


    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping
    public List<MovieResponseDTO> getAll(){
        List<MovieResponseDTO> movieList = repository.findAll().stream().map(MovieResponseDTO::new).toList();
        return movieList;
    }

    // Por enquanto o delete tá com o ID na URL, mas qualquer coisa posso mudar depois, só deixei o mais simples
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer id) {
        Optional<Movie> movieOptional = repository.findById(id);
        if (movieOptional.isPresent()) {
            repository.deleteById(id);
            return ResponseEntity.ok("Movie deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
