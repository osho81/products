package lia.practice.products.controller;

import lia.practice.products.model.Product;
import lia.practice.products.service.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

// Use UUID or String as paras/args depending on @Id datatype

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}) // React & Angular
public class ProductController {

    // Create logger object
    private static final Logger logger = LogManager.getLogger(ProductController.class);

//    @Autowired // Avoid field injection
//    private ProductService productService;

    // Constructor injection
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////

//    @GetMapping
//    public Flux<Product> getAllProducts() {
//        return productService.getAllProducts();
//    }

    // Example getAllProducts method with extensive trace example
    @GetMapping
    public Flux<Product> getAllProducts() {

        logger.info("Entering getAllProducts() method");

        Flux<Product> products = productService.getAllProducts();

        products.doOnComplete(() -> logger.trace("Finished retrieving all products"))
                .doOnError(error -> logger.error("Error occurred while retrieving products: {}", error.getMessage()))
                .doOnNext(product -> logger.trace("Retrieved product: {}", product.getId()))
                .subscribe();

        logger.trace("Leaving getAllProducts() method");
        return products;
    }

    // Basic get by id
//    @GetMapping("/productbyid/{id}")
//    public Mono<Product> getById(@PathVariable String id) {
//        return productService.getById(id);
//    }

    // Get by id with response
    @GetMapping("/productbyid/{id}")
    public Mono<ResponseEntity<Product>> getById(@PathVariable String id) {
        return productService.getById(id)
                .map(ResponseEntity::ok);
//                .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

//    @PostMapping("/createproducts")
////    @ResponseStatus(value = HttpStatus.CREATED) // Non-customized response
//    public Mono<Product> createProduct(@RequestBody Product product) {
////        return productService.createProduct(product);
//        return productService.createProductNoDuplicate(product); // Using the no duplicate logic
//    }


    // Create products with response
    @PostMapping("/createproducts")
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return productService.createProductNoDuplicate(product)
                // On success return response incl. saved product
                .map(savedProduct -> ResponseEntity.status(HttpStatus.CREATED).body(savedProduct))
                // Generic exception handle:
//                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
                // Specific exception handle with status and optional message:
               .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

    @PutMapping("/updateproducts/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody Product product) {
//        return productService.updateProduct(id, product);
        return productService.updateProductNoDuplicate(id, product); // Using the no duplicate name logic
    }

//    @DeleteMapping("/deleteproducts/{id}")
//    public Mono<Void> deleteById(@PathVariable String id) {
//        return productService.deleteById(id);
//    }

    // delete by id with response
    @DeleteMapping("/deleteproducts/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable String id) {
        return productService.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(error -> {
                    logger.error("Failed to delete product with id {}: {}", id, error.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .map(response -> ResponseEntity.status(response.getStatusCode()).build());
    }


    ////---- Methods used for multiple collection; orgId as pathvar ----////
    ////---- Methods used for multiple collection; orgId as pathvar ----////
    ////---- Methods used for multiple collection; orgId as pathvar ----////
    ////---- Methods used for multiple collection; orgId as pathvar ----////
    ////---- Methods used for multiple collection; orgId as pathvar ----////
    ////---- Methods used for multiple collection; orgId as pathvar ----////

    @PostMapping("/createproducts/{orgId}") // Get orgId as pathVar
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<Product> createProductInSpecificColl(@RequestBody Product product, @PathVariable UUID orgId) {
        // Use multiple/separated collections service method
        return productService.createProductInSpecificColl(product, orgId);
    }


    @GetMapping("/{orgId}")
    public Flux<Product> getAllProductsFromSpecificColl(@PathVariable UUID orgId) {

        logger.info("Entering getAllProductsFromSpecificColl() method");

        Flux<Product> products = productService.getAllProductsFromSpecificColl(orgId);

        products.doOnComplete(() -> logger.trace("Finished retrieving all products"))
                .doOnError(error -> logger.error("Error occurred while retrieving products: {}", error.getMessage()))
                .doOnNext(product -> logger.trace("Retrieved product: {}", product.getId()))
                .subscribe();

        logger.trace("Leaving getAllProductsFromSpecificColl() method");
        return products;
    }


    @GetMapping("/productbyid/{id}/{orgId}")
    public Mono<Product> getByIdFromSpecificColl(@PathVariable String id, @PathVariable UUID orgId) {
        return productService.getByIdFromSpecificColl(id, orgId);
    }


    ////---- Methods used for multiple collection; orgId as entity field ----////
    ////---- Methods used for multiple collection; orgId as entity field ----////
    ////---- Methods used for multiple collection; orgId as entity field ----////
    ////---- Methods used for multiple collection; orgId as entity field ----////
    ////---- Methods used for multiple collection; orgId as entity field ----////
    ////---- Methods used for multiple collection; orgId as entity field ----////


    @PostMapping("/createproducts/specificcoll") // No orgId pathVar; will use getOrgId
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<Product> createProductInSpecificCollWithoutPathVar(@RequestBody Product product) {
        // Use multiple/separated collections service method
        return productService.createProductInSpecificCollWithoutPathVar(product);
    }

    @GetMapping("/{id}") // Product id (not orgId in this version)
    public Flux<Product> getAllProductsFromSpecificColl(@PathVariable String id) {

        logger.info("Entering getAllProductsFromSpecificColl() method");

        Flux<Product> products = productService.getAllProductsFromSpecificColl(id);

        products.doOnComplete(() -> logger.trace("Finished retrieving all products"))
                .doOnError(error -> logger.error("Error occurred while retrieving products: {}", error.getMessage()))
                .doOnNext(product -> logger.trace("Retrieved product: {}", product.getId()))
                .subscribe();

        logger.info("Leaving getAllProductsFromSpecificColl() method");
        return products;
    }

    @GetMapping("/productbyid/specificcoll/{id}")
    public Mono<ResponseEntity<Product>> getByIdFromSpecificColl(@PathVariable String id) {
        return productService.getByIdFromSpecificColl(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

    @DeleteMapping("/deleteproducts/specificcoll/{id}")
    public Mono<ResponseEntity<Void>> deleteByIdInAllColl(@PathVariable String id) {
        return productService.deleteByIdInAllColl(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(error -> {
                    logger.error("Failed to delete product with id {}: {}", id, error.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .map(response -> ResponseEntity.status(response.getStatusCode()).build());
    }


    ////---- multiple coll; collName as arg; use with e.g. manually created db coll ----////
    ////---- multiple coll; collName as arg; use with e.g. manually created db coll ----////
    ////---- multiple coll; collName as arg; use with e.g. manually created db coll ----////
    ////---- multiple coll; collName as arg; use with e.g. manually created db coll ----////
    ////---- multiple coll; collName as arg; use with e.g. manually created db coll ----////
    ////---- multiple coll; collName as arg; use with e.g. manually created db coll ----////


        @PostMapping("/createproducts/collnameaspathvar/{collName}")
        @ResponseStatus(value = HttpStatus.CREATED)
        public Mono<ResponseEntity<Product>> createProductInSpecificColl(@RequestBody Product product, @PathVariable String collName) {
//            return productService.createProductInSpecificCollCollNamePathVar(product, collName)
            return productService.createProductInSpecificCollCollNamePathVarNoDuplicate(product, collName)
                    // On success return response incl. saved product
                    .map(savedProduct -> ResponseEntity.status(HttpStatus.CREATED).body(savedProduct))
                    // Generic exception handle:
//                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
                    // Specific exception handle with status and optional message:
                    .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
        }






}
