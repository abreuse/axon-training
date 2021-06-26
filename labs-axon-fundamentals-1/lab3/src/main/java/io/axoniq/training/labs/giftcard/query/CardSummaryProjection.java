package io.axoniq.training.labs.giftcard.query;

import io.axoniq.training.labs.giftcard.coreapi.*;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Component
public class CardSummaryProjection {

    private final CardSummaryRepository cardSummaryRepository;

    public CardSummaryProjection(CardSummaryRepository cardSummaryRepository) {
        this.cardSummaryRepository = cardSummaryRepository;
    }

    @EventHandler
    public void handle(CardIssuedEvent event, @Timestamp Instant timestamp) {
        cardSummaryRepository.save(new CardSummary(event.getCardId(), event.getAmount(), timestamp, event.getAmount()));
    }

    @EventHandler
    public void handle(CardRedeemedEvent event, @Timestamp Instant timestamp) {
        CardSummary cardSummary = cardSummaryRepository.getOne(event.getCardId());
        cardSummaryRepository.save(new CardSummary(cardSummary.getCardId(), cardSummary.getInitialValue(),
                cardSummary.getIssuedAt(), cardSummary.getRemainingValue() - event.getAmount()));
    }

    @EventHandler
    public void handle(CardReimbursedEvent event, @Timestamp Instant timestamp) {
        CardSummary cardSummary = cardSummaryRepository.getOne(event.getCardId());
        cardSummaryRepository.save(new CardSummary(cardSummary.getCardId(), cardSummary.getInitialValue(),
                cardSummary.getIssuedAt(), cardSummary.getRemainingValue() + event.getAmount()));
    }

    @QueryHandler
    public List<CardSummary> handle(FindCardSummariesQuery query) {
        return cardSummaryRepository.findAll(PageRequest.of(query.getPageNumber(), query.getLimit())).getContent();
    }

    @QueryHandler
    public Long handle(CountCardSummariesQuery query) {
        return cardSummaryRepository.count();
    }
}
