package com.example.backend.Category.service.Impl;

import com.example.backend.Category.dto.CategoryRequest;
import com.example.backend.Category.dto.CategoryResponse;
import com.example.backend.Category.entity.Category;
import com.example.backend.Category.exception.*;
import com.example.backend.Category.repository.CategoryRepository;
import com.example.backend.Category.service.CategoryService;
import com.example.backend.Product.dto.ProductResponse;
import com.example.backend.Product.repository.ProductRepository;
import com.example.backend.auth.dto.Responses.MessageResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;



    /**
     * Create a new category.
     *
     * @param request the {@link CategoryRequest} containing name, description, and optional parentId
     * @return the created {@link CategoryResponse} with a success message
     * @throws CategoryAlreadyExistsException if a category with the same name already exists
     * @throws InvalidCategoryException if parentId is invalid
     */
    @Override                                                                   //@Override → interface मधली method implement केली आहे.
    public CategoryResponse createCategory(CategoryRequest request) {           //request मध्ये Admin ने पाठवलेली category information आहे.

        //या नावाची category आधीपासून database मध्ये आहे का?Duplicate check
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {  //trim() → नावाच्या सुरुवातीला/शेवटी असलेले unnecessary spaces काढतो.
            throw new CategoryAlreadyExistsException("Category with this name already exists.");
        }

        //इथे CategoryRequest मधला data घेऊन Category entity object तयार करतो.
        Category category = Category.builder()
                .name(request.getName().trim())     //request.getName ==> because he aapn request DTO mdhun ghetoy
                .description(request.getDescription())      //request.description ==> he description aapn request DTO mdhun ghetoy
                .build();

        /*
        .name(request.getName().trim())
        Request मधून name घेतो.

उदा.

request.getName()
       ↓
"Snacks"

आणि Category object मध्ये set करतो:
category.name = "Snacks"

.description(request.getDescription()) Request मधून description घेतो.

उदा.

"Snack products"
आणि Category object मध्ये ठेवतो.
         */
        // handle parent part//Request मध्ये parentId दिलेला आहे का?//mhnje seller ne ID dili asel mhnje hi parent category chi ID aste tyat
        if (request.getParentId() != null) {

            //parent category शोधणे, //parentid ji seller ne dili ahe tyane aapn parent category shodhu
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InvalidCategoryException("Parent category not found with id " + request.getParentId()));
            category.setParent(parent);
        }
        categoryRepository.save(category);

        return mapToResponse(category, "Category created successfully");
    }

