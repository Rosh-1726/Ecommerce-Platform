package com.example.backend.Product.controller;

import com.example.backend.Product.dto.ProductRequest;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.Product.service.ProductService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;               //@RestController@RequestMapping ,@GetMapping ,@PostMapping ,@PutMapping ,@DeleteMapping ,@RequestParam ,@RequestPart  ,@PathVariable
import org.springframework.web.multipart.MultipartFile;     //Image/file upload handle करण्यासाठी.
import java.io.IOException;     //File read करताना IOException येऊ शकतो म्हणून.
import java.util.List;      //Multiple products ची list return करण्यासाठी.
import java.util.UUID;      //Product ID UUID type ची आहे म्हणून.

@SuppressWarnings("ALL")        //
@RestController                 //ही class REST Controller आहे हे Spring ला सांगतो.म्हणजे या class मधल्या methods HTTP requests handle करतील.//आणि methods मधून return झालेला object JSON response म्हणून client ला मिळतो.//"@RestController is a combination of @Controller and @ResponseBody. It is used to create REST APIs where the returned objects are directly serialized into the HTTP response body, usually as JSON."
@RequestMapping("/api/v1/products")     //Base URL for all APIS ,सगळ्या product APIs चा common URL.
@RequiredArgsConstructor            //lombok ch annotation ahe , spring tya constructor mdhun product service inject krto ,Lombok automatically constructor तयार करतो. //lombok autmatically creates the constructor for constructor injection
@Validated                         //Validation support करण्यासाठी.
public class ProductController {

    private final ProductService productService;


    /**
     * Create a new product.
     *
     * Accepts multipart/form-data to optionally include a product image.
     * Only accessible by users with SELLER role.
     *
     * @param authentication the current authenticated user (used to get seller email)
     * @param name the product name
     * @param description the product description
     * @param price the product price
     * @param categoryId the ID of the category the product belongs to
     * @param stock the available stock quantity
     * @param file optional product image
     * @return the created ProductResponse with all product details
     * @throws IOException if there is an error reading the uploaded file
     */


    /*
    Q: Why did you use multipart/form-data? I used multipart/form-data because the product creation request contains both normal product fields and an optional image file."
    @RequestParam काय करते? Client कडून आलेली request parameter value controller method च्या variable मध्ये bind करते.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //POST HTTP request आल्यावर ही method execute होईल.consumes काय?हा controller endpoint कोणत्या प्रकारचा request data accept करतो ते सांगणे.ithe aapn multipart/form-data gheto accept krto
    @PreAuthorize("hasRole('SELLER')")  //Endpoint execute करण्याआधी Spring Security check करेल:Current user role = SELLER?जर हो: Controller method execute जर नाही: Access denied  //"@PreAuthorize handles role-based authorization, while ownership validation is handled in the service layer.
    public ResponseEntity<ProductResponse> createProduct(
            Authentication authentication,  //Current logged-in user ची security information मिळते. example email , role etc, aaplyala email pahije mhnun he vapral ahe aapn getNAme ne email ghetoy current login user cha
            @RequestParam String name,      //@RequestParam Client Request मधून name नावाची value घे आणि name variable मध्ये ठेव.
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Long categoryId,
            @RequestParam Integer stock,
            // @RequestPart हा image/file साठी आहे. //required = false mhnje image optional ahe without image pn product create kru shkto
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {     //@requestPart=@RequestPart Multipart request मधून एक specific part घेण्यासाठी. //Spring uploaded file ला MultipartFile object मध्ये ठेवतो.


        ProductRequest request = new ProductRequest(name, description, price, categoryId, stock);
        String sellerEmail = authentication.getName();   //current seller cha mail
        ProductResponse resp = productService.createProduct(request, file, sellerEmail);        //service call with 3 parameters
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);            //201 CREATED
    }

    /*
    ProductRequest request =  new ProductRequest(name, description, price, categoryId, stock);

इथे आपण DTO object तयार करतो.

आत्तापर्यंत request मधून values वेगवेगळ्या variables मध्ये आल्या: name , description , price ,categoryId ,stock
आता त्या एकाच object मध्ये pack केल्या:

             ProductRequest
          ┌─────────────────┐
          │ name            │
          │ description     │
          │ price           │
          │ categoryId      │
          │ stock           │
          └─────────────────┘

म्हणजे:

new ProductRequest(
    name,
    description,
    price,
    categoryId,
    stock
);

हा object तयार झाला: ProductRequest request

कारण Controller मधून Service ला product-related request data एकाच object मध्ये पाठवणे clean आहे.

नाहीतर Service method अशी दिसली असती:

createProduct(
    String name,
    String description,
    Double price,
    Long categoryId,
    Integer stock,
    MultipartFile file,
    String sellerEmail
)

खूप parameters झाले.

DTO वापरल्यावर:
createProduct(
    ProductRequest request,
    MultipartFile file,
    String sellerEmail
)

clean दिसतं.

interview answer
"For creating a product, I use a POST endpoint that consumes multipart/form-data because
the request can contain both product details and an optional image. Before executing the
method, @PreAuthorize verifies that the authenticated user has the SELLER role.
I use @RequestParam to receive the product fields and @RequestPart to receive the
optional image. I then create a ProductRequest DTO from these values. Using the
Authentication object, I get the currently logged-in seller's email.
I pass the DTO, image and seller email to the service layer, where the business logic
is handled. Finally, I return the created ProductResponse with HTTP 201 CREATED."

     */

