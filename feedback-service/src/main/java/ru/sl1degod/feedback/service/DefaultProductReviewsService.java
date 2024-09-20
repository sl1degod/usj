package ru.sl1degod.feedback.service;

import ru.sl1degod.feedback.entity.ProductReview;
import ru.sl1degod.feedback.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultProductReviewsService implements ProductReviewsService {

    private final ProductReviewRepository productReviewRepository;

    @Override
    public Mono<ProductReview> createProductReview(int productId, int rating, String review) {
        return this.productReviewRepository.save(
                new ProductReview(UUID.randomUUID(), productId, rating, review));
    }

    @Override
    public Flux<ProductReview> findProductReviewsByProduct(int productId) {
        return this.productReviewRepository.findAllByProductId(productId);
    }
}
