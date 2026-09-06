package com.example.backend.Cart.entity;

import com.example.backend.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne       //एका User कडे एक Cart आहे.
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    /*
    इथे user_id सांगतो:  हा Cart कोणत्या User चा आहे?

| id   | user_id | created_at       | updated_at       |
| ---- | ------- | ---------------- | ---------------- |
| C101 | U001    | 2026-09-03 10:00 | 2026-09-03 10:30 |
| C102 | U002    | 2026-09-03 11:00 | 2026-09-03 11:15 |

    @JoinColumn म्हणजे relationship जोडण्यासाठी कोणता column वापरायचा ते सांगतो.

    Relationship कुठे आहे?
USERS
┌──────────────┐
│ id           │
│ name         │
│ email        │
└──────┬───────┘
       │
       │ 1 : 1
       ↓
CARTS
┌────────────────┐
│ id             │
│ user_id   FK   │
│ created_at     │
│ updated_at     │
└───────┬────────┘
        │
        │ 1 : Many
        ↓
CART_ITEMS
┌────────────────┐
│ id             │
│ cart_id   FK   │
│ product_id FK  │
│ quantity       │
└────────────────┘
     */

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)      //“orphanRemoval = true ensures that a CartItem removed from the Cart is also removed from the database.”//cascade=cascadeType.All=Parent Cart वर केलेले persistence operations associated CartItems वरही लागू होऊ शकतात.
    private List<CartItem> items = new ArrayList<>();
/*
One Cart → Many CartItems
@OneToMany(mappedBy = "cart")
private List<CartItem> items;

🧠 एक example

समजा:

User:
U001 = Roshani

तिचा Cart:

Cart:
C101 → U001

आणि Cart मध्ये 3 products:

CartItems

I1 → C101 → Product P10 → quantity 2
I2 → C101 → Product P20 → quantity 1
I3 → C101 → Product P30 → quantity 3
Roshani
   ↓
Cart C101
   ↓
 ┌───────────────┐
 │ Lays     × 2  │
 │ Oreo     × 1  │
 │ KitKat   × 3  │
 └───────────────┘
 */

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
/*
My Cart entity represents a user's shopping cart. It has a UUID as the primary key and has a One-to-One
 relationship with User because each user has one cart. It also has a One-to-Many relationship with CartItem because a
  cart can contain multiple items. I used cascade and orphanRemoval for managing cart items. I also maintain createdAt and
  updatedAt timestamps using Hibernate annotations.”
 */