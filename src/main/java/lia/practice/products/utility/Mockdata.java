package lia.practice.products.utility;

import lia.practice.products.model.Product;
import lia.practice.products.repository.ProductRepository;
import lia.practice.products.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Configuration // Enable bean for mockdata on startup
@Component // More specific class annotation
public class Mockdata {

//    @Autowired // Avoid field injection
//    private ProductRepository productRepository;

    // Constructor injection
    private final ProductRepository productRepository;
    private final ProductService productService;

    public Mockdata(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }


    @Bean
    public CommandLineRunner databaseSeeder() {
        return args -> {

            // Create mock snacks:
            // Id (uuid) is generated (so not included as arg)
            // Product-id (uuid) is randomized here and provided to the snack constructor
            // Where date & time is null, it gets assigned current date/time
            // (UUID for orgId generated at https://www.uuidgenerator.net/version1)
            Product product1 = new Product(UUID.fromString("58cdac92-871f-4b41-9fbc-91693075a3f4"),"mock snack 1", "cheese", 150, UUID.randomUUID(), "2023-02-02 12:00:00");
            Product product2 = new Product(UUID.fromString("58cdac92-871f-4b41-9fbc-91693075a3f4"),"mock snack 2", "salty", 275, UUID.randomUUID(), "2023-03-03 13:15:00");
            Product product3 = new Product(UUID.fromString("66bbcf48-cf60-40a5-a564-1a8db0bea8bb"), "mock snack 3", "onion", 125, UUID.randomUUID(), "2023-02-12 15:35:00");
            Product product4 = new Product(UUID.fromString("66bbcf48-cf60-40a5-a564-1a8db0bea8bb"),"mock snack 4", "sourcream", 100, UUID.randomUUID(), null);
            Product product5 = new Product(null,"mock snack 5", "cheese", 50, UUID.randomUUID(), "2023-02-29 22:05:20");

            // Use method in this class to create snacks with no duplicate logic
//            createSnackNoDuplicateMock(product1).subscribe();
//            createSnackNoDuplicateMock(product2).subscribe();
//            createSnackNoDuplicateMock(product3).subscribe();
//            createSnackNoDuplicateMock(product4).subscribe();
//            createSnackNoDuplicateMock(product5).subscribe();//

            // Use corresponding method in service class
//            snackService.createSnackNoDuplicate(product1).subscribe();
//            snackService.createSnackNoDuplicate(product2).subscribe();
//            snackService.createSnackNoDuplicate(product3).subscribe();
//            snackService.createSnackNoDuplicate(product4).subscribe();
//            snackService.createSnackNoDuplicate(product5).subscribe();

            // Use save to specific collections
            productService.createInSpecificCollWithoutPathVarNoDuplicate(product1).subscribe();
            productService.createInSpecificCollWithoutPathVarNoDuplicate(product2).subscribe();
            productService.createInSpecificCollWithoutPathVarNoDuplicate(product3).subscribe();
            productService.createInSpecificCollWithoutPathVarNoDuplicate(product4).subscribe();
//            productService.createInSpecificCollWithoutPathVarNoDuplicate(product5).subscribe();
            productService.createProductNoDuplicate(product5).subscribe(); // product5 saved to default coll
        };

    }

    // Create method for MOCK data (message + empty error if product already exists)
    // (Could also be in Service Method, with some slight changes)
//    public Mono<Product> createSnackNoDuplicateMock(Product product) {
//        return productRepository.existsByName(product.getName())
//                .flatMap(exists -> {
//                    if (exists) {
////                        logger.info(product.getName() + " already exist"); // Use logger
//                        System.out.println(product.getName() + " already exist");
//
//                        return Mono.empty(); // Compulsory return here, so set to empty
//
//                    } else {
//                        // Handle if date & time is null
//                        String creationDateTime;
//                        if (product.getCreationDateTimeString() == null) {
//                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Specify format
//                            String formattedDateTime = LocalDateTime.now().format(formatter); // Apply format
//                            creationDateTime = formattedDateTime;
//                        } else {
//                            creationDateTime = product.getCreationDateTimeString();
//                        }
//
//                        // uuid is provided in mock creation above
//                        Product tempSnack = new Product(product.getName(), product.getFlavour(), product.getWeight(),
//                                product.getProductId(), creationDateTime);
//
////                        logger.info(product.getName() + " created");
//                        System.out.println(product.getName() + " created");
//
//                        // Save each product passed into this method
//                        return productRepository.save(tempSnack);
//                    }
//                });
//    }


}