/*

START

Receive category request

Check if user is ADMIN
    IF YES
        Continue
    ELSE
        Return 403 Forbidden
        STOP

Get category name from request
Remove extra spaces

Check if category name already exists
    IF YES
        Throw CategoryAlreadyExistsException
        STOP
    ELSE
        Continue

Create new Category object
Set name
Set description

Check if parentId is provided
    IF YES
        Find parent category by parentId

        IF parent category found
            Set parent category
        ELSE
            Throw InvalidCategoryException
            STOP

    IF NO
        Keep parent as null
        Treat it as main category

Save Category into database

Convert Category Entity into CategoryResponse

Return 201 CREATED

END

Admin check → Duplicate check → Create object → Parent आहे? → Parent validate → Save → Response.
-------------------------------------------------------------------------------------------------
@Override
→ interface method implement केली

createCategory(request)
→ category create करण्याची method

existsByNameIgnoreCase()
→ duplicate category आहे का check

Category.builder()
→ नवीन Category object तयार

request.getName()
→ request मधून name घे

request.getDescription()
→ request मधून description घे

request.getParentId()
→ parent category ची ID घे

findById()
→ parent category database मधून शोध

orElseThrow()
→ parent सापडला नाही तर exception

setParent()
→ category ला parent शी जोड

save()
→ database मध्ये save

mapToResponse()
→ Entity ला Response DTO मध्ये convert

सगळ्यात important दोन lines:

category.setParent(parent); → Subcategory ला Main Category शी जोडते.
categoryRepository.save(category); → तयार झालेली category database मध्ये save करते. ❤️
 */

    /**
     * Get a category by its ID.
     *
     * @param id the ID of the category
     * @return the {@link CategoryResponse} for the requested category
     * @throws CategoryNotFoundException if the category does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
        return mapToResponse(category , "Category found successfully");
    }
    /*
    START

Receive category ID

Start read-only transaction

Find category by ID in database

    IF category is found
        Store category object
        Continue
    ELSE
        Throw CategoryNotFoundException
        STOP

Convert Category entity into CategoryResponse

Return CategoryResponse

END
     */

    /**
     * Get all root categories and their subcategories recursively.
     *
     * @return a list of {@link CategoryResponse} objects representing root categories
     */    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Override                                                   //ही method CategoryService interface मध्ये declare केलेली आहे आणि इथे आपण तिची implementation देतो.
    @Transactional(readOnly = true)                             //ही method फक्त database मधून data read करते. काही insert/update/delete करत नाही.
    public List<CategoryResponse> getAllCategories() {          //Method शेवटी CategoryResponse ची list return करणार आहे.
        List<Category> allCategories = categoryRepository.findAll();        //Repository database मधून सगळ्या categories fetch करते.

        // Filter root categories (parent == null)//इथे आपण फक्त root/main categories निवडतो.
        List<Category> rootCategories = allCategories.stream()      //allCategories मधल्या प्रत्येक category वर एक-एक करून operation करण्यासाठी stream वापरला आहे.
                .filter(c -> c.getParent() == null)     //c.getParent() म्हणजे त्या category चा parent कोण आहे ते.
                .collect(Collectors.toList());
/*
//YES → root category → list मध्ये ठेवतो.
        //NO → subcategory → filter out करतो.
        //.collect(Collectors.toList());==> Filter झालेल्या categories पुन्हा List<Category> मध्ये convert करतो.

        // Map each root category to CategoryResponse, including subcategories recursively

 */
        return rootCategories.stream()                            //आता फक्त root categories आहेत.
                .map(c -> mapToResponse(c, "")) // प्रत्येक Category entity ला CategoryResponse मध्ये convert करतो.//इथे "" म्हणजे listing करताना message empty string पाठवत आहे.
                .collect(Collectors.toList());
    }
/*
पहिले DB मधून सगळ्या categories आणतो → ज्यांचा parent null आहे अशा main categories filter करतो
→ प्रत्येक main category ला CategoryResponse मध्ये convert करतो → response list return करतो.

इथे आपण सगळ्या categories DB मधून fetch करतो — main + subcategory दोन्ही.

पण त्यानंतर:

.filter(c -> c.getParent() == null)

इथे आपण फक्त main/root categories select करतो.
 */

    /**
     * Update an existing category.
     *
     * @param id the ID of the category to update
     * @param request the {@link CategoryRequest} with updated data
     * @return the updated {@link CategoryResponse}
     * @throws CategoryNotFoundException if category does not exist
     * @throws CategoryUpdateException if the name conflicts with another category
     * @throws InvalidCategoryException if parentId is invalid or causes a cycle
     */

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

       //step 1: Category आधी DB मध्ये आहे का?पहिल्यांदा दिलेल्या id वर category शोधतो.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));

        // handle name update with uniqueness check (exclude current id)
        //2. Name update करायचा आहे का?Name null आहे का नाही? && Name फक्त spaces किंवा empty आहे का?
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            /*
 name null आहे?
        ↓
   YES → IF false → name update नाही
   NO  → पुढची condition

Name empty/blank आहे?
        ↓
   YES → IF false → name update नाही
   NO  → IF true → पुढे
             */
            String newName = request.getName().trim();                           //Request मधला name घेऊन extra spaces remove करतो.
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {       //हा नवीन name दुसऱ्या category ने आधीच वापरला आहे का?
                throw new CategoryUpdateException("Category with this name already exists.");
            }
            category.setName(newName);
        }

        // update description   //Request मधली description existing category मध्ये set करतो.
        category.setDescription(request.getDescription());

        // handle parent update (can be null to remove parent) //आता category चा parent update करायचा आहे का ते check करतो.
        /*
        Ha code category cha parent update kraycha ahe
        yat 4 condition use krto
        आपण example घेऊ:

Food (id=1)
 ├── Snacks (id=2)
 └── Biscuits (id=3)

आपण Snacks (id=2) update करत आहोत.
         */
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new InvalidCategoryException("Category cannot be its own parent.");
            }

            //Condition: नवीन Parent DB मध्ये अस्तित्वात आहे का?
            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new InvalidCategoryException("Parent category not found with id " + request.getParentId()));

            // ensure 'category' is not an ancestor of the newParent
            Category cur = newParent;
            while (cur != null) {
                if (cur.getId().equals(category.getId())) {
                    throw new InvalidCategoryException("Setting this parent would create a cycle.");
                }
                cur = cur.getParent();
            }

            category.setParent(newParent);
        } else {
            // request explicitly null => make it a root category
            category.setParent(null);
        }

        categoryRepository.save(category);

        return mapToResponse(category, "Category updated successfully");

    }
