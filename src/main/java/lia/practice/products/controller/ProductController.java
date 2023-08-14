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

import java.util.List;
import java.util.UUID;

// Use UUID or String as paras/args depending on @Id datatype

@RestController
@RequestMapping("/api/v1")
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

    @GetMapping("/products")
    private Mono<ResponseEntity<List<Product>>> getAllMockassess() {
        return productService.getAllProducts()
                .collectList()
                .map(assessmentList -> ResponseEntity.ok(assessmentList));
    }

    // Get by id; with pathvar; with response
    @GetMapping("/productbyid/{id}")
    public Mono<ResponseEntity<Product>> getById(@PathVariable String id) {
        return productService.getById(id)
//                .map(ResponseEntity::ok); // Short version
                .map(product -> ResponseEntity.ok(product)); // longer version
        // error handle at service method instead
//                .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

    // create product (doesn't consider duplicates), with shortcut response
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
        return productService.updateProductNoDuplicate(id, product);
    }

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


    ////---- Multiple collection approach 1: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 1: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 1: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 1: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 1: orgId from pathvar (no entity-field orgId)----////


    // Get all from specific coll, by orgid as pathvar, with reesponseentity, shorter version
    @GetMapping("/orgidaspathvar/{orgId}")
    public Mono<ResponseEntity<List<Product>>> getAllProductsFromSpecificColl(@PathVariable UUID orgId) {
        return productService.getAllProductsFromSpecificColl(orgId)
                .collectList() // Collect list from flux from service
                .map(productList -> ResponseEntity.ok(productList));
    }

    // Get product by id & orgId as pathvars
    @GetMapping("/idandorgidaspathvar/{id}/{orgId}")
    public Mono<Product> getByIdFromSpecificColl(@PathVariable String id, @PathVariable UUID orgId) {
        return productService.getByIdFromSpecificColl(id, orgId);
    }

    ////--- can also use the special get by id method, that loops all colls ----////

    @PostMapping("/createproducts/{orgId}") // Pass and get orgId as pathVar
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<Product> createProductInSpecificColl(@RequestBody Product product, @PathVariable UUID orgId) {
        // Use multiple/separated collections service method
        return productService.createProductInSpecificColl(product, orgId);
    }


    ////----  Multiple collection approach 2: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 2: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 2: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 2: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 2: orgId from ENTITY FIELD ----////


    // get all from a spec collection, by id, get orgid as entityfield via the provided id
    @GetMapping("/orgidasentityfield/{id}") // Product id (not orgId in this version)
    public Mono<ResponseEntity<List<Product>>> getAllProductsFromSpecificColl(@PathVariable String id) {
        return productService.getAllProductsFromSpecificColl(id)
                .collectList() // Collect list from flux from service
                .map(productList -> ResponseEntity.ok(productList));
    }

    @GetMapping("/productbyid/orgidasentityfield/{id}")
    public Mono<ResponseEntity<Product>> getByIdFromSpecificColl(@PathVariable String id) {
        return productService.getByIdFromSpecificColl(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

    // Create method that saves into specific collection;
    // no orgId pathvar; uses getOrgId() from reqbody
    @PostMapping("/createproducts/specificcoll") // No orgId pathVar; will use getOrgId
    @ResponseStatus(value = HttpStatus.CREATED) // Simplified response
    public Mono<Product> createProductInSpecificCollWithoutPathVar(@RequestBody Product product) {
        // Use multiple/separated collections service method
        return productService.createProductInSpecificCollWithoutPathVar(product);
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
