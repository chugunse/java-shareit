package ru.practicum.shareit.booking.exception;

import ru.practicum.shareit.exceptions.model.BadRequestException;

public class DoubleApproveException extends BadRequestException {
    public DoubleApproveException(String message) {
        super(message);
    }
}
