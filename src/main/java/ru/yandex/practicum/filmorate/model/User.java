package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must contain @")
    private String email;
    @NotBlank(message = "Login cannot be blank")
    @Pattern(regexp = "\\S+", message = "Login cannot contain spaces")
    private String login;
    private String name;
    @NotNull(message = "Birthday cannot be null")
    @PastOrPresent(message = "Birthday cannot be in the future")
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
}
