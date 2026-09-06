package com.example.backend.Wishlist.service.Impl;


import com.example.backend.Product.entity.Product;
import com.example.backend.Product.exception.ProductNotFoundException;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.Wishlist.exception.WishlistNotFoundException;
import com.example.backend.auth.exception.UserNotFoundException;
import com.example.backend.entity.Users;
import com.example.backend.repository.UsersRepo;
import com.example.backend.Wishlist.entity.Wishlist;
import com.example.backend.Wishlist.repository.WishlistRepository;
import com.example.backend.Wishlist.dto.WishlistResponse;
import com.example.backend.Wishlist.dto.ProductDto;
import com.example.backend.Wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UsersRepo usersRepository;




    /**
     * Add a product to the user's wishlist.
     *
     * @param userEmail the email of the authenticated user
     * @param productId UUID of the product to add
     * @return the updated wishlist
     */
    @Override                                //Interface मधल्या addToWishlist() method ला implement करतोय.
    @Transactional                           //या method मधली database operations एका transaction मध्ये execute होतात.
    public WishlistResponse addToWishlist(String userEmail, UUID productId) {   //userEmail → कोणाच्या wishlist मध्ये add करायचं? //productId → कोणता product add करायचा?  //WishlistResponse → updated wishlist client ला मिळेल.

        //इथे database मधून email वापरून user शोधतो.
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        //आता दिलेल्या productId वरून product database मधून शोधतो.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + productId));

        //या user ची wishlist आधीपासून आहे का? //जर wishlist सापडली नाही, तर खालील code execute होईल.
        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = new Wishlist();                             //नवीन Wishlist object तयार केला.
            w.setUser(user);                                         //ही wishlist त्या user सोबत associate केली.User → Wishlist
            w.setProducts(new ArrayList<>());                        //नवीन wishlist मध्ये initially empty product list ठेवली.
            return wishlistRepository.save(w);                       //नवीन wishlist database मध्ये save केली.
        });

        // add if not present
        boolean exists = false;

        for (Product p : wishlist.getProducts()) {      //Wishlist मधल्या p product चा ID आणि आपण add करत असलेल्या product चा ID same आहे का?

            if (p.getId().equals(product.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {                                      //!exists म्हणजे product wishlist मध्ये नाही.
            wishlist.getProducts().add(product);            //wishlist.getProducts() → wishlist मधली products ची list मिळते.
            wishlistRepository.save(wishlist);              //.add(product) → नवीन product त्या list मध्ये add करतो.
        }

        return mapToWishlistResponse(wishlist);
    }

    /*
    frontend = login user , product id ghetli
    login user la email gheun ghyaycha
    service mdhe
    = check kraych ha user exist ahe ka database mdhe nahitr exception user not found
    =user exist ahe mg dilela product exist ahe ka database mdhe nahitr exception product not found
    = user chi wishlist shodhaychi mhnje ya user chi wishlist database mdhe ahe ka
    = wishlist exist nasel tr new wishlist object create kru , wishlist current user sathi banvt ahot mg tyala set kru
    = user = wishlist
    = Wishlist → []
    survatila empty product list set kru mg database mdhe new wishlist la save kru
    = check kru ata product , already wishlist mdhe ahe ka  boolean exists = false;
    = for loop ne aapn , wishlist mdhil saglya products la compare kru with current product
    = jr equale aala tr exist la true set kru and loop stop
    = jr product nasla wishlist mdhe tr add kru
    if (!exists) {
        wishlist.getProducts().add(product);
Product wishlist च्या list मध्ये add करतो.
wishlistRepository.save(wishlist);

आता updated wishlist database मध्ये save होते.
म्हणजे product फक्त Java list मध्ये नाही, database मध्येही persist होतो.
return mapToWishlistResponse(wishlist);

🔥 आता FULL FLOW एकदाच बघ

User clicks ❤️ Add to Wishlist
              ↓
POST /api/v1/wishlist/add/{productId}
              ↓
WishlistController
              ↓
auth.getName() → userEmail
              ↓
WishlistService
              ↓
Find User by email in database = yes  || No (Exception user not found)
              ↓
Find Product by UUID = Yes || No ( Exception Product not found)
              ↓
Find Wishlist by User
              ↓
       Wishlist exists?
          /        \
        YES         NO
         ↓           ↓
  Use existing   Create Wishlist
                     ↓
                 Set User
                     ↓
                Empty List
                     ↓
                   Save
          \          /
           \        /
            ↓      ↓
      Check Product Exists
              ↓
       Already present?
          /        \
        YES         NO
         ↓           ↓
    Do nothing    Add Product
                      ↓
                    Save
                      ↓
          mapToWishlistResponse()
                      ↓
             WishlistResponse
                      ↓
                 Controller
                      ↓
                    User


### ❤️ Add Product to Wishlist — Interview Answer

> "When a user wants to add a product to the wishlist, the request first comes to the WishlistController with the product UUID.
 I get the currently authenticated user's email using `Authentication`.
>
> Then the controller passes the user email and product ID to the WishlistService.
In the service layer, I first validate that the user exists and then check whether the product exists.
>
> After that, I find the wishlist of that user. If the wishlist doesn't exist,
I create a new wishlist for that user.
>
> Before adding the product, I check whether the product is already present in the wishlist
to prevent duplicates. If it is not present, I add the product and save the updated wishlist.
>
> Finally, I convert the wishlist entity into a `WishlistResponse` DTO and return it to the client."

     */



    /**
     * Remove a product from the user's wishlist.
     *
     * @param userEmail the email of the authenticated user
     * @param productId UUID of the product to remove
     * @return the updated wishlist
     */
    @Override       //👉 Interface मध्ये असलेली removeFromWishlist() method इथे implement केली आहे.
    @Transactional  //👉 या method मधल्या database operations एकाच transaction मध्ये execute होतात.
    public WishlistResponse removeFromWishlist(String userEmail, UUID productId) {

       //👉 आधी email वरून logged-in user database मध्ये आहे का ते check करतो.
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        //👉 त्या user ची wishlist शोधतो.Wishlist नसेल तर exception.
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        //in short for loop samjat ithe pratek product wishlist mdhla compare hoil product id shi , jr equale sapla tr remove kraycha
        wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
        wishlistRepository.save(wishlist);

        return mapToWishlistResponse(wishlist);
    }


/*
🗑️ Remove Wishlist — Complete Deep Flow
1. User clicks "Remove from Wishlist"
             ↓
2. Frontend sends DELETE request
   DELETE /api/v1/wishlist/remove/{productId}
             ↓
3. Request reaches WishlistController
             ↓
4. @DeleteMapping("/remove/{productId}")
   identifies this endpoint
             ↓
5. @PathVariable UUID productId
   gets product ID from URL
             ↓
6. Authentication auth
   identifies currently logged-in user
             ↓
7. auth.getName()
   gets logged-in user's email
             ↓
8. Controller calls Service
   removeFromWishlist(userEmail, productId)
             ↓
9. WishlistService receives:
      userEmail
      productId
             ↓
10. Find User using email
    usersRepository.findByEmail(userEmail)
             ↓
11. User found?
      ├── NO → UserNotFoundException
      └── YES → continue
             ↓
12. Find Wishlist of that user
    wishlistRepository.findByUser(user)
             ↓
13. Wishlist found?
      ├── NO → Wishlist not found exception
      └── YES → continue
             ↓
14. Get products from wishlist
    wishlist.getProducts()
             ↓
15. Check each product
    Compare:
    existingProduct.getId()
             with
    productId received from request
             ↓
16. ID matches?
      ├── NO → keep checking
      └── YES → remove that product
             ↓
17. Save updated Wishlist
    wishlistRepository.save(wishlist)
             ↓
18. Convert Wishlist Entity
    → WishlistResponse DTO
             ↓
19. Service returns WishlistResponse
             ↓
20. Controller wraps response
    ResponseEntity.ok(...)
             ↓
21. HTTP 200 OK + updated WishlistResponse
             ↓
22. Frontend receives updated wishlist

"When a user wants to remove a product from the wishlist, the request first comes to the WishlistController with the product ID. The controller gets the authenticated user's email using Authentication and passes the email and product ID to the service layer.

In the service layer, I first find the user using their email. If the user doesn't exist, I throw a UserNotFoundException. Then I find the wishlist associated with that user. If the wishlist doesn't exist, I throw an exception.

After that, I check the products present in the wishlist and compare each product's ID with the product ID received in the request. If the IDs match, I remove that product from the wishlist.

Then I save the updated wishlist to the database. Finally, I convert the updated wishlist into a WishlistResponse DTO and return it to the controller."
 */
    /**
     * Retrieve the wishlist of a user.
     *
     * @param userEmail the email of the authenticated user
     * @return the wishlist
     */
    @Override                           //👉 Interface मध्ये getWishlist() method define केलेली आहे आणि इथे तिची implementation केली आहे.
    @Transactional(readOnly = true)     //👉 हा method database मधून फक्त data read/fetch करतो.इथे आपण database मध्ये काही update/insert/delete करत नाही, म्हणून readOnly = true.
    public WishlistResponse getWishlist(String userEmail) {     //👉 Method ला logged-in user's email मिळतो.

        //पहिल्यांदा email वरून user शोधतो.
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + userEmail));

        //"या user ची wishlist database मधून आण."  //जर त्या user ची wishlist database मध्ये नसेल, तर exception throw करण्याऐवजी empty wishlist तयार करतो.
        Wishlist wishlist = wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setProducts(new ArrayList<>());
            return w;
        });

        return mapToWishlistResponse(wishlist);
    }
