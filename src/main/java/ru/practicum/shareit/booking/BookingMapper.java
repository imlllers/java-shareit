package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItemId(booking.getItem().getId());
        bookingDto.setBookerId(booking.getBooker().getId());

        BookingDto.ItemInfo itemInfo = new BookingDto.ItemInfo();
        itemInfo.setId(booking.getItem().getId());
        itemInfo.setName(booking.getItem().getName());
        bookingDto.setItem(itemInfo);

        BookingDto.BookerInfo bookerInfo = new BookingDto.BookerInfo();
        bookerInfo.setId(booking.getBooker().getId());
        bookingDto.setBooker(bookerInfo);

        bookingDto.setStatus(booking.getStatus());
        return bookingDto;
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        return booking;
    }
}
