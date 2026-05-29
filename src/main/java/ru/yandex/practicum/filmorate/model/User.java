package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

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
    @PastOrPresent(message = "Birthday cannot be in the future")
    private LocalDate birthday;
}
