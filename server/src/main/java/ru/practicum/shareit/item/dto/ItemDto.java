package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.ForItemBookingDto;

import java.util.List;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private ForItemBookingDto lastBooking;
    private ForItemBookingDto nextBooking;
    private List<CommentDto> comments;
    private Long requestId;
}