package com.example.backend.Category.Controller;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.Category.service.CategoryService;
import com.example.backend.auth.dto.Responses.MessageResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("ALL")
@RestController                                 //हा class REST APIs handle करतो.
@RequestMapping("/api/v1/categories")           //सर्व category APIs चा base URL.
@AllArgsConstructor                             //constructor automatically तयार होतो, त्यामुळे categoryService inject होतं.
public class CategoryController {

    private final CategoryService categoryService;

/*
“This is a POST API used to create a new category. The endpoint is
restricted to ADMIN using @PreAuthorize. The request JSON is converted
 into CategoryRequest using @RequestBody, and @Valid performs validation.
 The controller passes the request to the service layer. After successful
 creation, it returns CategoryResponse with HTTP 201 Created.”
 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")   //👉 फक्त ADMIN हा API call करू शकतो.
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest categoryRequest){    //@RequestBody Frontend/Postman मधून आलेला JSON data CategoryRequest object मध्ये convert करतो.//@Valid 👉 CategoryRequest मध्ये validation annotations असतील तर ते check करतो.//CategoryRequest 👉 हा input DTO आहे. Client कडून category create करण्यासाठी data घेतो.
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryRequest));
    }

    /**
     * Get a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return the {@link CategoryResponse} for the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }


    /**
     * Get all categories.
     *
     * @return a list of all {@link CategoryResponse} objects
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Update an existing category (ADMIN only).
     *
     * @param id the ID of the category to update
     * @param categoryRequest the new category data
     * @return the updated {@link CategoryResponse}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")       //फक्त ADMIN role ch category la update करू शकतो.//User ADMIN आहे?// YES → पुढे Controller execute// NO  → 403 Forbidden
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id , @RequestBody @Valid CategoryRequest categoryRequest){    //@Valid मुळे CategoryRequest वर validation rules असतील तर ते check होतात.
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequest));
    }//ResponseEntity मुळे HTTP status + response body return करू शकतो.
//updateCategory hi method CategoryResponse return करते., category response aapn ek vegli file banvli ahe DTO chi

    /*
    PUT /categories/{id}
        ↓
@PreAuthorize ADMIN?
   ↓             ↓
 YES             NO
 ↓               ↓
@PathVariable    403
 id
 ↓
@RequestBody
 ↓
CategoryRequest
 ↓
@Valid
 ↓
categoryService.updateCategory(id, request)
 ↓
Service
 ↓
CategoryResponse
 ↓
ResponseEntity.ok()
 ↓
200 OK
     */
    /**
     * Delete a category by ID (ADMIN only).
     *
     * @param id the ID of the category to delete
     * @return a {@link MessageResponse} confirming deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
