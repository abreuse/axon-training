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
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.axoniq.training.labs.booking.coreapi.RejectBookingCommand.INSUFFICIENT_FUNDS;
import static io.axoniq.training.labs.booking.coreapi.RejectBookingCommand.PARTIAL_PAYMENT_DEADLINE_EXPIRED;

@Saga
@Profile("saga")
public class BookingSaga {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PAYMENT_DEADLINE = "paymentDeadline";

    private transient CommandGateway commandGateway;
    private transient DeadlineManager deadlineManager;

    private String bookingId;
    private String cardId;
    private boolean partialPayment;
    private String paymentDeadlineId;

    @StartSaga
    @SagaEventHandler(associationProperty = "bookingId")
    public void on(BookingPlacedEvent event) {
        this.bookingId = event.getBookingId();
        this.cardId = event.getCardId();
        this.partialPayment = event.isPartialPayment();

        SagaLifecycle.associateWith("cardId", cardId);

        if (partialPayment) {
            this.paymentDeadlineId = deadlineManager.schedule(Duration.of(7, ChronoUnit.DAYS), PAYMENT_DEADLINE);
        }

        commandGateway.send(new RedeemCardCommand(cardId, bookingId, event.getGiftCardAmount()))
                      .exceptionally(throwable -> {
                          commandGateway.send(new RejectBookingCommand(bookingId, cardId, INSUFFICIENT_FUNDS));
                          return null;
                      });
    }

    @SagaEventHandler(associationProperty = "cardId")
    public void on(CardRedeemedEvent event) {
        if (!bookingId.equals(event.getTransactionId())) {
            return;
        }

        commandGateway.send(new ConfirmBookingCommand(bookingId, event.getCardId(), event.getAmount()));
        if (!partialPayment) {
            SagaLifecycle.end();
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "bookingId")
    public void on(BookingRejectedEvent event) {
        logger.info("booking [{}] rejected ", event.getBookingId());
        if (paymentDeadlineId != null) {
            deadlineManager.cancelSchedule(paymentDeadlineId, PAYMENT_DEADLINE);
        }
    }

    @DeadlineHandler(deadlineName = PAYMENT_DEADLINE)
    public void handle() {
        commandGateway.send(new RejectBookingCommand(bookingId, cardId, PARTIAL_PAYMENT_DEADLINE_EXPIRED));
        SagaLifecycle.end();
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