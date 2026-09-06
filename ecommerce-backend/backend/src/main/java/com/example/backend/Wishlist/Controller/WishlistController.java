package com.example.backend.Wishlist.Controller;

import com.example.backend.Wishlist.dto.WishlistResponse;
import com.example.backend.Wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController                         //हा REST API controller आहे.
@RequestMapping("/api/v1/wishlist")     //सगळ्या wishlist APIs चा base URL.
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;



    /**
     * Add a product to the authenticated user's wishlist.
     *
     * @param auth current authenticated user
     * @param productId UUID of the product to add
     * @return the updated wishlist
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(Authentication auth, @PathVariable UUID productId) {          //URL मधून product ID घेतो.
        return ResponseEntity.ok(wishlistService.addToWishlist(auth.getName(), productId));
    }
/*
ResponseEntity = पूर्ण HTTP response represent करतो.
आणि <WishlistResponse> म्हणजे त्या response च्या body मध्ये WishlistResponse object असेल.
WishlistResponse
हा तुझा DTO आहे, जो wishlist ची माहिती client ला पाठवतो.

म्हणून simple भाषेत:
public ResponseEntity<WishlistResponse> म्हणजे हा public method HTTP response return करतो आणि त्या response मध्ये WishlistResponse DTO असतो.

Interview मध्ये विचारलं तर:
"ResponseEntity is used to return the HTTP response along with the response body and status code.
 Here, WishlistResponse represents the response body."

Authentication auth ==> यातून currently logged-in user मिळतो.
@PathVariable UUID productId ==> URL मधून product ID घेतो.
auth.getName() → logged-in user's email.
 */

    /**
     * Remove a product from the authenticated user's wishlist.
     *
     * @param auth current authenticated user
     * @param productId UUID of the product to remove
     * @return the updated wishlist
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(Authentication auth, @PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(auth.getName(), productId));
    }
    /*
    Authentication auth → currently logged-in user कोण आहे ते मिळवण्यासाठी.
    @PathVariable UUID productId → URL मधून कोणता product remove करायचा त्याचा ID.
    auth.getName()= Spring Security मधून logged-in user's email milavte
     */



    /**
     * Retrieve the authenticated user's wishlist.
     *
     * @param auth current authenticated user
     * @return the wishlist
     */    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication auth) {
        return ResponseEntity.ok(wishlistService.getWishlist(auth.getName()));
    }



    /**
     * Clear all items from the authenticated user's wishlist.
     *
     * @param authentication current authenticated user
     * @return the empty wishlist
     */
    @DeleteMapping("/clear")
    public ResponseEntity<WishlistResponse> clearWishlist(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(wishlistService.clearWishlist(email));
    }
}
/*
Authentication authentication👉 Spring Security कडून currently logged-in user ची information मिळते.
String email = authentication.getName();👉 authentication.getName() मधून logged-in user's email मिळतो.
 */
/*
Authentication auth 👉 Spring Security कडून currently logged-in user ची information मिळते.
auth.getName() 👉 त्यातून user चा email/username मिळतो.
Controller service ला सांगतो: "या logged-in user ची wishlist मला घेऊन दे."
ResponseEntity<WishlistResponse> 👉 API चा response WishlistResponse DTO मध्ये येणार आहे.
ResponseEntity मुळे HTTP status सुद्धा control करता येतो
 */
/*
1] Add wishlist
2]Remove wishlist
3]get all wishlist
4] clear wishlist


| Operation | Method | Endpoint                              |
| --------- | ------ | ------------------------------------- |
| Add       | POST   | `/api/v1/wishlist/add/{productId}`    |
| Remove    | DELETE | `/api/v1/wishlist/remove/{productId}` |
| Get       | GET    | `/api/v1/wishlist`                    |
| Clear     | DELETE | `/api/v1/wishlist/clear`              |

"I have implemented four wishlist APIs: add, remove, get and clear. The controller receives
the request and delegates the business logic to the WishlistService. I use Authentication to
identify the currently logged-in user, and auth.getName() gives the user's email. For add and
 remove operations, the product UUID is received using @PathVariable. Finally, the service
  returns a WishlistResponse, which is wrapped inside ResponseEntity."
 */