package com.example.backend.Product.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend.Category.entity.Category;
import com.example.backend.Category.exception.CategoryNotFoundException;
import com.example.backend.Category.repository.CategoryRepository;

import com.example.backend.Product.dto.ProductRequest;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.Product.entity.Product;
import com.example.backend.Product.exception.*;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.Product.service.ProductService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import com.example.backend.entity.Users;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UsersRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

     private final ProductRepository repo;
     private final CategoryRepository categoryRepo;
     private final Cloudinary cloudinary;
     private final UsersRepo usersRepository;




    /**
     * Create a new product.
     *
     * Validates product name uniqueness, category existence, and sets the authenticated user as seller.
     * Optionally uploads a product image to Cloudinary.
     *
     * @param request the product data
     * @param file optional product image file
     * @param sellerEmail the email of the authenticated seller
     * @return ProductResponse containing created product details
     * @throws IOException if an error occurs while uploading the image
     */

    /*
    "When the seller sends a create-product request, the controller passes the ProductRequest,
    image file and authenticated seller's email to the service. First, I trim the product name
    and check for duplicate products. Then I validate the category ID and fetch the category
     from the database. I also fetch the logged-in seller using their email. After that,
     I build the Product entity with the product details, category and seller. If an image
     is provided, I upload it to Cloudinary, get the secure URL and set that URL in the
     Product entity. Finally, I save the Product using the repository and map the saved
     entity to ProductResponse DTO."
     */
    @Override       //"@Override indicates that this method is implementing a method declared in the ProductService interface."
    public ProductResponse createProduct(ProductRequest request, MultipartFile file ,String sellerEmail ) throws IOException {  //ProductRequest request ==Controller मधून आलेला DTO.,MultipartFile file Controller मधून आलेली product image,String sellerEmail Controller मध्ये:String sellerEmail = authentication.getName();केलं होतं.म्हणजे currently logged-in seller चा email Service ला आला.
        String name = request.getName().trim(); //ProductRequest मधून product name घेतो. Beginning आणि ending मधले unnecessary spaces remove करतो.

        //duplicate product check kru
        /*
        "Before creating a product, I check whether a product with the same name already exists. This prevents duplicate products from being created."
        या नावाचा product आधीपासून आहे का?" existsByNameIgnoreCase() नावावरूनच समजतं:

existsBy → record आहे का?
Name → product name वर check
IgnoreCase → uppercase/lowercase ignore
         */

        if (repo.existsByNameIgnoreCase(name)) {
            throw new ProductAlreadyExistsException("Product with this name already exists.");
        }

        // category validation
        Long categoryId = request.getCategoryId();  //ProductRequest मधून category ID घेतो.
        if (categoryId == null) {                   //जर client ने category ID दिलीच नाही तर:
            throw new InvalidProductException("Category ID is required");
        }

        //आता आपण database मध्ये category शोधतो."I validate the category before creating the product so that the product always references an existing category."
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id : " + categoryId));

        // owner of the product validation, आता आपण logged-in seller database मध्ये शोधतो.
        Users seller = usersRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", sellerEmail));

        //आता actual Product Entity तयार करतो 🔥
        //इथे आपण Product Entity चा object तयार करत आहोत builder() हा Builder Pattern चा वापर आहे त्यामुळे object readable पद्धतीने तयार करता येतो.
        Product product = Product.builder()
                .name(name)                             //आपण वर घेतलेला name Product entity मध्ये set करतो.
                .description(request.getDescription())  //Request DTO मधून description घेतला आणि Product मध्ये set केला.
                .price(BigDecimal.valueOf(request.getPrice()))      //"I use BigDecimal for price because monetary values require accurate decimal precision.
                .stock(request.getStock())                          //Request मधला stock Product entity मध्ये set केला.
                .category(category)                                 //आधी database मधून पूर्ण Category entity शोधली.  आता ती Product मध्ये set केली.
                .seller(seller)     // set owner                   //Product कोणत्या seller चा आहे हे database मध्ये store होतं.   //Why did you associate seller with Product? "I associated each product with its seller so that we can identify who owns the product and implement ownership-based authorization for update and delete operations."
                .active(true)       // active by default
                .build();           //आत्तापर्यंत आपण Product object ची fields specify केली: name description price stock category ,seller ,active ,build() actual Product object तयार करतो.



        // Upload Product image to Cloudinary
        /*
        file exists?
             ↓
            YES
             ↓
        file empty?
              ↓
            NO
              ↓
            Upload
 */     //File object आहे  && File मध्ये content आहे
        if (file != null && !file.isEmpty()) {          //File object आहे का?   File आहे पण त्यात actual content आहे का? दोन्ही true असतील तरच image upload करतो.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),            //file.getBytes()=➡️ MultipartFile मधली image bytes मध्ये convert करतो.mhnje binary mdhe convert krto
                    ObjectUtils.asMap(
                            "folder", "Products",// folder name
                            "public_id", name, //file name
                            "overwrite", true, //same image name aadhi pasun asel tr tya image la replace kru shkto ya navin image ne
                            "resource_type", "image"    //file kontya format mdhe ahe image ahe ass
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            product.setImageUrl(imageUrl);
        }
        repo.save(product);
        return mapToResponse(product, "Product created successfully :<>:");//mapToResponse() चं काम म्हणजे:Product Entity->ProductResponse DTO मध्ये convert करणे.
    }
/*
file.getBytes() ➡️ MultipartFile मधली image bytes मध्ये convert करतो.

उदा.

User → image.jpg
          ↓
MultipartFile
          ↓
file.getBytes()
          ↓
image चे binary data

मग:

cloudinary.uploader().upload(...) ➡️ हे binary data Cloudinary वर upload करते.

3️⃣ Cloudinary ला settings देणे ObjectUtils.asMap( ➡️ Cloudinary upload करताना काही options/settings देण्यासाठी map तयार केला जातो.

आता एक-एक option:

4️⃣ Folder
"folder", "Products",

➡️ Cloudinary मध्ये image कोणत्या folder मध्ये ठेवायची ते सांगतो. म्हणजे Cloudinary मध्ये:
Products/
    product-image

असा structure तयार होईल.
5️⃣ Public ID
"public_id", name,   ➡️ Cloudinary वर uploaded image ला कोणते नाव/ID द्यायचे ते सांगतो.

इथे:
name  हा product चा name आहे.

उदा. product name: iPhone 15

तर Cloudinary मध्ये त्याचा public ID त्या नावावर आधारित असेल.

6️⃣ Overwrite
"overwrite", true,➡️ त्याच public_id ची image आधीपासून Cloudinary मध्ये असेल तर ती replace/overwrite करण्याची permission देतो.

उदा. पहिल्यांदा:  Products/iPhone15 upload झाली.

नंतर same public ID ने नवीन image upload केली.
"overwrite", true
असल्यामुळे जुनी image replace होऊ शकते.

7️⃣ Resource type
"resource_type", "image" ➡️ Cloudinary ला सांगतो की आपण upload करत असलेली file image आहे.

म्हणजे: resource_type = image

8️⃣ Cloudinary चा response
पूर्ण upload झाल्यावर: Map uploadResult = ...
➡️ Cloudinary आपल्याला एक Map response देते.
त्यात अनेक information असते, जसं:
public_id
secure_url
format
width
height
...

आपल्याला त्यातून मुख्यतः image URL पाहिजे.

9️⃣ Secure URL मिळवणे
String imageUrl = (String) uploadResult.get("secure_url");

इथे:
uploadResult.get("secure_url")

➡️ Cloudinary ने upload केलेल्या image चा HTTPS URL मिळतो.
उदा. https://res.cloudinary.com/.../Products/iPhone15.jpg

आणि:
(String)

➡️ मिळालेली value String मध्ये convert करतो.

म्हणून: String imageUrl = ...

मध्ये image चा URL store होतो.

1️⃣0️⃣ Product table object मध्ये URL set करणे
product.setImageUrl(imageUrl);

➡️ Product object मध्ये Cloudinary चा URL ठेवतो.
आता product object roughly असा आहे:

Product
 ├── name = "iPhone 15"
 ├── price = 70000
 ├── category = ...
 └── imageUrl = "https://res.cloudinary.com/..."

महत्त्वाचं: इथे actual image database मध्ये save होत नाही. Database मध्ये फक्त Cloudinary image URL save होतो.

1️⃣1️⃣ Database मध्ये Product save
repo.save(product);

आता Product object database मध्ये save होतो.

त्यात:

name
price
category
seller
imageUrl
...

सगळे fields save होतात.
िशेषतः: imageUrl मध्ये Cloudinary चा URL save झालेला असतो.

1️⃣2️⃣ Response return
return mapToResponse(product, "Product created successfully :<>:");

➡️ Database मध्ये product save झाल्यानंतर ProductResponse तयार करून client/controller ला return करतो.

mapToResponse() चं काम म्हणजे:

Product Entity
      ↓
ProductResponse DTO

मध्ये convert करणे.


Flow execution
Controller
   ↓
createProduct(request, file, sellerEmail)
   ↓
1. Product name घ्या + trim करा
   ↓
2. Duplicate product check
   ↓
3. categoryId घ्या
   ↓
4. categoryId null आहे का check
   ↓
5. Database मधून Category शोधा
   ↓
6. sellerEmail वरून logged-in Seller/User शोधा
   ↓
7. Product Entity तयार करा
   ↓
8. Image असेल तर Cloudinary वर upload करा
   ↓
9. Cloudinary मधून secure_url घ्या
   ↓
10. Product मध्ये imageUrl set करा
   ↓
11. Product database मध्ये save करा
   ↓
12. Product → ProductResponse
   ↓
13. Response Controller कडे
 */



    /**
     * Get a product by its UUID.
     *
     * Validates that the product is active and in stock.
     *
     * @param id the UUID of the product
     * @return ProductResponse with product details
     * @throws ProductNotFoundException if product does not exist
     * @throws ProductInactiveException if product is inactive
     * @throws ProductOutOfStockException if product stock is zero
     */
    @Override  //@Override → हा method ProductService interface मध्ये declare केलेला आहे, त्याची implementation इथे आहे.
    public ProductResponse getProductById(UUID id) { //ProductResponse → शेवटी frontend ला ProductResponse DTO मिळणार.
        Product product = repo.findById(id)         //दिलेल्या UUID चा product database मध्ये शोध.
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));


        //product Active or not
        if (Boolean.FALSE.equals(product.getActive())) {        //इथे product.getActive() मधून product active आहे की नाही ते मिळते.active = true  → Product active आहे //active = false → Product inactive आहे

            throw new ProductInactiveException("Product is not active right now.");
        }

        //Stock valueच नाही., Stock 0 किंवा negative आहे.
        if (product.getStock() == null || product.getStock() <= 0) {
            throw new ProductOutOfStockException("Sorry, product is out of stock.");
        }
        return mapToResponse(product, "Product found successfully :D "); //आपल्याकडे Product Entity आहे. पण frontend ला आपण ProductResponse DTO पाठवतो.mapToResponse() product ची required information घेऊन ProductResponse तयार करतो.
    }

    /*
    GET /products/{id}
        ↓
Controller
        ↓
getProductById(UUID id)
        ↓
repo.findById(id)
        ↓
Product सापडला?
   ↓             ↓
 No             Yes
 ↓               ↓
Exception    active check
                  ↓
             inactive?
              ↓       ↓
             Yes      No
              ↓       ↓
          Exception  stock check
                         ↓
                    stock available?
                     ↓          ↓
                    No         Yes
                    ↓           ↓
                Exception   mapToResponse()
                                ↓
                         ProductResponse
                                ↓
                           Controller
                                ↓
                         Frontend

 "First, I fetch the product by UUID using findById(). If the product is not found,
 I throw a ProductNotFoundException. Then I check whether the product is active and
 whether stock is available. If either condition fails, I throw the respective exception.
 Finally, I convert the Product entity into a ProductResponse DTO and return it."
     */




    /**
     * Get all products.
     *
     * @return list of ProductResponse objects
     */
    @Override   //@override ==>हा getAllProducts() method ProductService interface मध्ये declare केलेला आहे आणि आपण त्याची implementation इथे ProductServiceImpl मध्ये करत आहोत.
    public List<ProductResponse> getAllProducts() {     //List<productResponse>म्हणजे method आपल्याला multiple ProductResponse objects ची List return करणार आहे.
        List<Product> products = repo.findAll();        //Repository database ला query करते आणि सगळे Product records आणते.
        if (products.isEmpty()) {
            return Collections.emptyList();
            //throw new ProductNotFoundException("No Products Found :( ");
        }
        List<ProductResponse> responseList = new ArrayList<>();

        for (Product p : products) {
            ProductResponse response =
                    mapToResponse(p, "Products found successfully :D "); //Product Entity (p) ला ProductResponse DTO मध्ये convert करणे.

            responseList.add(response);
        }

        return responseList;
    }



    /**
     * Update an existing product.
     *
     * Only allowed for the product owner or admin. Validates updates and optionally uploads a new image.
     *
     * @param id the UUID of the product to update
     * @param request the updated product data
     * @param file optional new product image
     * @param sellerEmail the email of the authenticated user
     * @return ProductResponse containing updated product details
     * @throws IOException if an error occurs while uploading the image
     * @throws ProductOwnershipException if user is not owner/admin
     * @throws ProductNotFoundException if product does not exist
     */

    @Override //ProductService interface मध्ये updateProduct() method आहे. त्याची implementation इथे आहे.
    @Transactional  //या method मधले DB operations एका transaction मध्ये execute होतात.
    public ProductResponse updateProduct(UUID id, ProductRequest request , MultipartFile file, String sellerEmail) throws IOException {  //id          → कोणता product update करायचा// request     → name, description, price, categoryId, stock //file        → नवीन product image  //sellerEmail → logged-in seller कोण आहे

        //Step 1 : Fetch the product by its ID
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));

        // OWNER check: allow if product.seller.email == sellerEmail OR if user has ADMIN role
        //product kontya seller cha ahe to email milvto ani current logged in seller cha email barobr compare krto
        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail) && !isCurrentUserAdmin()) {
            throw new ProductOwnershipException("You are not the owner of this product");
        }

        // update name if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) { //trim() extra spaces काढतो. , isEmpty()check करतो की string रिकामी आहे का.
            String newName = request.getName().trim(); //Request मधला name घेतो आणि spaces remove करतो.

           //3. Duplicate Name Check
            if (!product.getName().equalsIgnoreCase(newName) && repo.existsByNameIgnoreCase(newName)) {
                throw new ProductAlreadyExistsException("Product with this name already exists.");
            }
            product.setName(newName);
        }

        // update description if provided
        // description
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) { //request.getDescription()==>Request मधून description घेते.//.trim()==>Description च्या सुरुवातीचे आणि शेवटचे spaces काढतो. //.isEmpty()==> String पूर्णपणे empty आहे का ते check करतो.//! म्हणजे NOT / उलट.
            product.setDescription(request.getDescription().trim()); //Product object मध्ये नवीन description ठेवतो

        }

        // update price if provided
        if (request.getPrice() != null) { //request मध्ये नवीन price दिली आहे ti null nahi pahije
            if (request.getPrice() < 0) {
                throw new InvalidProductException("Price cannot be negative");
            }
            product.setPrice(BigDecimal.valueOf(request.getPrice()));//product chi price bigDecimal mdhe Convert krun Product chi price update kra
        }
        /*
        "जर नवीन price दिली असेल, तर ती negative नाही ना हे validate कर. Valid असेल तर
        BigDecimal मध्ये convert करून Product ची price update कर."
         */

        // update stock if provided
        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new InvalidProductException("Stock cannot be negative");
            }
            product.setStock(request.getStock());
        }

        // update category if provided
        if (request.getCategoryId() != null) { //user ne navin category dili ahe ka ,
            Category category = categoryRepo.findById(request.getCategoryId())    //categoryId ची actual Category database मधून शोधतो.
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id : " + request.getCategoryId()));
            product.setCategory(category);      //Product ची जुनी category काढून नवीन category set करतो.
        }

        // update image if provided
        //→ file आली आहे का? && file रिकामी नाही ना?
        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), //file.getBytes() म्हणजे uploaded image चा actual data/bytes घेतो. //uploadResult मध्ये Cloudinary कडून आलेला result store होतो.
                    ObjectUtils.asMap(
                            "folder", "Products",
                            "public_id", product.getName(),     //Image चा Cloudinary मधला identifier म्हणून product चे name वापरतो.
                            "overwrite", true,
                            "resource_type", "image"            //त्याच public_id ची image आधी असेल तर नवीन image ने replace/overwrite करण्याची permission देतो.
                    )
            );
            String imageUrl = (String) uploadResult.get("secure_url");//Cloudinary upload झाल्यावर आपल्याला image ची URL मिळते.
            product.setImageUrl(imageUrl);      //ती URL imageUrl मध्ये store करतो.Product entity मध्ये नवीन image URL set करतो.
        }

        repo.save(product); // persist changes
        return mapToResponse(product, "Product updated successfully");  //Product entity ला ProductResponse DTO मध्ये convert करतो.
    }

    /*
    Client
  ↓
PUT /products/{id}
  ↓
Controller
  ↓
Authentication + Authorization
  ↓
ProductRequest DTO
  ↓
Service
  ↓
Find Product by UUID
  ↓
Ownership Check
  ↓
Update Name
  ↓
Update Description
  ↓
Update Price + Validation
  ↓
Update Stock + Validation
  ↓
Update Category
  ↓
Upload Image to Cloudinary
  ↓
Update Image URL
  ↓
repo.save(product)
  ↓
mapToResponse()
  ↓
ProductResponse
  ↓
Controller
  ↓
200 OK

"For product update, the client sends a PUT request with the product
UUID and the fields that need to be updated. The controller validates
the user's role and creates a ProductRequest DTO. In the service layer,
 I first fetch the product and verify that the logged-in seller owns it
 , unless the user is an admin. Then I update only the fields provided
  in the request, with validations for price and stock. If a new
   category is provided, I validate that the category exists. If a new
   image is provided, I upload it to Cloudinary and store the secure
   URL in the database. Finally, I save the updated product and return
    a ProductResponse DTO."
     */


    /**
     * Delete a product by UUID.
     *
     * Only allowed for the product owner or admin.
     *
     * @param id the UUID of the product
     * @param sellerEmail the email of the authenticated user
     * @return MessageResponse confirming deletion
     * @throws ProductOwnershipException if user is not owner/admin
     * @throws ProductNotFoundException if product does not exist
     */
    @Override
    public MessageResponse deleteProduct(UUID id , String sellerEmail)  {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + id));

        //current logged in user and seller chi email match hoto trch delete hoil kiva  current logged in admin pahije
        if (!product.getSeller().getEmail().equalsIgnoreCase(sellerEmail) && !isCurrentUserAdmin()) {
            throw new ProductOwnershipException("You are not allowed to delete this product");
        }
        repo.deleteById(id);
        return new MessageResponse("Product deleted successfully");
    }
/*
"For deleting a product, I use a DELETE endpoint with the product UUID. Only sellers and admins are authorized. I get the logged-in user's email from Authentication and pass it along with the product ID to the service layer, where the ownership and deletion logic is handled. Finally, I return a success message with HTTP 200."
 */

    //---------------------------------------------------helper mapper--------------------------------------------------//

    // --- mapToResponse --- //
    private ProductResponse mapToResponse(Product p, String message) {
        Long categoryId = null;
        String categoryName = null;
        if (p.getCategory() != null) {
            categoryId = p.getCategory().getId();
            categoryName = p.getCategory().getName();
        }

        return ProductResponse.builder()
                .message(message)
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .stock(p.getStock())
                .active(p.getActive())
                .imageUrl(p.getImageUrl())
                .build();
    }


    // --- isCurrentUserAdmin --- //
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
