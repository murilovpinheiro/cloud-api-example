package com.example.films.controller;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.example.films.movie.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("movie")
public class MovieController {

    @Autowired
    private MovieRepository repository;
    String tableName = "dynamo-cloud";

    AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(new EnvironmentVariableCredentialsProvider())
            .build();

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping
    public ResponseEntity<String> saveMovie(@RequestBody MovieRequestDTO data) {
        try {
            Movie movie = new Movie(data);
            repository.save(movie);

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("key", new AttributeValue(movie.getId().toString()));
            item.put("title", new AttributeValue(movie.getTitle()));
            item.put("action",  new AttributeValue("Insertion"));
            item.put("dateTime", new AttributeValue(LocalDateTime.now().toString()));


            // Executando a operação de inserção
            var response = dynamoDB.putItem(tableName, item);

            // Verificando se a inserção foi bem-sucedida
            if (response.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value()) {
                System.out.println("Item adicionado com sucesso!");
            } else {
                System.err.println("Erro ao adicionar o item.");
            }

            return ResponseEntity.ok("Movie saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save movie");
        }
    }


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
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("key", new AttributeValue(movieOptional.get().getId().toString()));
            item.put("title", new AttributeValue(movieOptional.get().getTitle()));
            item.put("action",  new AttributeValue("Deletion"));
            item.put("dateTime", new AttributeValue(LocalDateTime.now().toString()));

            // Executando a operação de inserção
            var response = dynamoDB.putItem(tableName, item);

            // Verificando se a inserção foi bem-sucedida
            if (response.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value()) {
                System.out.println("Item adicionado com sucesso!");
            } else {
                System.err.println("Erro ao adicionar o item.");
            }
            return ResponseEntity.ok("Movie deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
