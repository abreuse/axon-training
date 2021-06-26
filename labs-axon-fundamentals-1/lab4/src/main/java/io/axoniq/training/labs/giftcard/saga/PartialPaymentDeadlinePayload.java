package io.axoniq.training.labs.giftcard.saga;

public class PartialPaymentDeadlinePayload {
    private final String bookingId;
    private final String cardId;
    private final String reason;

    public PartialPaymentDeadlinePayload(String bookingId, String cardId, String reason) {
        this.bookingId = bookingId;
        this.cardId = cardId;
        this.reason = reason;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCardId() {
        return cardId;
    }

    public String getReason() {
        return reason;
    }
}
