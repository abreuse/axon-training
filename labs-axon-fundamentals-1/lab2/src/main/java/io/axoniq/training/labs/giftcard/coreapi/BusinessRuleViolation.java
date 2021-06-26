package io.axoniq.training.labs.giftcard.coreapi;

public class BusinessRuleViolation extends RuntimeException {
    public BusinessRuleViolation(String message) {
        super(message);
    }
}
