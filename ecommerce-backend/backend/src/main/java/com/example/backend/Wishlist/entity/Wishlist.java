package com.example.backend.Wishlist.entity;

import com.example.backend.Product.entity.Product;
import com.example.backend.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity             //हा class database मधल्या wishlists table शी map होतो.
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wishlists")
public class Wishlist {
    @Id                                                 //@Id → Primary Key
    @GeneratedValue(strategy = GenerationType.UUID)     //@GeneratedValue → ID automatically generate होईल
    private UUID id;                                    //UUID → unique identifier
//"I used UUID as the primary key for the Wishlist entity to generate unique identifiers."

    @OneToOne                                       //एक User → एक Wishlist
    @JoinColumn(name = "user_id", nullable = false)     //user_id हा wishlists table मध्ये foreign key असेल.//nullable =false ==>म्हणजे Wishlist ला User असणं compulsory आहे.
    private Users user;

    /*
    "Each user has one wishlist, so I used a One-to-One relationship between User and Wishlist."
    User
     ↓
    Wishlist
       ↓
    Products

    user_id हा wishlists table मध्ये foreign key असेल.
     */


    @ManyToMany         //एका Wishlist मध्ये multiple products असू शकतात.//आणि एक Product multiple users च्या wishlists मध्ये असू शकतो.
    @JoinTable(
            name = "wishlist_products",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();
}
    /*
    एका Wishlist मध्ये multiple products असू शकतात.

आणि एक Product multiple users च्या wishlists मध्ये असू शकतो.
उदा.

Wishlist A → Product 1, Product 2
Wishlist B → Product 2, Product 3

म्हणून ManyToMany.
@JoinTable(
            name = "wishlist_products",
            joinColumns = @JoinColumn(name = "wishlist_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
Wishlist आणि Product यांच्यामध्ये एक third/junction table तयार होतो:

wishlist_products

wishlist_id | product_id
------------|-----------
UUID-1      | P1
UUID-1      | P2
UUID-2      | P2

म्हणजे actual products wishlists table मध्ये store होत नाहीत. त्यांचे relationships wishlist_products मध्ये store होतात.

joinColumns → Wishlist ची ID
inverseJoinColumns → Product ची ID

एका Wishlist मध्ये अनेक Products ठेवण्यासाठी List.

new ArrayList<>() केल्यामुळे सुरुवातीला empty list मिळते आणि null problem टाळता येतो.

User → Wishlist = OneToOne
Wishlist → Product = ManyToMany
ManyToMany साठी wishlist_products = Join Table
     */

