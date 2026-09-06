package com.example.backend.Cart.entity;

import com.example.backend.Product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    /*
    2️⃣ Cart सोबत relationship
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "cart_id", nullable = false)
private Cart cart;

याचा अर्थ:

अनेक CartItems एका Cart चे असू शकतात.

उदा.

Cart C101
   │
   ├── CartItem CI01
   ├── CartItem CI02
   └── CartItem CI03

म्हणून:

Many CartItems → One Cart

Database मध्ये:
cart_items
--------------------------------
id      cart_id
CI01    C101
CI02    C101
CI03    C101

इथे cart_id foreign key आहे.

@JoinColumn(name = "cart_id")

याचा अर्थ: cart_items table मधला cart_id हा Cart ला connect करतो.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;  // quantity of the product in the cart
}
/*
| id   | cart_id | product_id | quantity |
| ---- | ------- | ---------- | -------: |
| CI01 | C101    | P101       |        2 |
| CI02 | C101    | P102       |        1 |
| CI03 | C101    | P103       |        3 |

समज:

C101 = Roshani's Cart

P101 = Lays
P102 = Oreo
P103 = KitKat

तर:

Roshani
   ↓
Cart C101
   ↓
┌─────────────────────┐
│ Lays    × 2         │
│ Oreo    × 1         │
│ KitKat  × 3         │
└─────────────────────┘
⭐ FetchType.LAZY काय आहे?

तुझ्याकडे:

@ManyToOne(fetch = FetchType.LAZY)

याचा simple अर्थ:

CartItem fetch केला म्हणून त्याचा Cart/Product लगेच database मधून load करू नको; जेव्हा actual गरज असेल तेव्हा load कर.

उदा. फक्त CartItem ची माहिती पाहिजे:

id
quantity

तर Product ची पूर्ण information लगेच load करण्याची गरज नाही.

हे performance साठी useful आहे.

Interview answer:

“I used lazy fetching so that related Cart and Product entities are not loaded immediately unless they are actually needed.”

🧠 आता Cart + CartItem पूर्ण relationship
             USER
               │
             1 : 1
               ↓
              CART
               │
            1 : Many
               ↓
           CART_ITEM
           /       \
       Many:1      Many:1
         ↓           ↓
       CART        PRODUCT

Actual DB:

USERS
  │
  │ user_id
  ↓
CARTS
  │
  │ cart_id
  ↓
CART_ITEMS
  │
  │ product_id
  ↓
PRODUCTS
हे 4 points पक्के कर:
User → Cart = One-to-One
Cart → CartItem = One-to-Many
CartItem → Cart = Many-to-One
CartItem → Product = Many-to-One

आणि CartItem मध्ये actual product + quantity ठेवली जाते.

🎯 Interview मध्ये CartItem explain करायचा असेल तर:

“CartItem represents a product entry inside a cart. It has a Many-to-One relationship with Cart and Product. cart_id identifies which cart the item belongs to, product_id identifies the product, and quantity stores how many units of that product are added to the cart. I used lazy fetching for Cart and Product relationships.”
उदा. Cart मध्ये:

Cart
 ├── Lays × 2
 ├── Oreo × 1
 └── KitKat × 3

इथे प्रत्येक row एक CartItem आहे.

प्रत्येक CartItem ला unique UUID मिळतो.

उदा.

id
CI001
CI002
CI003
 */