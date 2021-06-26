package io.axoniq.training.labs.booking.coreapi;

import java.util.Objects;

import org.axonframework.commandhandling.RoutingKey;

public class BookingRejectedEvent {

    private final String bookingId;
    private final String reason;

    public BookingRejectedEvent(String bookingId, String reason) {
        this.bookingId = bookingId;
        this.reason = reason;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookingRejectedEvent that = (BookingRejectedEvent) o;
        return Objects.equals(bookingId, that.bookingId) &&
                Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, reason);
    }

    @Override
    public String toString() {
        return "BookingRejectedEvent{" +
                "bookingId='" + bookingId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
