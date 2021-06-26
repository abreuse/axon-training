package io.axoniq.training.labs.giftcard.saga;

import io.axoniq.training.labs.booking.coreapi.BookingPlacedEvent;
import io.axoniq.training.labs.booking.coreapi.BookingRejectedEvent;
import io.axoniq.training.labs.booking.coreapi.ConfirmBookingCommand;
import io.axoniq.training.labs.booking.coreapi.RejectBookingCommand;
import io.axoniq.training.labs.giftcard.coreapi.CardRedeemedEvent;
import io.axoniq.training.labs.giftcard.coreapi.RedeemCardCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class BookingSaga {

    public static final String PARTIAL_PAYMENT_DEADLINE = "partialPaymentDeadline";
    private transient CommandGateway commandGateway;
    private transient DeadlineManager deadlineManager;

    private String bookingId;
    private String cardId;
    private boolean partialPayment;
    private String paymentDeadlineId;

    @SagaEventHandler(associationProperty = "bookingId")
    @StartSaga
    public void handle(BookingPlacedEvent event) {
        this.bookingId = event.getBookingId();
        this.cardId = event.getCardId();
        this.partialPayment = event.isPartialPayment();

        SagaLifecycle.associateWith("transactionId", event.getBookingId());

        if (event.isPartialPayment()) {
            this.paymentDeadlineId = deadlineManager.schedule(Duration.ofDays(7),
                    PARTIAL_PAYMENT_DEADLINE,
                    new PartialPaymentDeadlinePayload(event.getBookingId(), event.getCardId(),
                            "Booking is rejected because partial payment has not been paid in 7 days"));
        }

        commandGateway.send(new RedeemCardCommand(event.getCardId(), event.getBookingId(), event.getGiftCardAmount()))
        .exceptionally((throwable -> {
            commandGateway.send(new RejectBookingCommand(event.getBookingId(), event.getCardId(), "error occured"));
            return null;
        }));
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CardRedeemedEvent event) {
        commandGateway.send(new ConfirmBookingCommand(event.getTransactionId(), event.getCardId(), event.getAmount()));

        if(!partialPayment) {
            SagaLifecycle.end();
        }
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void handle(BookingRejectedEvent event) {
        deadlineManager.cancelSchedule(paymentDeadlineId, PARTIAL_PAYMENT_DEADLINE);
        SagaLifecycle.end();
    }

    @DeadlineHandler(deadlineName = "partialPaymentDeadline")
    public void on(PartialPaymentDeadlinePayload payload) {
        commandGateway.send(new RejectBookingCommand(payload.getBookingId(), payload.getCardId(), payload.getReason()));
    }

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    public void setDeadlineManager(DeadlineManager deadlineManager) {
        this.deadlineManager = deadlineManager;
    }
}
