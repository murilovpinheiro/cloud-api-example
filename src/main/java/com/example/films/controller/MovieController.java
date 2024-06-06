package com.example.films.controller;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.example.films.movie.*;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    @Autowired
    MinioClient minioClient;

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping
    public ResponseEntity<String> saveMovie(@RequestBody MovieRequestDTO data) {

            Movie movie = new Movie(data);
            repository.save(movie);

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("key", new AttributeValue(movie.getId().toString()));
            item.put("title", new AttributeValue(movie.getTitle()));
            item.put("action",  new AttributeValue("Insertion"));
            item.put("dateTime", new AttributeValue(LocalDateTime.now().toString()));

            System.out.println("chegou para salvar o filme " + movie.getTitle());

            // Executando a operação de inserção
            var response = dynamoDB.putItem(tableName, item);

            // Verificando se a inserção foi bem-sucedida
            if (response.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value()) {
                System.out.println("Item adicionado com sucesso!");
            } else {
                System.err.println("Erro ao adicionar o item.");
            }

            return ResponseEntity.ok("Movie saved successfully");

    }


    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping
    public List<MovieResponseDTO> getAll(){
        return repository.findAll().stream().map(MovieResponseDTO::new).toList();
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

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        System.out.println("tentando salvar a foto: " + file.getOriginalFilename());
        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket("movies")
                .object(file.getOriginalFilename())
                .stream(file.getInputStream(), file.getSize(), -1)
                        .build());
        String url =
                minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket("movies")
                                .object(file.getOriginalFilename())
                                .expiry(60 * 60 * 24)
                                .build());
        return ResponseEntity.ok(url);

    }
}
