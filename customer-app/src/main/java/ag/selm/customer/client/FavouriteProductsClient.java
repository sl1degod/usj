package ag.selm.customer.client;

import ag.selm.customer.entity.FavouriteProduct;
import ag.selm.customer.entity.ProductReview;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface FavouriteProductsClient {

    Mono<FavouriteProduct> findFavouriteProductByProduct(int id);

    Mono<FavouriteProduct> addProductToFavourites(Integer productId);

    Flux<FavouriteProduct> findFavouriteProducts();

    Mono<Void> removeProductFromFavourites(int productId);
}
