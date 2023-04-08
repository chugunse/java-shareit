package ru.practicum.shareit.booking.dto;

import java.util.Arrays;
import java.util.Optional;

public enum BookingState {
	// Все
	ALL,
	// Текущие
	CURRENT,
	// Будущие
	FUTURE,
	// Завершенные
	PAST,
	// Отклоненные
	REJECTED,
	// Ожидающие подтверждения
	WAITING;

	public static Optional<BookingState> from(String stringState) {
		return Arrays.stream(values()).filter(i -> i.toString().equals(stringState)).findAny();
	}
}
