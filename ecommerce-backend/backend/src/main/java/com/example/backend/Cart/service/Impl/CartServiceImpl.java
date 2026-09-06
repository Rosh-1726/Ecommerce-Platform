package com.example.backend.Cart.service.Impl;

import com.example.backend.Cart.dto.CartItemResponse;
import com.example.backend.Cart.dto.CartResponse;
import com.example.backend.Cart.entity.Cart;
import com.example.backend.Cart.entity.CartItem;
import com.example.backend.Cart.repository.CartItemRepository;
import com.example.backend.Cart.repository.CartRepository;
import com.example.backend.Cart.service.CartService;
import com.example.backend.Product.entity.Product;
import com.example.backend.Product.exception.*;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.auth.dto.Responses.MessageResponse;
import com.example.backend.auth.exception.UserNotFoundException;
import com.example.backend.entity.Users;
import com.example.backend.repository.UsersRepo;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UsersRepo usersRepository;



    /**
     * Add a product to the user's cart.
     *
     * If the user has no existing cart, a new cart is created automatically.
     * If the product already exists in the cart, its quantity is increased.
     *
     * @param userEmail the email of the user adding the product
     * @param productId the UUID of the product to add
     * @param quantity the quantity to add (defaults to 1 if null or <=0)
     * @return the updated {@link CartResponse} including all current items
     * @throws UserNotFoundException if the user email does not exist
     * @throws ProductNotFoundException if the product ID does not exist
     * @throws ProductOutOfStockException if the product is inactive or stock is insufficient
     */

    /*
    User Email + Product ID + Quantity
              ↓
       Quantity validate
              ↓
         Find User
              ↓
        Find Product
              ↓
      Product Active?
              ↓
        Stock available?
              ↓
       Find User's Cart
        ↙           ↘
   Cart exists     No Cart
      ↓               ↓
   continue       Create Cart
      ↓               ↓
      └──────→ Find existing CartItem
                       ↓
                Already exists?
                 ↙          ↘
               YES          NO
                ↓            ↓
        Increase quantity   Create CartItem
                ↓            ↓
             Save          Save
                 \          /
                  ↓        ↓
                 CartResponse

      “First, I validate the quantity and set it to 1 if it is null or
       invalid. Then I find the authenticated user using the email and
       fetch the requested product using its ID. I check whether the
       product is active and whether sufficient stock is available.
       After that, I find the user's cart, and if it doesn't exist,
       I create one. Then I check whether the product already exists
       in the cart. If it exists, I increase its quantity after checking
       stock again. Otherwise, I create a new CartItem with the cart,
        product and quantity. Finally, I save the changes and return the
         CartResponse.”


         1. Explain your Add to Cart flow.

हे नक्की विचारू शकतो.

“First, I validate the quantity, then find the user and product. I check whether the product is active and whether sufficient stock is available. Then I find or create the user's cart. After that, I check whether the product already exists in the cart. If it exists, I increase the quantity; otherwise, I create a new CartItem. Finally, I save it and return CartResponse.”

2. What happens if quantity is null or 0?
if (quantity == null || quantity <= 0) quantity = 1;

Answer:

“If quantity is null or less than or equal to zero, I set it to 1 as the default quantity.”

3. How do you identify which user's cart to update?

Answer:

“I get the logged-in user's email from the Authentication object and use that email to find the user. Then I find the cart associated with that user.”

Flow:

Authentication → Email → User → Cart

4. What happens if the product doesn't exist?

Answer:

“I throw a ProductNotFoundException.”

5. How do you check product availability?

तुझ्या code मध्ये दोन checks आहेत:

Active?
   ↓
Stock available?

Answer:

“First, I check whether the product is active. Then I check whether the available stock is sufficient for the requested quantity.”

6. What happens if the user doesn't have a cart?

Answer:

“If the cart doesn't exist, I create a new Cart, associate it with the user, initialize the items list and save it.”

7. How do you check whether the product is already in the cart?

तुझा code:

cart.getItems().stream()
    .filter(i -> i.getProduct().getId().equals(productId))
    .findFirst();

Answer:

“I iterate through the cart items and compare each CartItem's product ID with the requested product ID. If it matches, the product already exists in the cart.”

8. What happens if the product already exists in the cart?

Answer:

“I don't create a new CartItem. Instead, I add the requested quantity to the existing quantity and save the updated CartItem.”

Example:

Existing = 2
New = 3

2 + 3 = 5
9. What happens if the product is not already in the cart?

Answer:

“I create a new CartItem, set the cart, product and quantity, add it to the cart's items list and save it.”

10. Why do you check stock again when increasing quantity?

हा interviewer ला आवडणारा question आहे.

उदा.:

Stock = 5

Cart मध्ये already = 3
User adds = 3

New quantity = 6

जरी requested quantity 3 available असली तरी total cart quantity 6 झाली.

म्हणून:

“When the product already exists in the cart, I calculate the new total quantity and check it against the available stock. This prevents the cart quantity from exceeding the available stock.”

⭐ Medium-level Questions
11. Why do you use @Transactional?

“I use @Transactional so that the database operations performed during adding an item to the cart are handled within a single transaction.”

12. Why do you use CartItem instead of directly storing Product in Cart?

“CartItem represents a product entry in the cart and allows me to store additional information such as quantity. It also connects the Cart and Product.”

13. What does CartItem contain?

“CartItem contains the cart reference, product reference and quantity.”

cart
product
quantity
14. Why do you use ManyToOne between CartItem and Product?

“Multiple CartItems can reference the same Product, while each CartItem refers to one Product, so I use Many-to-One.”

15. Why do you use ManyToOne between CartItem and Cart?

“A cart can contain multiple CartItems, while each CartItem belongs to one Cart.”

16. Why do you use LAZY fetching?

“I use LAZY fetching so that the related Cart and Product entities are loaded only when they are actually needed.”

17. Why do you use CascadeType.ALL?

“It allows persistence operations on the Cart to be cascaded to its associated CartItems.”

18. What is orphanRemoval = true?

“When a CartItem is removed from the Cart's items collection, orphanRemoval allows that CartItem to be removed from the database as well.”

🔥 Tricky Questions
19. Why are you getting email from Authentication instead of taking userId from request?

“Because the authenticated user's identity should come from the security context rather than trusting a user ID sent by the client.”

हा answer strong आहे.

20. What if user tries to add more quantity than available stock?

“I check the requested quantity against the available stock and throw a ProductOutOfStockException if sufficient stock is not available.”

21. What if product is inactive but has stock?

“I still don't allow it to be added because I first check whether the product is active.”

22. What if the same product is added twice?

“I check whether the product already exists in the cart. If it exists, I increase its quantity instead of creating another CartItem.”

Example:

Lays × 2

Add Lays × 3

Result:
Lays × 5
23. Why don't you create a new CartItem when the product already exists?

“Because I want one CartItem entry for a product in a cart. When the same product is added again, I update its quantity instead of creating a duplicate entry.”

24. Why do you return CartResponse instead of Cart?

“I use CartResponse DTO to control the data exposed through the API and avoid directly exposing the entity.”
     */
    @Override
    @Transactional
    public CartResponse addToCart(String userEmail, UUID productId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;

        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + productId));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductOutOfStockException("Product is not active");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ProductOutOfStockException("Not enough stock for product " + product.getName());
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            return cartRepository.save(c);
        });

        // find existing item
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        // update quantity
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new ProductOutOfStockException("Not enough stock to increase quantity to " + newQty);
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
            cartRepository.save(cart);
        }
        return mapToCartResponse(cart);

    }





    /**
     * Update the quantity of a product in the user's cart.
     *
     * If the new quantity is <= 0, the item is removed from the cart.
     *
     * @param userEmail the email of the user updating the cart
     * @param productId the UUID of the product to update
     * @param quantity the new quantity for the product
     * @return the updated {@link CartResponse} including all current items
     * @throws UserNotFoundException if the user email does not exist
     * @throws ProductNotFoundException if the product is not in the cart
     * @throws ProductOutOfStockException if the new quantity exceeds product stock
     */


    /*
    🔄 Short Flow

START
↓
userEmail + productId + quantity मिळते
↓
User शोधतो
→ User नाही → UserNotFoundException
↓
User चा Cart शोधतो
→ Cart नाही → Cart not found
↓
Cart मध्ये Product शोधतो
→ Product नाही → ProductNotFoundException
↓
Quantity <= 0 किंवा null?

➡️ YES → Cart मधून item remove → DB मधून delete → Cart save → Response

➡️ NO → पुढे
↓
Stock check
→ Stock कमी → ProductOutOfStockException
↓
item.setQuantity(quantity)
↓
Save CartItem
↓
CartResponse बनवतो
↓
END

🧠 Interview मध्ये बोलायचं

“First, I find the authenticated user using the email. Then I retrieve the user's cart and find the requested product in the cart by comparing product IDs. If the new quantity is null or less than or equal to zero, I remove the item from the cart. Otherwise, I check whether sufficient stock is available, update the quantity, save the CartItem, and return the updated CartResponse.”

लक्षात ठेव फक्त हे:

User → Cart → CartItem → Quantity? → Remove OR Stock Check → Update → Save → Response

@Transactional चा simple answer:

“I used @Transactional because updating the cart involves multiple database operations, and I want them to be handled as one transaction.”
     */
    @Override
    @Transactional
    public CartResponse updateQuantity(String userEmail, UUID productId, Integer quantity) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found")); // or custom exception

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not found in cart: " + productId));

        // Update quantity (if the new quantity <=0 -> remove)
        if (quantity == null || quantity <= 0) {
            // remove
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
            return mapToCartResponse(cart);

        }

        Product product = item.getProduct();
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ProductOutOfStockException("Not enough stock for product " + product.getName());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return mapToCartResponse(cart);

    }
