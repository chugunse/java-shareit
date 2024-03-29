package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    //меняем статус
    @Modifying
    @Query("UPDATE Booking b " +
            "SET b.status = :status  " +
            "WHERE b.id = :bookingId")
    void update(BookingStatus status, Long bookingId);

    //запросы по юзеру

    List<Booking> findAllByBooker_Id(long id, Pageable page);

    List<Booking> findAllByBooker_IdAndStatusIs(Long id,
                                                                BookingStatus status,
                                                                Pageable page);

    List<Booking> findAllByBooker_IdAndEndIsAfterAndStartIsBefore(Long id,
                                                                                  LocalDateTime end,
                                                                                  LocalDateTime start,
                                                                                  Pageable page);

    List<Booking> findAllByBooker_IdAndEndIsBefore(Long id,
                                                                   LocalDateTime time,
                                                                   Pageable page);

    List<Booking> findAllByBooker_IdAndStartIsAfter(Long id,
                                                                    LocalDateTime time,
                                                                    Pageable page);

    List<Booking> findAllByBooker_IdAndStartIsAfterAndStatusIs(Long bookerId,
                                                                               LocalDateTime start,
                                                                               BookingStatus status,
                                                                               Pageable page);

    //запросы по хозяину
    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.ownerId = :ownerId " +
            "order by b.start desc")
    List<Booking> findAllBookingsOwner(Long ownerId, Pageable page);

    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.ownerId = :ownerId " +
            "and :time between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findAllCurrentBookingsOwner(Long ownerId, LocalDateTime time, Pageable page);

    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.ownerId = :ownerId " +
            "and b.end < :time " +
            "order by b.start desc")
    List<Booking> findAllPastBookingsOwner(Long ownerId, LocalDateTime time, Pageable page);

    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.ownerId = :ownerId " +
            "and b.start > :time " +
            "order by b.start desc")
    List<Booking> findAllFutureBookingsOwner(Long ownerId, LocalDateTime time, Pageable page);

    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.ownerId = :ownerId " +
            "and b.start > :time and b.status = :status " +
            "order by b.start desc")
    List<Booking> findAllWaitingBookingsOwner(Long ownerId, LocalDateTime time, BookingStatus status, Pageable page);

    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.ownerId = :ownerId " +
            "and b.status = :status " +
            "order by b.start desc")
    List<Booking> findAllRejectedBookingsOwner(Long ownerId, BookingStatus status, Pageable page);

    // для item
    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.id = :itemId " +
            "order by b.start desc")
    List<Booking> findAllBookingsItem(Long itemId);

    //для comment

    List<Booking> findAllByItem_IdAndBooker_IdAndStatusIsAndEndIsBefore(Long itemId,
                                                                        Long bookerId,
                                                                        BookingStatus status,
                                                                        LocalDateTime time);
}
