package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.ForItemBookingDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(message = "поле 'name' не может быть пустым")
    private String name;
    @NotBlank(message = "поле 'description' не может быть пустым")
    private String description;
    @NotNull
    private Boolean available;
    private ForItemBookingDto lastBooking;
    private ForItemBookingDto nextBooking;
    private List<CommentDto> comments;
}