/*
🛒 Update Quantity — Short Pseudocode

START
↓
userEmail + productId + quantity मिळते
↓
User शोध
→ User नाही → UserNotFoundException
↓
User चा Cart शोध
→ Cart नाही → Cart not found
↓
Cart मध्ये Product शोध
→ Product नाही → ProductNotFoundException
↓
Quantity <= 0 किंवा null?

➡️ YES
→ Cart मधून Item remove
→ cartItemRepository.delete()
→ Cart save
→ CartResponse
→ END

➡️ NO
→ Product चा Stock check
→ Stock कमी → ProductOutOfStockException
↓
item.setQuantity(quantity)
↓
CartItem save
↓
mapToCartResponse()
↓
END

🧠 फक्त हे लक्षात ठेव:

User → Cart → Product → Quantity?

YES (0/null) → Remove → Delete → Save → Response

NO → Stock Check → Update Quantity → Save → Response
 */



    /**
     * Remove a product from the user's cart by productId.
     *
     * @param userEmail the email of the user removing the product
     * @param productId the UUID of the product to remove
     * @return the updated {@link CartResponse} including remaining items
     * @throws UserNotFoundException if the user email does not exist
     */    @Override
    @Transactional
    public CartResponse removeFromCart(String userEmail, UUID productId) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            cartRepository.save(cart);
        }

        return mapToCartResponse(cart);

    }





    /**
     * Get the current cart for the user.
     *
     * If the user has no cart, an empty cart is returned.
     *
     * @param userEmail the email of the user retrieving the cart
     * @return the {@link CartResponse} including all items and total price
     * @throws UserNotFoundException if the user email does not exist
     */

 /*
 🛒 Remove From Cart — Short Pseudocode

START
↓
userEmail + productId मिळते
↓
User शोध
→ User नाही → UserNotFoundException
↓
User चा Cart शोध
→ Cart नाही → Cart not found
↓
Cart मध्ये Product शोध
↓
Product exists?

➡️ NO
→ काहीही remove होत नाही
→ CartResponse return
→ END

➡️ YES
→ CartItem मिळवतो
→ Cart मधून Item remove
→ cartItemRepository.delete(item)
→ cartRepository.save(cart)
↓
mapToCartResponse(cart)
↓
END

🧠 फक्त हे लक्षात ठेव:

User → Cart → Product आहे?

NO → Direct Response

YES → Remove Item → Delete → Save → Response
  */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            c.setItems(new ArrayList<>());
            return c;
        });

        return mapToCartResponse(cart);

    }
    /*
    🛒 Get Cart — Short Pseudocode

START

↓

userEmail मिळते

↓

User शोध

→ User नाही → UserNotFoundException

↓

User चा Cart शोध

↓

Cart exists?

➡️ NO

→ New Cart object create
→ User set करतो
→ Empty items list set करतो
→ पुढे

➡️ YES

→ Existing Cart मिळतो

↓

mapToCartResponse(cart)

↓

CartResponse return

↓

END

🧠 फक्त हे लक्षात ठेव:

User → Cart आहे?

NO → New Empty Cart

YES → Existing Cart

दोन्ही cases → CartResponse → END

Important: इथे new cart save() केलेला नाही; फक्त empty Cart object तयार करून response बनवला जातो.
     */




    /**
     * Clear all items from the user's cart.
     *
     * @param userEmail the email of the user clearing the cart
     * @return a {@link MessageResponse} confirming the cart has been cleared
     * @throws UserNotFoundException if the user email does not exist
     */    @Override
    @Transactional
    public MessageResponse clearCart(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));
        cartRepository.findByUser(user).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
        return new MessageResponse(" Cart cleared successfully.");
    }

    //---------------------------------------------------mapping helpers--------------------------------------------------//

    /**
     * Helper method to map a {@link Cart} entity to {@link CartResponse} DTO.
     *
     * Calculates total price and maps each cart item to {@link CartItemResponse}.
     *
     * @param cart the cart entity to map
     * @return a {@link CartResponse} representing the cart state
     */
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = (cart.getItems() == null ? Collections.emptyList() : cart.getItems()).stream()
                .map(o -> {
                    CartItem i = (CartItem) o;
                    return CartItemResponse.builder()
                            .productId(i.getProduct().getId())
                            .productName(i.getProduct().getName())
                            .price(i.getProduct().getPrice())
                            .quantity(i.getQuantity())
                            .build();
                }).collect(Collectors.toList());


        BigDecimal total = (cart.getItems() == null ? Collections.emptyList() : cart.getItems()).stream()
                .map(o -> {
                    CartItem i = (CartItem) o;
                    BigDecimal price = i.getProduct().getPrice() == null ? BigDecimal.ZERO : i.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(i.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(total.doubleValue())
                .build();
    }
}
