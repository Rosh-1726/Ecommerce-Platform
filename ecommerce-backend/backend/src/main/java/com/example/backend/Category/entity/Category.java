package com.example.backend.Category.entity;

import com.example.backend.Product.entity.Product;
import jakarta.persistence.*;                       //JPA annotations साठी:@Entity @Table ,@Id ,@OneToMany ,@ManyToOne ,@JoinColumn
import lombok.*;                                    //lombok annotation sathi = @getter @setter @Builder @NoArgsConstructor @AllArgsConstructure
import org.hibernate.annotations.CreationTimestamp; //Created आणि updated time automatically manage करण्यासाठी.
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;             //List initialize करण्यासाठी
import java.util.List;                  //multiple categories/products ठेवण्यासाठी

@Entity                                 //हा Java class database मधल्या table शी map होणार आहे.
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor                  //No-argument constructor तयार करतो.
@AllArgsConstructor                 //सगळ्या fields चा constructor तयार करतो.
@Builder                            //Builder pattern वापरून object तयार करता येतो.
/*
@builder ch example ==>
Category category = Category.builder()
        .name("Electronics")
        .description("Electronic products")
        .build();
 */
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Database स्वतः ID generate करेल.
    private Long id;           //id हा Category चा primary key आहे.

    @Column(unique = true, nullable = false) //unique = true ==>दोन categories चे same name असू शकत नाही.//Name null असू शकत नाही.
    private String name;

    private String description;

    @CreationTimestamp      //Category create झाल्यावर Hibernate automatically creation time set करतो.
    @Column(name = "created_at", updatable = false)//updatable = false म्हणजे category update करताना createdAt बदलणार नाही.
    private LocalDateTime createdAt;

    @UpdateTimestamp        //Category update झाली की Hibernate updatedAt automatically update करतो.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    /*
    इथे Category चा Category सोबतच relationship आहे.

    Example:

Electronics
   ↓
   ├── Mobiles
   ├── Laptops
   └── TVs

Mobiles ची parent category = Electronics.

many ==> Mobile , laptops , TVs = child category
one ==> Electronics = Parent
@ManyToOne का? ==> कारण अनेक child categories एका parent category खाली असू शकतात.

Electronics
    ↑
    |
Mobiles
Laptops
TVs

