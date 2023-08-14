package lia.practice.products.service;

import lia.practice.products.model.Product;
import lia.practice.products.repository.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Use UUID or String as paras/args depending on @Id datatype

@Service
public class ProductService {

    // Create logger object
    private static final Logger logger = LogManager.getLogger(ProductService.class);

//    @Autowired // Avoid field injection
//    private ProductRepository productRepository;

    // Constructor injection
    private ProductRepository productRepository;
    private ReactiveMongoTemplate reactiveMongoTemplate; // Needed for the multiple collections approach

    public ProductService(ProductRepository productRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productRepository = productRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate; // Needed for the multiple collections approach
    }


    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////
    ////-------- Methods used for default collection  -------////

    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get by id with logic/error handle etc
    public Mono<Product> getById(String id) {
        return productRepository.findById(UUID.fromString(id))
                // shorter version
                // If not exist, the task switches from finding to erroring
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with id: " + id + " not found")));

                // Longer version
                .switchIfEmpty(Mono.defer(() -> {
                    logger.error("Failed to find Product with id {}", id);
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
                })).doOnSuccess(productResult -> logger.info("Found Product with {}", id));
    }

    // create product (doesn't consider duplicates)
    public Mono<Product> createProduct(Product product) {
        // If no date/time is provided, set current date
        String creationDateTime;
        if (product.getCreationDateTimeString() == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Specify format
            String formattedDateTime = LocalDateTime.now().format(formatter); // Apply format
            creationDateTime = formattedDateTime;

            // Else set the date/time provided form postman/frontend
        } else {
            creationDateTime = product.getCreationDateTimeString();
        }

        // Use Product entity constructor, to generate uuid as id, before save in db
//        Product tempProduct = new Product(product.getName(), product.getFlavour(), product.getWeight());

        // Example create product and provide productId-UUID
//        Product tempProduct = new Product(product.getName(), product.getFlavour(), product.getWeight(), UUID.randomUUID());

        // Example create product and provide productId-UUID & creation date
        Product tempProduct = new Product(product.getName(), product.getDescription(), product.getWeight(), UUID.randomUUID(), creationDateTime);

        logger.info("Created a product");
        return productRepository.save(tempProduct);
    }

    // Create product with logic rejecting duplicate names
    public Mono<Product> createProductNoDuplicate(Product product) {
        // Check if product already exists in MongoDB
        // Use repository existByName method, that returns a boolean
        return productRepository.existsByName(product.getName())
                .flatMap(exists -> {
                    if (exists) {
//                        return Mono.error(new RuntimeException("Duplicate product found"));

                        logger.info(product.getName() + " already exist"); // Use logger
//                        System.out.println(product.getName() + " already exist");

                        return Mono.empty(); // Compulsory return here, so set to empty
                        // Or use springboot specific exception:
//                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, product.getName() + " already exist"));
                        // Or ordinary runtimeexception:
//                        return Mono.error(new RuntimeException("Duplicate product found"));

                    } else { // If not already exist, set creation logic and save
                        String creationDateTime;
                        if (product.getCreationDateTimeString() == null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Specify format
                            String formattedDateTime = LocalDateTime.now().format(formatter); // Apply format
                            creationDateTime = formattedDateTime;
                        } else {
                            creationDateTime = product.getCreationDateTimeString();
                        }

                        // Generate uuid for productId here:
//                        Product tempProduct = new Product(product.getName(), product.getFlavour(), product.getWeight(), UUID.randomUUID(), creationDateTime);

                        // Provided uuid for productId from postman/frontend etc:
                        Product tempProduct = new Product(product.getName(), product.getDescription(), product.getWeight(), product.getProductId(), creationDateTime);

                        logger.info(product.getName() + " created");
//                        System.out.println(product.getName() + " created");

                        return productRepository.save(tempProduct)
                                // On error (e.g. mongo db is shut down etc), log this error:
                                .doOnError(error -> logger.error("Error creating Product: {}", error.getMessage()))
                                .onErrorResume(error -> Mono.empty())
                                .doOnSuccess(productResult -> logger.info("Created Product: {}", productResult));
                    }
                });
    }

