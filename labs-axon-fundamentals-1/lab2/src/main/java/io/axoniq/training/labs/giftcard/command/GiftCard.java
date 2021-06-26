package io.axoniq.training.labs.giftcard.command;

import io.axoniq.training.labs.giftcard.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.List;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class GiftCard {

    @AggregateIdentifier
    private String cardId;

    private int amount;

    @AggregateMember
    private List<Transaction> transactions;

    public GiftCard() {
    }

    @CommandHandler
    public GiftCard(IssueCardCommand command) {
        apply(new CardIssuedEvent(command.getCardId(), command.getAmount()));
    }

    @EventSourcingHandler
    public void on(CardIssuedEvent event) {
        cardId = event.getCardId();
        amount = event.getAmount();
        transactions = new ArrayList<>();
    }

    @CommandHandler
    public void handle(RedeemCardCommand command) {
        //shouldn't the Aggregate generates the transaction id ?

        if (command.getAmount() <= 0) {
            throw new BusinessRuleViolation("You must redeem a positive amount!");
        }

        if (command.getAmount() > amount) {
            throw new BusinessRuleViolation("You don't have enough funds on your Gift Card!");
        }

        if (transactionIdMatchesAnyPreviousTransactionId(command.getTransactionId())) {
            throw new BusinessRuleViolation("This transaction id already exists!");
        }

        apply(new CardRedeemedEvent(command.getCardId(), command.getTransactionId(), command.getAmount()));
    }

    private boolean transactionIdMatchesAnyPreviousTransactionId(String transactionId) {
        return transactions.stream()
                .map(Transaction::getTransactionId)
                .anyMatch(it -> it.equals(transactionId));
    }

    @EventSourcingHandler
    public void on(CardRedeemedEvent event) {
        amount = amount - event.getAmount();
        transactions.add(new Transaction(event.getTransactionId(), event.getAmount()));
    }

    @EventSourcingHandler
    public void on(CardReimbursedEvent event) {
        amount = amount + event.getAmount();
    }
}
