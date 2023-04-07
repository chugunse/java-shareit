package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "поле 'name' не может быть пустым")
    private String name;
    @NotBlank(message = "поле 'email' пустое")
    @Email(message = "поле 'email' неподходящий формат")
    private String email;
}
