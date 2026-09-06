package com.example.backend.Product.entity;

import com.example.backend.Category.entity.Category;     //Product कोणत्या category मध्ये आहे
import com.example.backend.entity.Users;                //Product कोणत्या seller ने create केला
import jakarta.persistence.*;                            //यातून आपल्याला annotations मिळतात- @Entity ,@Table ,@Id  , @GeneratedValue ,@Column ,@ManyToOne ,@JoinColumn हे annotations Java class ला database table शी map करण्यासाठी वापरले जातात.
import lombok.*;                                         //@Getter , @Setter ,@Builder ,@NoArgsConstructor ,@AllArgsConstructor वापरता येतात.
import org.hibernate.annotations.CreationTimestamp;        //created time
import org.hibernate.annotations.UpdateTimestamp;        //updated time

import java.math.BigDecimal; //price
import java.time.LocalDateTime; //date/time
import java.util.UUID; //product id


@Entity                     //It tells Hibernate that this java class should be mapped to a database table."
@Table(name = "products")    //"I used @Table to explicitly specify the database table name as products."
@Getter                       //Automatically generates getter methods. Instead of manually writing: lombok generates it for us
@Setter
@Builder                    //Allows us to create objects using the Builder pattern. , mhnje jevdhe pn columns ahet tyana aapn dot. ne sagal kru shkto "I used Lombok's @Builder to create Product objects in a clean and readable way, especially because the entity contains multiple fields."
@NoArgsConstructor          //Generates a constructor with no arguments:
@AllArgsConstructor         //Generates a constructor containing all fields.
public class Product {

    @Id                     //Marks id as the Primary Key.
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)               //The database does not allow this field to be NULL.Value MUST pahije, "nullable = false ensures that every product must have a name."
    private String name;

    @Column(length = 1000)                  //length = 1000 specifies the maximum column length.
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)   //Scale = 2 , Number of digits after the decimal point.  , precision = 10 Total number of digits.
    private BigDecimal price;                               //"I used BigDecimal for monetary values because it provides precise decimal calculations and avoids floating-point precision issues."

    @Column(nullable = false)                               //nullable = false means stock cannot be NULL.
    private Integer stock;                                  // quantity available of the product

    @ManyToOne(fetch = FetchType.LAZY)                  //Many Products → One Category @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)  //It specifies the foreign-key column in the products table.
    private Category category;                          //"I used a Many-to-One relationship because multiple products can belong to the same category. I used @JoinColumn to create the category_id foreign-key column."
                                                        //FetchType.LAZY it means category data is not loaded immediately it is only loaded when it is needed
    // image URL (Cloudinary)
    private String imageUrl;                            //You are not storing the actual image inside the database."I used Cloudinary for product image storage. The uploaded image is sent to Cloudinary, and I store the returned secure URL in the Product entity."

    @Column(nullable = false)
    private Boolean active = true;                      //true = active , false = inactive

    // owner of the product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)           //Many products can belong to one seller.Many Products → One Seller
    private Users seller;                                          //“A product belongs to one seller, while a seller can have multiple products. seller_id is used as the foreign key.”

    @CreationTimestamp                                          //Hibernate automatically sets the creation timestamp.
    @Column(name = "created_at", updatable = false)             //updated=false means .Once created, the creation timestamp should not change.
    private LocalDateTime createdAt;

    @UpdateTimestamp                                            //This automatically stores the latest update time.
    @Column(name = "updated_at")                                //Whenever the product is updated, Hibernate updates this timestamp.
    private LocalDateTime updatedAt;

}


/* Theory
*
*                   PRODUCT
                       │
     ┌─────────────────┼─────────────────┐
     │                 │                 │
     ↓                 ↓                 ↓
 CATEGORY            SELLER            IMAGE
     │                 │                 │
category_id         seller_id        imageUrl
*
*
*
* Product Table COlumns
id
name
description
price
stock
category
imageUrl
active
seller
createdAt
updatedAt
*
*
* ⭐ Most Important Interview Questions from this Entity

Prepare these:

⭐ What is @Entity?
It marks the java class as a JPA entity that is mapped to a database table.

⭐Why @Id?
It marks the primary key of the entity.The primary key uniquely identifies each product in the database."

⭐Why UUID?
To generate unique identifiers without relying on sequential IDs.
* साधारण आपण ID असा ठेवू शकतो:
1
2
3
4

पण UUID मध्ये ID असा दिसतो:   550e8400-e29b-41d4-a716-446655440000

म्हणजे तो एक मोठा unique random-looking identifier असतो.

⭐Why BigDecimal for price?
It helps avoid floating-point precision issues that can occur with double or float."I used BigDecimal for the product price because it provides precise decimal calculations

⭐Why @ManyToOne with Category?
Because many products can belong to one category.

⭐Why @JoinColumn?
To specify the foreign-key column used for the relationship.
"@JoinColumn is used to specify the foreign-key column for a relationship. In my Product entity, I used category_id as the foreign key to establish the relationship between Product and Category."

⭐ Why FetchType.LAZY?
To avoid unnecessarily loading related entities until they are needed.
* "LAZY loads the related entity when it is needed, whereas EAGER loads it immediately along with the main entity."

⭐Why seller relationship?
To identify the owner of a product and perform seller-based authorization.
"I added the seller relationship to identify the owner of each product. I use this relationship to implement ownership-based authorization, so only the product owner or an admin can update or delete the product."

⭐Why Cloudinary?
Product images are stored in Cloudinary, while only the image URL is stored in the database.

⭐Why @CreationTimestamp and @UpdateTimestamp?
To automatically maintain product creation and last-update timestamps.
"I used @CreationTimestamp and @UpdateTimestamp to automatically maintain the creation and last-update timestamps of a product. Hibernate manages these values automatically."

* */