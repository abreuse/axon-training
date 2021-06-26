package io.axoniq.training.labs.giftcard.coreapi;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CardSummary {

    @Id
    private String cardId;
    private int initialValue;
    private Instant issuedAt;
    private int remainingValue;

    public CardSummary() {
    }

    public CardSummary(String cardId, int initialValue, Instant issuedAt, int remainingValue) {
        this.cardId = cardId;
        this.initialValue = initialValue;
        this.issuedAt = issuedAt;
        this.remainingValue = remainingValue;
    }

    public String getCardId() {
        return cardId;
    }

    public int getInitialValue() {
        return initialValue;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public int getRemainingValue() {
        return remainingValue;
    }
}
