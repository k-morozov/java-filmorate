package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {

    public static final int MAX_DESCRIPTION_LENGTH = 200;

    private long id;
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Description must be 200 characters or less")
    private String description;
    @NotNull(message = "Release date cannot be null")
    private LocalDate releaseDate;
    @Positive(message = "Duration must be a positive number")
    private int duration;
    @NotNull(message = "MPA rating cannot be null")
    private Mpa mpa;
    private Set<Genre> genres = new LinkedHashSet<>();
    private Set<Long> likes = new HashSet<>();
}
