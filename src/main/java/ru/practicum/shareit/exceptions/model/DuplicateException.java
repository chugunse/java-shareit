package ru.practicum.shareit.exceptions.model;

public class DuplicateException extends IllegalArgumentException {
    public DuplicateException(String message) {
        super(message);
    }
}
