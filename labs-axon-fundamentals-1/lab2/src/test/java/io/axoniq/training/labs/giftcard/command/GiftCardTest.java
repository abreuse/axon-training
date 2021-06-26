package io.axoniq.training.labs.giftcard.command;

import io.axoniq.training.labs.giftcard.coreapi.*;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GiftCardTest {

    private FixtureConfiguration<GiftCard> fixture;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(GiftCard.class);
    }

    @Test
    void shouldIssueGiftCard() {
        fixture.given()
                .when(new IssueCardCommand("1", 100))
                .expectEvents(new CardIssuedEvent("1", 100));
    }

    @Test
    void shouldRedeemGiftCard() {
        fixture.given(new CardIssuedEvent("1", 100))
                .when(new RedeemCardCommand("1", "1", 50))
                .expectEvents(new CardRedeemedEvent("1", "1", 50));
    }

    @Test
    void shouldNotRedeemWithNegativeAmount() {
        fixture.given(new CardIssuedEvent("1", 100))
                .when(new RedeemCardCommand("1", "1", -50))
                .expectNoEvents()
                .expectException(BusinessRuleViolation.class)
                .expectExceptionMessage("You must redeem a positive amount!");
    }

    @Test
    void shouldNotRedeemWhenThereIsNotEnoughMoney() {
        fixture.given(new CardIssuedEvent("1", 100))
                .when(new RedeemCardCommand("1", "1", 200))
                .expectNoEvents()
                .expectException(BusinessRuleViolation.class)
                .expectExceptionMessage("You don't have enough funds on your Gift Card!");
    }

    @Test
    void shouldReimburseTransaction() {
        fixture.given(new CardIssuedEvent("1", 100), new CardRedeemedEvent("1", "1", 50))
                .when(new ReimburseCardCommand("1", "1"))
                .expectEvents(new CardReimbursedEvent("1", "1", 50));
    }

    @Test
    void shouldNotReimburseTransactionWhenItIsAlreadyReimbursed() {
        fixture.given(new CardIssuedEvent("1", 100),
                new CardRedeemedEvent("1", "1", 50),
                new CardReimbursedEvent("1", "1", 50))
                .when(new ReimburseCardCommand("1", "1"))
                .expectNoEvents()
                .expectException(BusinessRuleViolation.class);
    }
}
