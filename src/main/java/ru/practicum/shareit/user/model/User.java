package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Long id;
    @NotBlank(message = "поле 'name' не может быть пустым")
    private String name;
    @NotBlank(message = "поле 'email' пустое")
    @Email(message = "поле 'email' неподходящий формат")
    private String email;
}