    /**
     * Get all products.
     *
     * Public endpoint that lists all products.
     *
     * @return list of ProductResponse objects
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }
    /*
    इथे return type समजून घे. List<ProductResponse> आपल्याला एक product नाही, तर अनेक products मिळणार आहेत.
     ResponseEntity => ResponseEntity वापरून आपण पूर्ण HTTP response return करू शकतो.
     उदा.:

HTTP Status → 200 OK
Body → List<ProductResponse>
     */

    /**
     * Get a product by its ID.
     *
     * @param id the UUID of the product
     * @return ProductResponse for the requested product
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) { //pathvariable use to fetch the data from the url
        return ResponseEntity.ok(productService.getProductById(id));
    }


    /**
     * Update an existing product.
     *
     * Only accessible by the product owner (SELLER) or ADMIN.
     * Accepts multipart/form-data to optionally update product image.
     *
     * @param id the UUID of the product
     * @param authentication the current authenticated user (used to verify owner)
     * @param name optional new product name
     * @param description optional new product description
     * @param price optional new price
     * @param categoryId optional new category ID
     * @param stock optional new stock quantity
     * @param file optional new product image
     * @return updated ProductResponse
     * @throws IOException if there is an error reading the uploaded file
     */

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //याचा अर्थ request मध्ये text fields + image file दोन्ही पाठवता येतील. //कारण आपल्याला name, price, stock सोबत file सुद्धा पाठवायची आहे.
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')") //फक्त SELLER किंवा ADMIN ला product update करण्याची permission आहे.
    public ResponseEntity<ProductResponse> updateProduct(       //हा method शेवटी ProductResponse return करतो.//ResponseEntity मुळे HTTP status सुद्धा control करता येतो.
            @PathVariable UUID id,                          //URL मधला {id} इथे येतो. //हा id service ला सांगतो कोणता product update करायचा आहे.
            Authentication authentication,                  //यामध्ये currently logged-in user ची authentication information असते.
            @RequestParam(required = false) String name,    //Request मधून name घेते. // required = false म्हणजे name देणे compulsory नाही.
            @RequestParam(required = false) String description, //Product ची description घेते.he pn optional ahe
            @RequestParam(required = false) Double price,           // optional now ////Product ची updated price घेते. हे optional ठेवले आहे. म्हणजे seller ला फक्त stock update करायचा असेल तर price पाठवण्याची गरज नाही.
            @RequestParam(required = false) Long categoryId,       //Updated category चा ID घेते.
            @RequestParam(required = false) Integer stock,          //Updated stock quantity घेते.
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException { //MultipartFile म्हणजे uploaded file represent करण्यासाठी Spring चा type.//required = false म्हणजे image update करणे compulsory नाही.

        ProductRequest request = new ProductRequest(name, description, price, categoryId, stock);
        String sellerEmail = authentication.getName();//authentication.getName() → currently logged-in user चा username/email मिळवतो.
        ProductResponse resp = productService.updateProduct(id, request, file, sellerEmail);
        return ResponseEntity.ok(resp);
    }
/*
आता controller ला वेगवेगळे fields मिळाले:

name
description
price
categoryId
stock

हे सगळे एकाच ProductRequest DTO मध्ये ठेवले.

name ──────────┐
description ───┤
price ─────────┤
categoryId ────┤ → ProductRequest
stock ─────────┘

इथे file ProductRequest मध्ये नाही, कारण image वेगळ्या MultipartFile म्हणून handle केली आहे.
authentication.getName() → currently logged-in user चा username/email मिळवतो.
हा email service ला पाठवतो.

Service मग seller कोण आहे ते database मधून verify करू शकते.

आता controller आपले सगळे data service ला देतो:
id
request
file
sellerEmail

Service actual business logic करणार.Controller स्वतः database update करत नाही.

 Frontend
   ↓
PUT /products/{id}
   ↓
@PutMapping
   ↓
@PreAuthorize
   ↓
SELLER / ADMIN check
   ↓
@PathVariable → id
@RequestParam → name, description, price,
                 categoryId, stock
@RequestPart → file
   ↓
ProductRequest तयार
   ↓
authentication.getName()
   ↓
sellerEmail
   ↓
productService.updateProduct(...)
   ↓
Service
   ↓
Database / Cloudinary update
   ↓
ProductResponse
   ↓
ResponseEntity.ok()
   ↓
Frontend

"This endpoint updates an existing product using a multipart PUT request.
It accepts the product ID, optional product fields, an optional image, and the
 authenticated user's email. The controller creates a ProductRequest DTO and passes
 all the data to the service layer. Authorization is restricted to SELLER and ADMIN roles,
  while the service handles the actual update logic."
 */


    /**
     * Delete a product by its ID.
     *
     * Only accessible by the product owner (SELLER) or ADMIN.
     *
     * @param id the UUID of the product
     * @param authentication the current authenticated user (used to verify owner)
     * @return MessageResponse indicating deletion success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProduct( @PathVariable UUID id,Authentication authentication)   //Delete केल्यानंतर आपण MessageResponse return करणार आहोत. example = product deleted successfully
    {       // @PathVariable UUID id==> URL मधून product ची UUID घेतो.
            //Authentication authentication==> Spring Security कडून current logged-in user ची information मिळते.

        String sellerEmail = authentication.getName();      //Logged-in user चा email मिळवतो.
        MessageResponse resp = productService.deleteProduct(id, sellerEmail); //2 gosht = id ==> konta product delete kraycha ahe //2==> kon delete krt ahe
        return ResponseEntity.ok(resp);
    }
}

/*
| Operation             | HTTP Method | Endpoint                | Access         |
| --------------------- | ----------- | ----------------------- | -------------- |
| **Create Product**    | POST        | `/api/v1/products`      | SELLER         |  Create
| **Get All Products**  | GET         | `/api/v1/products`      | Public         |  Get All
| **Get Product By ID** | GET         | `/api/v1/products/{id}` | Public         |  Get One
| **Update Product**    | PUT         | `/api/v1/products/{id}` | SELLER / ADMIN |  Update
| **Delete Product**    | DELETE      | `/api/v1/products/{id}` | SELLER / ADMIN |  Delete


 */