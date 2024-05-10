package com.example.films.movie;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "movies")
@Entity(name = "movies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Movie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private Integer release_year;
    private String description;
    String image;

    public Movie(MovieRequestDTO data){
        this.title = data.title();
        this.release_year = data.release_year();
        this.description = data.description();
        this.image = data.image();
    }
}
