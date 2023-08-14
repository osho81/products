package lia.practice.products.controller;

import lia.practice.products.model.Product;
import lia.practice.products.service.ProductService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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


    // Create products with response, and no duplicates
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


    // Create product, in spec coll; use
    @PostMapping("/createproducts/{orgId}") // Pass and get orgId as pathVar
    public Mono<ResponseEntity<Product>> createProductInSpecificColl(@RequestBody Product product, @PathVariable UUID orgId) {
        // Use multiple/separated collections service method
        return productService.createProductInSpecificColl(product, orgId)
                .map(saveProduct -> ResponseEntity.status(HttpStatus.CREATED).body(saveProduct))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
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

    // Get by id from specific collection (without knowing the collection name)
    // get orgid from provided id; find product in that orgid-coll
    @GetMapping("/productbyid/unknowncoll/{id}")
    public Mono<ResponseEntity<Product>> getByIdFromSpecificColl(@PathVariable String id) {
        return productService.getByIdFromSpecificColl(id)
                .map(ResponseEntity::ok)
                .onErrorResume(ResponseStatusException.class, e -> Mono.just(ResponseEntity.status(e.getStatusCode()).build()));
    }

    // Create method that saves into specific collection;
    // no orgId pathvar; uses getOrgId() from reqbody
    @PostMapping("/createproducts/reqbody") // No orgId pathVar; will use getOrgId
    public Mono<ResponseEntity<Product>> createProductInSpecificCollWithoutPathVar(@RequestBody Product product) {
        // Use multiple/separated collections service method
//        return productService.createProductInSpecificCollWithoutPathVar(product) // duplicates allowed
        return productService.createInSpecificCollWithoutPathVarNoDuplicate(product) // No duplicates
                .map(saveProduct -> ResponseEntity.status(HttpStatus.CREATED).body(saveProduct))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

    }

    // Delete by ID in ANY collection (except null)
    // (If orgId is null, don't use this delete method)
    @DeleteMapping("/deleteproducts/orgidasentityfield/{id}")
    public Mono<ResponseEntity<Void>> deleteByIdInAllColl(@PathVariable String id) {
//        return productService.deleteByIdInAllColl(id)
        return productService.deleteByIdUnknownColl(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(error -> {
                    logger.error("Failed to delete product with id {}: {}", id, error.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .map(response -> ResponseEntity.status(response.getStatusCode()).build());
    }


    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////


    // Get all products from all collection, specific colls as well as default coll
    @GetMapping("/allfromallcolls")
    public Mono<ResponseEntity<List<Product>>> getAllFromAllColls() {
        return productService.getAllFromAllColls()
                .map(productList -> ResponseEntity.ok(productList));
    }

    // Get all by collection name (so NOT constructed as products_ + ....)
    @GetMapping("/products/collnameaspathvar/{collName}")
    public Mono<ResponseEntity<List<Product>>> getAllCollNamePathvar(@PathVariable String collName) {
        return productService.getAllCollNamePathvar(collName)
                .collectList()
                .map(assessmentList -> ResponseEntity.ok(assessmentList));
    }


    // Return all collection in database
    @GetMapping("/collections")
    public Mono<ResponseEntity<List<String>>> getAllCollectionNames() {
        return productService.getAllCollectionNames()
                .map(productList -> ResponseEntity.ok(productList));
    }

    // TODO:
    // Eventually add follwoing methods:
    // - save a list of products
    // - copy coll to another coll
    // - aggregation methods, i.e. calculate total or average weight for products in a coll





}
