package lia.practice.products.repository;

import lia.practice.products.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;


// Set to String or UUID depending on @Id datatype in entity
public interface ProductRepository extends ReactiveMongoRepository<Product, UUID> { // UUID used in Product Entity
    Mono<Product> findByName(String name);

    Mono<Boolean> existsByName(String name);

    Mono<Boolean> existsById(UUID id);


}