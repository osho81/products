package lia.practice.products.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

// Annotation declaring this as a mongoDB entity to create records from,
// and value is name of the collection
@Document (value = "products")
//@Document // Better without value, when creating multiple collections
public class Product {

    @Id
    private UUID id; // If UUID in ProductRepository
    // private String id; // If String in ProductRepository

    private UUID orgId; // used to create specific collections

    private UUID productId;

    private String name;

    private String description;

    private double weight;

//    private LocalDateTime creationDateTime;

    private String creationDateTimeString;

    // Empty constructor
    public Product() {
    }

    // Constructor without productId
    public Product(String name, String description, double weight) {
//        this.id = id;
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    // Constructor including productId as well
    public Product(String name, String description, double weight, UUID productId) {
        this.id = UUID.randomUUID(); // Example using db generated UUID
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.productId = productId; // Example assigning provided UUID
    }

    // Constructor including DateTime as well
    public Product(String name, String description, double weight, UUID productId, String creationDateTimeString) {
        this.id = UUID.randomUUID(); // Example using db generated UUID
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.productId = productId; // Example assigning provided UUID
//        this.creationDateTime = creationDateTime.withNano(0); // Remove nano seconds
//        this.creationDateTime = creationDateTime.truncatedTo(ChronoUnit.SECONDS); // Remove nano seconds
        this.creationDateTimeString = creationDateTimeString;
    }

    // Constructor including orgId as well
    public Product(UUID orgId, String name, String description, double weight, UUID productId, String creationDateTimeString) {
        this.id = UUID.randomUUID();
        this.orgId = orgId; // orgId, used to create specific collection
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.productId = productId; // Example assigning provided UUID
        this.creationDateTimeString = creationDateTimeString;
    }


//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // orgId, used to create specific collection
    public UUID getOrgId() {
        return orgId;
    }
    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

//    public LocalDateTime getCreationDateTime() {
////        return creationDateTime.withNano(0);
//        return creationDateTime;
//    }
//
//    public void setCreationDateTime(LocalDateTime creationDateTime) {
////        this.creationDateTime = creationDateTime.withNano(0);
////        this.creationDateTime = creationDateTime.truncatedTo(ChronoUnit.SECONDS);
//        this.creationDateTime = creationDateTime;
//    }

    public String getCreationDateTimeString() {
        return creationDateTimeString;
    }

    public void setCreationDateTimeString(String creationDateTimeString) {
        this.creationDateTimeString = creationDateTimeString;
    }
}