/*
1. नवीन parent दिला आहे?
       │
       ├─ NO → parent = null → Root Category
       │
       └─ YES
            ↓
2. स्वतःचीच parent आहे?
       │
       ├─ YES → Exception
       │
       └─ NO
            ↓
3. Parent DB मध्ये आहे?
       │
       ├─ NO → Exception
       │
       └─ YES
            ↓
4. Cycle तयार होईल?
       │
       ├─ YES → Exception
       │
       └─ NO
            ↓
5. category.setParent(newParent)
            ↓
6. Save



flow
Category Parent Update — Interview Flow

समजा आपण Snacks category update करत आहोत आणि तिचा parent बदलायचा आहे.

1. नवीन Parent दिला आहे का?
Request मध्ये parentId आहे का?
        ↓
   ┌────┴────┐
  NO        YES
   ↓          ↓
parent =     पुढचा check
null
   ↓
Snacks आता
Root Category

NO: User ने parentId = null दिला → category चा parent remove करतो → ती root category बनते.

YES: नवीन parent दिला आहे → पुढे validation करतो.

2. नवीन Parent हीच Category तर नाही ना?
नवीन parent ID == current category ID ?
              ↓
         ┌────┴────┐
        YES        NO
         ↓          ↓
     Exception    पुढे
         ↓
       STOP

उदा. Snacks चा parent पुन्हा Snacks ठेवता येणार नाही.

3. नवीन Parent DB मध्ये आहे का?
New Parent database मध्ये आहे का?
              ↓
         ┌────┴────┐
        NO         YES
         ↓           ↓
     Exception     पुढे
         ↓
       STOP

Parent अस्तित्वात नसेल तर update करता येणार नाही.

4. नवीन Parent मुळे Cycle तयार होईल का?

हा check hierarchy सुरक्षित ठेवण्यासाठी आहे.

उदा.:

Food
 ↓
Snacks
 ↓
Chips

आता Food चा parent Chips केला तर:

Food → Chips → Snacks → Food

Cycle तयार होईल.

म्हणून code नवीन parent पासून वरच्या parent chain मध्ये जाऊन check करतो:

Cycle तयार होईल?
       ↓
  ┌────┴────┐
 YES        NO
  ↓          ↓
Exception   पुढे
  ↓
STOP
5. सगळे checks successful झाले तर
category.setParent(newParent)
        ↓
नवीन parent assign
        ↓
categoryRepository.save(category)
        ↓
Database मध्ये update
        ↓
CategoryResponse
        ↓
200 OK
🔥 Interview मध्ये सलग असं सांग

"While updating a category, I first check whether a new parent category is provided.
 If no parent is provided, I set the parent to null and make the category a root category.
  If a parent is provided, I first check that the category is not assigning itself as its
  own parent. Then I verify that the new parent exists in the database. After that,
  I check the parent hierarchy to make sure assigning the new parent will not create a
  circular relationship. If all validations pass, I assign the new parent, save the
  category, and return the updated response."
 */



    /**
     * Delete a category by ID.
     *
     * @param id the ID of the category to delete
     * @return a {@link MessageResponse} confirming deletion
     * @throws CategoryNotFoundException if category does not exist
     * @throws CategoryDeletionException if category has subcategories or linked products
     */    @Override            ////"@Override indicates that this method is overriding a method declared in the parent interface."
    public MessageResponse deleteCategory(Long id) {
        /*
       2. Method declaration
public MessageResponse deleteCategory(Long id)

यात 3 गोष्टी आहेत.

🔥 public
हा method दुसऱ्या class मधून call करता येतो.
Controller मधून Service ला call करता येतो.

🔥MessageResponse
Method काय return करणार?
MessageResponse object.
उदा.

{
    "message": "Category deleted successfully"
}

🔥deleteCategory(Long id)
हा category delete करण्यासाठीचा method आहे.
Long id म्हणजे कोणती category delete करायची तिची ID.

उदा.  DELETE /api/v1/categories/3
तर: id = 3
         */

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));


        // If subcategories exist -> prevent delete
        /*
        aapn category ghetli na findby id ne ti category variable mdhe store zali
        aapn category.getsubCategories kel jr subcategories astil ani ani subcategories empty nasel tr aapn delete nahi kru kru shkt
         */
        boolean hasSub = category.getSubCategories() != null && !category.getSubCategories().isEmpty();

        if (hasSub) {   //hasSub boolean ahe to true kiva false return krel , jr true asel cannot delete
            throw new CategoryDeletionException("Cannot delete category that has subcategories.");
        }

        // Check for linked products//जर subcategories नसतील तर आपण पुढे येतो:त्या category शी कमीत कमी एक Product linked आहे का हे check करणे.
        if (productRepository.existsByCategory(category)) {
            throw new CategoryDeletionException("Cannot delete category that has products linked to it.");
        }

        categoryRepository.deleteById(id);
        return new MessageResponse("Category deleted successfully");       // response message
    }