    public Mono<ResponseEntity<Product>> updateProductNoDuplicate(String id, Product product) {
        return productRepository.findById(UUID.fromString(id))
                .flatMap(existingProduct ->
                        // Also check if input name already exists
                        productRepository.existsByName(product.getName())
                                .flatMap(exists -> { // Check if exist but ignore duplicate if it is same as itself
                                    if (exists && !product.getName().trim().equalsIgnoreCase(existingProduct.getName().trim())) {
                                        return Mono.error(new RuntimeException("Duplicate product name"));

                                    } else { // If name is free to use, update/save new fields
                                        existingProduct.setName(product.getName());
                                        existingProduct.setDescription(product.getDescription());
                                        existingProduct.setWeight(product.getWeight());
                                        // Test string date/time:
                                        existingProduct.setCreationDateTimeString(product.getCreationDateTimeString());
                                        return productRepository.save(existingProduct);
                                    }
                                }))
                .map(updatedProduct -> new ResponseEntity<>(updatedProduct, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete by id with logic, error etc
    public Mono<Void> deleteById(String id) {
        return productRepository.deleteById(UUID.fromString(id))
                .doOnSuccess(result -> logger.info("Product with id {} has been deleted", id)) // Placeholder
                .doOnError(error -> {
                    logger.error("Failed to delete product with id {}: {}", id, error.getMessage());
                    throw new RuntimeException("Failed to delete product");
                });
    }


    ////----  Multiple collection approach 1: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 1: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 1: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 1: orgId from ENTITY FIELD ----////
    ////----  Multiple collection approach 1: orgId from ENTITY FIELD ----////


    // get all from a spec collection, by id, get orgid as entityfield via that id (using utility method)
    public Flux<Product> getAllProductsFromSpecificColl(String id) {
        return findByIdInAllCollections(UUID.fromString(id)) // Find/get it by id, with utility method
                .flatMapMany(foundProduct -> { // flatMapMany, mono to flux
                    // Use the Product's orgId to find its collection, get all products in it
                    String collectionName = "products_" + foundProduct.getOrgId();
                    return reactiveMongoTemplate.findAll(Product.class, collectionName);
                });
    }

    // Get by id from specific collection (without knowing the collection name)
    // get orgid from provided id; find product in that orgid-coll
    public Mono<Product> getByIdFromSpecificColl(String id) {
        return existsByIdInAllCollections(UUID.fromString(id)) // Call method for checking if exist
                .flatMap(exists -> { // Check if exists could be redundant; enough to find by id
                    System.out.println("exists: " + exists);
                    if (exists) {
                        // Call the method for finding it in all collections (and get it)
                        return findByIdInAllCollections(UUID.fromString(id))
                                .flatMap(foundProduct -> {
                                    System.out.println("my print " + foundProduct);
                                    String collectionName = "products_" + foundProduct.getOrgId();
                                    return reactiveMongoTemplate.findById(UUID.fromString(id), Product.class, collectionName)
                                            // If exsts, it will be found; so the following is a redundant error handle:
//                                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
//                                                    "Product with id" + id + " not found in collection " + collectionName)))
                                            .map(product -> foundProduct);
                                });
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with id: " + id + " not found"));
                    }
                })
                // Optional printout of eventual error
                .doOnError(error -> logger.error("Error while retrieving product: {}", error.getMessage()));
    }

    // Create method that saves into specific collection;
    // no orgId pathvar; uses getOrgId() from reqbody
    public Mono<Product> createProductInSpecificCollWithoutPathVar(Product product) {
        // If no date/time is provided, set current date
        String creationDateTime;
        if (product.getCreationDateTimeString() == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Specify format
            String formattedDateTime = LocalDateTime.now().format(formatter); // Apply format
            creationDateTime = formattedDateTime;

            // Else set the date/time provided form postman/frontend
        } else {
            creationDateTime = product.getCreationDateTimeString();
        }

        Product tempProduct = new Product(product.getOrgId(), product.getName(), product.getDescription(), product.getWeight(), product.getProductId(), creationDateTime);

        logger.info("Created a product");

        String collectionName = "products_" + product.getOrgId();

        // Use reactiveMongoTEMPLATE to save product into org-specific collection
        return reactiveMongoTemplate.save(tempProduct, collectionName); // second arg = collection to save to
    }

    // Checks if ealready exists in ANY of the collections
    // (used e.g. by mock data)
    public Mono<Product> createInSpecificCollWithoutPathVarNoDuplicate(Product product) {
        // Check if product already exists in MongoDB
        // Use existByName method in this service class, that returns a boolean
        return existsByNameInAllCollections(product.getName())
                .flatMap(exists -> {
                    if (exists) {
//                        return Mono.error(new RuntimeException("Duplicate product found"));

//                        logger.info(product.getName() + " already exist"); // Use logger
                        System.out.println(product.getName() + " already exist");

                        return Mono.empty(); // Compulsory return here, so set to empty

                    } else { // If not already exist, set creation logic and save
                        String creationDateTime;
                        if (product.getCreationDateTimeString() == null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Specify format
                            String formattedDateTime = LocalDateTime.now().format(formatter); // Apply format
                            creationDateTime = formattedDateTime;
                        } else {
                            creationDateTime = product.getCreationDateTimeString();
                        }

                        // Generate uuid for productId here:
//                        Product tempProduct = new Product(product.getName(), product.getFlavour(), product.getWeight(), UUID.randomUUID(), creationDateTime);

                        // Provided uuid for productId from postman/frontend etc:
//                        Product tempProduct = new Product(product.getName(), product.getFlavour(), product.getWeight(), product.getProductId(), creationDateTime);

                        // Also include orgId
                        Product tempProduct = new Product(product.getOrgId(), product.getName(), product.getDescription(), product.getWeight(), product.getProductId(), creationDateTime);


//                        logger.info(product.getName() + " created");
                        System.out.println(product.getName() + " created");

                        String collectionName = "products_" + product.getOrgId();
                        // Use reactiveMongoTEMPLATE to save product into org-specific collection
                        return reactiveMongoTemplate.save(tempProduct, collectionName)
                                .doOnError(error -> logger.error("Error creating Product: {}", error.getMessage()))
                                .onErrorResume(error -> Mono.empty())
                                .doOnSuccess(productResult -> logger.info("Created Product: {} in collection " + collectionName, productResult));
                    }
                });
    }


    // Delete by ID in ANY collection, simplified method
    // (If orgId is null, don't use this delete method)
    public Mono<Void> deleteByIdInAllColl(String id) {
        return findByIdInAllCollections(UUID.fromString(id)) // Find/get it by id
                .flatMap(foundProduct -> {
                    String collectionName = "products_" + foundProduct.getOrgId();
                    // Note that reactiveMongoTemlate delete in specific coll, uses remove()
                    // Also note that it requires the Product object, not its id
                    return reactiveMongoTemplate.remove(foundProduct, collectionName)
                            .doOnSuccess(result -> logger.info("Product with id {} has been deleted", id)) // Placeholder
                            .doOnError(error -> {
                                logger.error("Failed to delete product with id {}: {}", id, error.getMessage());
                                throw new RuntimeException("Failed to delete product");
                            });
                })
                // Convert nested mono from flatMap into single mono:
                .then();
    }

    // Improved delete method, checks thouroughly if exists, then deletes etc.
    // Use for any approach;
    // Delete by id; use customized find collection by Product id, to specify collection
    // Find collectionName and passes it in as coll to delete from
    public Mono<Void> deleteByIdUnknownColl(String id) {
        return existsByIdInAllCollections(UUID.fromString(id))// Check if exists
                .flatMap(exists -> {
                    if (exists) { // If exists, delete it, log this, and return empty mono (as it should)
                        return findByIdInAllCollections(UUID.fromString(id)) // Find & get it by id
                                .flatMap(foundProduct -> {
                                    logger.info("I got Product " + foundProduct);
                                    // Use our customized method to find the foundProduct' collection (mono-string)
                                    return findCollectionNameById(UUID.fromString(id))
                                            .flatMap(collectionName -> {
                                                logger.info("I got collection " + collectionName);
                                                return reactiveMongoTemplate.remove(foundProduct, collectionName)
                                                        // Coll name as string
                                                        // Error handling
                                                        .doOnSuccess(result -> logger.info("Product with id {} has been deleted", id))
                                                        .doOnError(error -> {
                                                            logger.error("Failed to delete product with id {}: {}", id, error.getMessage());
                                                            throw new RuntimeException("Failed to delete product");
                                                        })
                                                        .then();
                                            });
                                });
                    } else { // If doesn't exist, log this, and return error (see onErrorResume part)
                        logger.info("**No Assessment found with id {}", id);
                        return Mono.error(new RuntimeException("No Assessment found with id " + id));
                    }
                })
                .onErrorResume(error -> { // Handle eventual error from previous step
                    logger.error("Failed to delete Product with id {}: {}", id, error.getMessage());
                    return Mono.error(new RuntimeException("Failed to delete Assessment"));
                })
                // Return an empty Mono on completion; besides eventual previous error returned above
                .then();
    }


    ////---- Multiple collection approach 2: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 2: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 2: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 2: orgId from pathvar (no entity-field orgId)----////
    ////---- Multiple collection approach 2: orgId from pathvar (no entity-field orgId)----////


    // Get all from specific coll, by orgid as pathvar, with error handle
    public Flux<Product> getAllProductsFromSpecificColl(UUID orgId) {
        String collectionName = "products_" + orgId;
        return reactiveMongoTemplate.findAll(Product.class, collectionName);
    }


    public Mono<Product> getByIdFromSpecificColl(String id, UUID orgId) {
        String collectionName = "products_" + orgId;
        return reactiveMongoTemplate.findById(UUID.fromString(id), Product.class, collectionName);
    }

    // Create into spec coll, as set in orgid in pathvar;
    public Mono<Product> createProductInSpecificColl(Product product, UUID orgId) {

        return existsByNameInAllCollections(product.getName())
                .flatMap(exists -> {
                    if (exists) {

                        logger.info(product.getName() + " already exist");

                        return Mono.empty();
                    } else {


                        // If no date/time is provided, set current date
                        String creationDateTime;
                        if (product.getCreationDateTimeString() == null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Specify format
                            String formattedDateTime = LocalDateTime.now().format(formatter); // Apply format
                            creationDateTime = formattedDateTime;

                            // Else set the date/time provided form postman/frontend
                        } else {
                            creationDateTime = product.getCreationDateTimeString();
                        }

                        // Note: the passed in orgId is set as coll, not eventual body orgId
                        // If orgid is null, it is saved in default/products colelction
                        // Use if orgId entity field should be null:
//                        Product tempProduct = new Product(product.getName(), product.getDescription(), product.getWeight(), product.getProductId(), creationDateTime);
                        // Use if orgId entity field should be set to the provided orgId in the pathvar
                        Product tempProduct = new Product(orgId, product.getName(), product.getDescription(), product.getWeight(), product.getProductId(), creationDateTime);
                        // Use if orgId entity field is provided in req body:
//                        Product tempProduct = new Product(product.getOrgId(), product.getName(), product.getDescription(), product.getWeight(), product.getProductId(), creationDateTime);

                        logger.info("Created a product");

                        String collectionName = "products_" + orgId; // Create specified collection for this orgId

                        // Use reactiveMongoTEMPLATE to save product into org-specific collection
                        return reactiveMongoTemplate.save(tempProduct, collectionName); // second arg = collection to save to

                    }
                });
    }

    //----- DELETE BY ID, USE the imrpoved DELETE METHOD deleteByIdUnknownColl() -----//


    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////
    ////---- Other eventually needed endpoints (Might be moved to own service class ----////


    // Get all by collection name (so NOT constructed as products_ + ....)
    public Flux<Product> getAllCollNamePathvar(String collName) {
        String collecstionName = collName; // Redundant step, but pedagogic
        return reactiveMongoTemplate.findAll(Product.class, collName);
    }

    // Return list of all collection names (and log them for control check)
    public Mono<List<String>> getAllCollectionNames() {
        Flux<String> collectionNames = reactiveMongoTemplate.getCollectionNames();
        List<String> collectionNameList = new ArrayList<>();

        return collectionNames
                .doOnNext(collectionName -> logger.info(collectionName))// Control log
                .collectList() // Collect from flux...
                .doOnSuccess(collectionNameList::addAll) // ...add them into the created ArrayList
                .thenReturn(collectionNameList); // Return the ArrayList
    }





    ////---- Utility methods for multiple collections; for all approaches ----////
    ////---- Utility methods for multiple collections; for all approaches ----////
    ////---- Utility methods for multiple collections; for all approaches ----////

    // Method for finding by id in ALL collections
    public Mono<Product> findByIdInAllCollections(UUID id) {
        Flux<String> collectionNames = reactiveMongoTemplate.getCollectionNames(); // Get all collections
        return collectionNames
                .doOnNext(collectionName -> System.out.println("Collection name: " + collectionName)) // Control print
                // Check if exists in each of the retrieved collections:
                .flatMap(collectionName -> reactiveMongoTemplate.findById(id, Product.class, collectionName))
                .next(); // Get only first one, in case there are duplicates; "flux is transformed to mono"
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with id: " + id + " not found")));
    }

    // Method for checking by ID if exists in ANY of the collections
    public Mono<Boolean> existsByIdInAllCollections(UUID id) {
        Flux<String> collectionNames = reactiveMongoTemplate.getCollectionNames(); // Get all collections
        return collectionNames
                .doOnNext(collectionName -> System.out.println("Collection name*: " + collectionName)) // Control print
                // Check if exists in each of the retrieved collections:
                .flatMap(collectionName -> reactiveMongoTemplate.exists(Query.query(Criteria.where("id").is(id)), Product.class, collectionName))
                .any(exists -> exists); // Returns true if any of the collections includes this product
    }

    // Method for checking by NAME if exists in ANY of the collections
    public Mono<Boolean> existsByNameInAllCollections(String name) {
        Flux<String> collectionNames = reactiveMongoTemplate.getCollectionNames(); // Get all collections
        return collectionNames
                .doOnNext(collectionName -> System.out.println("Collection name*: " + collectionName)) // Control print
                // Check if exists in each of the retrieved collections:
                .flatMap(collectionName -> reactiveMongoTemplate.exists(Query.query(Criteria.where("name").is(name)), Product.class, collectionName))
                .any(exists -> exists); // Returns exists/true if any of the collections includes this product
    }

    // Return collection for any given Product id
    public Mono<String> findCollectionNameById(UUID id) {
        Flux<String> collectionNames = reactiveMongoTemplate.getCollectionNames();
        return collectionNames
                .flatMap(collectionName ->
                        // Loop through each collection, search for Product id (note: entity class as second arg)
                        reactiveMongoTemplate.findOne(Query.query(Criteria.where("id").is(id)), Product.class, collectionName)
                                .map(obj -> collectionName)
                                .switchIfEmpty(Mono.empty())
                )
                .next() // Construct and return first found collection with this Product id
                .doOnSuccess(collectionName -> logger.info("Collection name found for Product ID {}: {}", id, collectionName));
    }


}