म्हणजे:  Many subcategories → One parent category
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent; // points to the parent category

    /*
    Subcategories ==> हे parent category च्या child categories ची list ठेवतं.
    उदा.

Electronics
subCategories:
    Mobiles
    Laptops
    TVs
mappedBy = "parent" म्हणजे?

हे खूप important interview question आहे.  parent हे Category class मधलं field आहे: (category table mdhe parent navacha column ahe )
     private Category parent;

म्हणून:
mappedBy = "parent"
हणजे relationship चा owning side parent field आहे.

Simple interview answer:
“mappedBy tells JPA that the relationship is already mapped by the parent field on the other side, so this side is not the owning side.”
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> subCategories = new ArrayList<>(); // list of children categories

    /*
    एका category मध्ये multiple products असतात.

उदा.

Electronics
   |
   ├── iPhone
   ├── Samsung
   ├── Laptop
   └── TV

म्हणून:

One Category → Many Products
आणि mappedBy = "category" म्हणजे Product entity मध्ये category field आहे.

FetchType.LAZY का?
Category fetch केली म्हणून लगेच तिचे सगळे products database मधून आणायची गरज नाही.
LAZY मुळे products गरज पडल्यावर load होतात.

Interview answer:
“I use lazy fetching so that products are not loaded unnecessarily when only category information is required.”


     IMPORTANT
     5️⃣ @JoinColumn कुठे टाकायचं?

यासाठी अजून एक भारी trick: ज्या table मध्ये दुसऱ्या table ची ID store होणार, तिथे @JoinColumn येतो.
Foreign Key ज्या table मध्ये → @JoinColumn त्या entity मध्ये.
“ज्याच्याकडे List आहे = Many side.
ज्याच्याकडे single object आहे = One side.
Foreign key जिथे = JoinColumn तिथे.

     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>(); // list of products in this category

    /*

•  category_id → Product ला Category शी connect करतो
•  parent_id → Subcategory ला Main Category शी connect करतो same table mdhe
🍔 1. Categories Table
आपल्याकडे Food ही main category आहे आणि Snacks, Biscuits, Chocolates या subcategories आहेत.

| id | name       | description        | created_at       | updated_at       | parent_id |
| -: | ---------- | ------------------ | ---------------- | ---------------- | --------: |
|  1 | Food       | All food products  | 2026-09-01 10:00 | 2026-09-01 10:00 |      NULL |
|  2 | Snacks     | Snacks items       | 2026-09-01 10:05 | 2026-09-01 10:05 |         1 |
|  3 | Biscuits   | Biscuit products   | 2026-09-01 10:10 | 2026-09-01 10:10 |         1 |
|  4 | Chocolates | Chocolate products | 2026-09-01 10:15 | 2026-09-01 10:15 |         1 |


इथे काय झालं?
Food
 ├── Snacks
 ├── Biscuits
  └── Chocolates
Food ची id = 1
म्हणून:
Snacks      → parent_id = 1
Biscuits    → parent_id = 1
Chocolates  → parent_id = 1

म्हणून Category entity मध्ये:

@ManyToOne
@JoinColumn(name = "parent_id")
private Category parent;

parent_id same categories table मधल्या parent category च्या id ला refer करतो
याचा अर्थ database मध्ये parent_id नावाचा column वापरून हे connection store कर.
🍫 2. Products Table
आता actual products:
|  id | name        | description       | price | stock | active | image_url   | category_id |
| --: | ----------- | ----------------- | ----: | ----: | ------ | ----------- | ----------: |
| 101 | Lays        | Potato chips      |    20 |    50 | true   | lays.jpg    |           2 |
| 102 | Kurkure     | Masala snack      |    20 |    40 | true   | kurkure.jpg |           2 |
| 103 | Parle-G     | Glucose biscuit   |    10 |   100 | true   | parle.jpg   |           3 |
| 104 | Happy Happy | Chocolate biscuit |    20 |    60 | true   | happy.jpg   |           3 |
| 105 | Good Day    | Butter biscuit    |    30 |    70 | true   | goodday.jpg |           3 |
| 106 | Oreo        | Cream biscuit     |    40 |    50 | true   | oreo.jpg    |           3 |
| 107 | Dairy Milk  | Milk chocolate    |    50 |    80 | true   | dairy.jpg   |           4 |
| 108 | KitKat      | Chocolate bar     |    40 |    60 | true   | kitkat.jpg  |           4 |




🧠 3. category_id काय करतो?
Product table मधला category_id त्या product ला category शी connect करतो.
उदा.
Lays
category_id = 2
Categories table मध्ये:
id = 2 → Snacks
म्हणून:
Lays → Snacks

Similarly:
Parle-G
category_id = 3
id = 3 → Biscuits
म्हणून:
Parle-G → Biscuits

आणि:
Dairy Milk
category_id = 4
id = 4 → Chocolates
म्हणून:
Dairy Milk → Chocolates

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
private List<Category> subCategories = new ArrayList<>();

1. @OneToMany
One parent category → Many subcategories
Food
 ├── Snacks
 ├── Biscuits
  └── Chocolates
म्हणून @OneToMany.
________________________________________
2. private List<Category> subCategories
याचा अर्थ: या Category ची सर्व child/subcategories ची list इथे ठेवणार.
उदा. Food object मध्ये:
subCategories = [Snacks, Biscuits, Chocolates]
List<Category> कारण एकापेक्षा जास्त Category असू शकतात.
________________________________________
3. mappedBy = "parent" ⭐
हा सगळ्यात important आहे.
आपण आधी लिहिलं:
private Category parent;
म्हणजे प्रत्येक subcategory ला एक parent आहे.
mappedBy = "parent" म्हणजे:
हे relationship Category मधल्या parent field ने manage केलं आहे.
म्हणून:
Food
  ↑
  │ parent
  │
Snacks
आणि Food च्या:
subCategories
मध्ये Snacks येईल.
4. cascade = CascadeType.ALL
Parent category वर काही operation केल्यास त्याच्या subcategories वरही तो operation लागू होऊ शकतो.


•	Category table → categories ठेवतो.
•	parent_id → category ला दुसऱ्या category शी connect करतो.
•	subCategories → एका parent category च्या child categories ची list.
•	category_id → Product ला Category शी connect करतो.

     */
}
