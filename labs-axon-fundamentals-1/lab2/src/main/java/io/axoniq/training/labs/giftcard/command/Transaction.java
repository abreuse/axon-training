package io.axoniq.training.labs.giftcard.command;

import io.axoniq.training.labs.giftcard.coreapi.BusinessRuleViolation;
import io.axoniq.training.labs.giftcard.coreapi.CardReimbursedEvent;
import io.axoniq.training.labs.giftcard.coreapi.ReimburseCardCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

import java.util.Objects;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

public class Transaction {

    @EntityId
    private final String transactionId;
    private final int amount;
    private Status status = Status.PAID;

    public Transaction(String transactionId, int amount) {
        this.transactionId = transactionId;
        this.amount = amount;
    }

    @CommandHandler
    public void handle(ReimburseCardCommand command) {
        if(status == Status.REIMBURSED) {
            throw new BusinessRuleViolation("This transaction is already reimbursed!");
        }

        apply(new CardReimbursedEvent(command.getCardId(), command.getTransactionId(), amount));
    }

    @EventSourcingHandler
    public void on(CardReimbursedEvent event) {
        status = Status.REIMBURSED;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return amount == that.amount && Objects.equals(transactionId, that.transactionId) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, amount, status);
    }
}
