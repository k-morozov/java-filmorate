package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private long id;
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @Size(max = 200, message = "Description must be 200 characters or less")
    private String description;
    @NotNull(message = "Release date cannot be null")
    private LocalDate releaseDate;
    @Positive(message = "Duration must be a positive number")
    private int duration;
}
