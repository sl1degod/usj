package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductReviewsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.client.exception.BadRequestException;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import ag.selm.customer.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("customer/products/{productId:\\d+}")
@Slf4j
public class ProductController {

    private final ProductsClient productsClient;

    private final FavouriteProductsClient favouriteProductsClient;

    private final ProductReviewsClient productReviewsClient;


    @ModelAttribute(name = "product", binding = false)
    public Mono<Product> loadProduct(@PathVariable("productId") int id) {
        return this.productsClient.findProduct(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("customer.products.error.not_found")));
    }

    @GetMapping
    public Mono<String> getProductPage(@PathVariable("productId") int id, Model model) {
        model.addAttribute("inFavourite", false);
        return this.productReviewsClient.findProductReviewsByProductId(id)
                .collectList()
                .doOnNext(productReviews -> model.addAttribute("reviews", productReviews))
                .then(this.favouriteProductsClient.findFavouriteProductByProduct(id)
                        .doOnNext(favouriteProduct -> model.addAttribute("inFavourite", true)))
                .thenReturn("customer/products/product");
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addProductToFavourites(@ModelAttribute("product") Mono<Product> productMono) {
        return productMono
                .map(Product::id)
                .flatMap(productId -> this.favouriteProductsClient.addProductToFavourites(productId)
                        .thenReturn("redirect:/customer/products/%d".formatted(productId))
                        .onErrorResume(ex -> {
                            log.error(ex.getMessage(), ex);
                            return Mono.just("redirect:/customer/products/%d".formatted(productId));
                        }));
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> removeProductFromFavourites(@ModelAttribute("product") Mono<Product> productMono) {
        return productMono
                .map(Product::id)
                .flatMap(productId -> this.favouriteProductsClient.removeProductFromFavourites(productId)
                        .thenReturn("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("create-review")
    public Mono<String> createReview(@PathVariable("productId") int id,
                                     NewProductReviewPayload payload,
                                     Model model) {
            return this.productReviewsClient.createProductReview(id, payload.rating(), payload.review())
                    .thenReturn("redirect:/customer/products/%d".formatted(id))
                    .onErrorResume(BadRequestException.class, exception -> {
                        model.addAttribute("inFavourite", false);
                        model.addAttribute("payload", payload);
                        model.addAttribute("errors", exception.getErrors());
                        return this.favouriteProductsClient.findFavouriteProductByProduct(id)
                                .doOnNext(favouriteProduct -> model.addAttribute("inFavourite", true))
                                .thenReturn("customer/products/product");
                    });
        }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception, Model model) {
        model.addAttribute("error", exception.getMessage());
        return "errors/404";
    }
}