/*
"For fetching the wishlist, I use a read-only transaction because this operation only reads
 data. First, I find the user using their email. If the user doesn't exist, I throw a
 UserNotFoundException. Then I find the wishlist associated with that user. If the
 wishlist doesn't exist, I create an empty wishlist object for that user with an empty
 product list. Finally, I map the wishlist entity to a WishlistResponse DTO and return
 it to the controller."

🧠 Short memory trick

Email → Find User → Find Wishlist → If absent create empty wishlist → Map to DTO → Return
🧠 Complete Deep Flow
GET /api/v1/wishlist
        ↓
WishlistController
        ↓
Authentication
        ↓
auth.getName()
        ↓
User Email
        ↓
wishlistService.getWishlist(userEmail)
        ↓
Find User by Email
        ↓
User found?
   ├── NO → UserNotFoundException
   └── YES
          ↓
   Find Wishlist by User
          ↓
   Wishlist found?
      ├── YES
      │    ↓
      │  Get existing Wishlist
      │
      └── NO
           ↓
      Create empty Wishlist object
           ↓
      Set User
           ↓
      Set Products = empty list
           ↓
      Return Wishlist object
           ↓
mapToWishlistResponse()
           ↓
WishlistResponse DTO
           ↓
Controller
           ↓
ResponseEntity.ok()
           ↓
HTTP 200 OK
 */


    /**
     * Clear all products from the user's wishlist.
     *
     * @param userEmail the email of the authenticated user
     * @return the empty wishlist
     */
    @ Override      //👉 Interface मध्ये define केलेली clearWishlist() method इथे implement केली आहे.
    public WishlistResponse clearWishlist(String userEmail) {       //👉 Method ला logged-in user's email मिळतो.
        Users user = usersRepository.findByEmail(userEmail)         //👉 Email वापरून database मधून user शोधतो.
                .orElseThrow(() -> new UserNotFoundException("User not found"));
/*
userEmail
   ↓
usersRepository.findByEmail()
   ↓
User found?
 ├── NO  → UserNotFoundException
 └── YES → पुढे
 */
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found"));
/*
User
 ↓
findByUser(user)
 ↓
Wishlist found?
 ├── NO  → WishlistNotFoundException
 └── YES → पुढे
 */
        wishlist.getProducts().clear();     //👉 wishlist.getProducts() म्हणजे wishlist मधली products ची list.//.clear() केल्यावर:sagle products clear hotil
        wishlistRepository.save(wishlist);  //👉 Products clear केल्यानंतर updated wishlist database मध्ये save करतो.

        return mapToWishlistResponse(wishlist); //👉 Updated Wishlist entity ला WishlistResponse DTO मध्ये convert करून return करतो.
    }

    /*
    "For clearing the wishlist, I first find the user using the authenticated user's email.
     Then I fetch the wishlist associated with that user. If the user or wishlist is not
      found, I throw the respective exception. Once the wishlist is found, I use the
      clear() method on the products list to remove all products from the wishlist.
       Then I save the updated wishlist and convert it into a WishlistResponse DTO before
        returning it to the controller."

        DELETE /api/v1/wishlist/clear
              ↓
      WishlistController
              ↓
   Authentication
              ↓
     Get user email
              ↓
 WishlistService.clearWishlist(email)
              ↓
       Find User by email
              ↓
        User found?
       /           \
     NO             YES
     ↓               ↓
 Exception      Find Wishlist
                     ↓
              Wishlist found?
               /          \
             NO            YES
             ↓              ↓
         Exception      Get Products
                              ↓
                         .clear()
                              ↓
                    All products removed
                              ↓
                      Save Wishlist
                              ↓
                 Map Entity → DTO
                              ↓
                    WishlistResponse
                              ↓
                       Controller
                              ↓
                        200 OK
     */
    //---------------------------------------------------mapping helpers--------------------------------------------------//

    // --- mapToWishlistResponse --- //
    private WishlistResponse mapToWishlistResponse(Wishlist w) {

        List<ProductDto> products = (w.getProducts() == null ? Collections.emptyList() : w.getProducts())
                .stream()
                .map(o -> {
                    Product p = (Product) o;
                    return ProductDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .price(p.getPrice() != null ? p.getPrice().doubleValue() : 0.0)
                            .imageUrl(p.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .wishlistId(w.getId())
                .products(products)
                .build();
    }

}
