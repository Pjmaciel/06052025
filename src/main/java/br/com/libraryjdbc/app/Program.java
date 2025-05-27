package br.com.libraryjdbc.app;

import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.service.BookService;
import br.com.libraryjdbc.service.CategoryService;
import br.com.libraryjdbc.service.impl.BookServiceImpl;
import br.com.libraryjdbc.service.impl.CategoryServiceImpl;
import db.DB;


public class Program {

    public static void main(String[] args) {
        try {
            DB.getConnection();
            System.out.println("✅ Database connected successfully!");

            // Create services (business logic layer)
            CategoryService categoryService = new CategoryServiceImpl();
            BookService bookService = new BookServiceImpl();

            // Create entities
            Category category = new Category("Technical", "Programming and technical books");
            Book book = new Book("Clean Code", "Robert C. Martin",
                    "A handbook of agile software craftsmanship", "9780132350884", 2008, category);

            System.out.println("\n=== TESTING SERVICE LAYER VALIDATIONS ===");

            // Save using Services (with business validations)
            try {
                Category savedCategory = categoryService.save(category);
                System.out.println("📂 Category saved: " + savedCategory);

                book.setCategory(savedCategory); // Update with saved category (has ID)
                Book savedBook = bookService.save(book);
                System.out.println("📖 Book saved: " + savedBook);

            } catch (IllegalArgumentException e) {
                System.out.println("❌ Business rule validation failed: " + e.getMessage());
            }

            System.out.println("\n=== TESTING BUSINESS RULE VALIDATIONS ===");

            // Test business rule: duplicate ISBN
            try {
                Book duplicateIsbn = new Book("Another Book", "Another Author",
                        "Another synopsis", "9780132350884", 2010, category);
                bookService.save(duplicateIsbn);
                System.out.println("❌ VALIDATION FAILURE: Duplicate ISBN was allowed!");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ ISBN validation working: " + e.getMessage());
            }

            // Test business rule: invalid year
            try {
                Book invalidYear = new Book("Old Book", "Old Author",
                        "Old synopsis", "1234567890", 1950, category);
                bookService.save(invalidYear);
                System.out.println("❌ VALIDATION FAILURE: Invalid year was allowed!");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Year validation working: " + e.getMessage());
            }

            // Test business rule: remove category with books
            try {
                categoryService.remove(category.getId());
                System.out.println("❌ VALIDATION FAILURE: Category with books was removed!");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Category removal validation working: " + e.getMessage());
            }

            System.out.println("\n✅ Service Layer working correctly!");
            System.out.println("📋 Business rules validated successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DB.closeConnection();
            System.out.println("✅ Database connection closed.");
        }
    }
}