/*
DELETE category request
        ↓
Category ID मिळाली
        ↓
Category DB मध्ये शोधली
        ↓
Category exists?
   ↓             ↓
 NO             YES
 ↓               ↓
Exception      पुढे
                  ↓
        Subcategories आहेत?
             ↓          ↓
            YES         NO
             ↓           ↓
         Exception     पुढे
                          ↓
                  Products linked आहेत?
                     ↓          ↓
                    YES         NO
                     ↓           ↓
                 Exception    Delete
                                ↓
                         Success Response



            1. Category exists आहे का?
→ नाही → CategoryNotFoundException

2. Subcategories आहेत का?
→ आहेत → CategoryDeletionException

3. Products linked आहेत का?
→ आहेत → CategoryDeletionException

सगळं safe असेल तर → Delete.

Interview मध्ये code explain करताना ही exact sequence लक्षात ठेव:

“First I fetch the category and validate that it exists. Then I
 check whether it has any subcategories. If subcategories exist, I prevent deletion. Next,
 I check whether any products are linked to the category. If products are linked,
 I again prevent deletion. Only if both validations pass, I delete the category and return a
 success message.”
 */


    /**
     * Map a Category entity to {@link CategoryResponse}, including subcategories and products.
     *
     * @param c the category entity
     * @param message optional message
     * @return the {@link CategoryResponse} representing the category
     */
    private CategoryResponse mapToResponse(Category c, String message) {
        Long parentId = c.getParent() != null ? c.getParent().getId() : null;
        String parentName = c.getParent() != null ? c.getParent().getName() : null;

        // Recursively map subcategories
        List<CategoryResponse> subCat = (c.getSubCategories() == null ? Collections.emptyList() : c.getSubCategories())
                .stream()
                .map(sub -> mapToResponse((Category) sub, ""))
                .collect(Collectors.toList());

        List<ProductResponse> products = c.getProducts() != null
                ? c.getProducts().stream()
                .map(p -> ProductResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .stock(p.getStock())
                        .active(p.getActive())
                        .imageUrl(p.getImageUrl())
                        .categoryId(c.getId())
                        .categoryName(c.getName())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return CategoryResponse.builder()
                .message(message)
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .parentId(parentId)
                .parentName(parentName)
                .subCategories(subCat)
                .products(products)
                .build();
    }
}
