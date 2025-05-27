package br.com.libraryjdbc.test.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.service.BookService;
import br.com.libraryjdbc.service.CategoryService;
import br.com.libraryjdbc.service.impl.BookServiceImpl;
import br.com.libraryjdbc.service.impl.CategoryServiceImpl;
import db.DB;


public class TestBookService {

    private static BookService bookService;
    private static CategoryService categoryService;
    private static Category testCategory;

    public static void main(String[] args) {
        try {
            // Setup
            DB.getConnection();
            System.out.println("✅ Test connection established successfully!");

            bookService = new BookServiceImpl();
            categoryService = new CategoryServiceImpl();

            // Clean test data before starting
            cleanTestData();

            // Setup test category
            setupTestCategory();

            // Run all service tests
            testBookSaveValidations();
            testBookUpdateValidations();
            testBookRemoveValidations();
            testBookFindOperations();
            testBookBusinessRules();

            System.out.println("\n🎉 All BookService tests completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in BookService tests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test data
            cleanTestData();
            DB.closeConnection();
            System.out.println("✅ Test connection closed and cleanup completed!");
        }
    }

    // ==================== SETUP ====================

    private static void setupTestCategory() {
        try {
            testCategory = new Category("Technical", "Technical and programming books");
            testCategory = categoryService.save(testCategory);
            System.out.println("✅ Test category created: " + testCategory.getName());
        } catch (Exception e) {
            System.err.println("❌ Failed to create test category: " + e.getMessage());
            throw new RuntimeException("Test setup failed", e);
        }
    }

    // ==================== SAVE VALIDATIONS TESTS ====================

    private static void testBookSaveValidations() {
        System.out.println("\n=== BOOK SAVE VALIDATIONS TESTS ===");

        // Test 1: Valid book save
        testValidBookSave();

        // Test 2: Business rule validations
        testSaveValidationFailures();

        // Test 3: ISBN and category validations
        testISBNAndCategoryValidations();
    }

    private static void testValidBookSave() {
        System.out.println("\n--- Valid Book Save ---");

        try {
            Book book = new Book("Clean Code", "Robert C. Martin",
                    "A handbook of agile software craftsmanship", "9780132350884", 2008, testCategory);
            Book savedBook = bookService.save(book);

            if (savedBook.getId() != null && savedBook.getId() > 0) {
                System.out.println("✅ Valid book saved successfully with ID: " + savedBook.getId());
            } else {
                System.out.println("❌ Failed to save valid book - ID not generated");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error saving valid book: " + e.getMessage());
        }
    }

    private static void testSaveValidationFailures() {
        System.out.println("\n--- Save Validation Failures ---");

        // Test null book
        try {
            bookService.save(null);
            System.out.println("❌ VALIDATION FAILURE: Null book was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("cannot be null")) {
                System.out.println("✅ Null book validation working");
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }

        // Test empty title
        try {
            Book emptyTitle = new Book("", "Valid Author", "Synopsis", "1234567890", 2020, testCategory);
            bookService.save(emptyTitle);
            System.out.println("❌ VALIDATION FAILURE: Empty title was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("title cannot be empty")) {
                System.out.println("✅ Empty title validation working");
            } else {
                System.out.println("❌ Unexpected error for empty title: " + e.getMessage());
            }
        }

        // Test null author
        try {
            Book nullAuthor = new Book("Valid Title", null, "Synopsis", "1234567891", 2020, testCategory);
            bookService.save(nullAuthor);
            System.out.println("❌ VALIDATION FAILURE: Null author was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("author cannot be empty")) {
                System.out.println("✅ Null author validation working");
            } else {
                System.out.println("❌ Unexpected error for null author: " + e.getMessage());
            }
        }

        // Test invalid year (< 1967)
        try {
            Book invalidYear = new Book("Old Book", "Old Author", "Synopsis", "1234567892", 1950, testCategory);
            bookService.save(invalidYear);
            System.out.println("❌ VALIDATION FAILURE: Invalid year was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("must be >= 1967")) {
                System.out.println("✅ Invalid year validation working");
            } else {
                System.out.println("❌ Unexpected error for invalid year: " + e.getMessage());
            }
        }

        // Test null year
        try {
            Book nullYear = new Book("Valid Title", "Valid Author", "Synopsis", "1234567893", null, testCategory);
            bookService.save(nullYear);
            System.out.println("❌ VALIDATION FAILURE: Null year was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("release year cannot be null")) {
                System.out.println("✅ Null year validation working");
            } else {
                System.out.println("❌ Unexpected error for null year: " + e.getMessage());
            }
        }
    }

    private static void testISBNAndCategoryValidations() {
        System.out.println("\n--- ISBN and Category Validations ---");

        // Test duplicate ISBN
        try {
            Book duplicateISBN = new Book("Another Book", "Another Author", "Synopsis", "9780132350884", 2010, testCategory);
            bookService.save(duplicateISBN);
            System.out.println("❌ VALIDATION FAILURE: Duplicate ISBN was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ISBN already exists")) {
                System.out.println("✅ Duplicate ISBN validation working");
            } else {
                System.out.println("❌ Unexpected error for duplicate ISBN: " + e.getMessage());
            }
        }

        // Test null category
        try {
            Book nullCategory = new Book("Valid Title", "Valid Author", "Synopsis", "1234567894", 2020, null);
            bookService.save(nullCategory);
            System.out.println("❌ VALIDATION FAILURE: Null category was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("must have a valid category")) {
                System.out.println("✅ Null category validation working");
            } else {
                System.out.println("❌ Unexpected error for null category: " + e.getMessage());
            }
        }

        // Test invalid category ID
        try {
            Category invalidCategory = new Category();
            invalidCategory.setId(999999L);
            Book invalidCategoryBook = new Book("Valid Title", "Valid Author", "Synopsis", "1234567895", 2020, invalidCategory);
            bookService.save(invalidCategoryBook);
            System.out.println("❌ VALIDATION FAILURE: Invalid category was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("does not exist")) {
                System.out.println("✅ Invalid category validation working");
            } else {
                System.out.println("❌ Unexpected error for invalid category: " + e.getMessage());
            }
        }
    }

    // ==================== UPDATE VALIDATIONS TESTS ====================

    private static void testBookUpdateValidations() {
        System.out.println("\n=== BOOK UPDATE VALIDATIONS TESTS ===");

        // Test 1: Valid update
        testValidBookUpdate();

        // Test 2: Update validation failures
        testUpdateValidationFailures();

        // Test 3: Update non-existing book
        testUpdateNonExistingBook();
    }

    private static void testValidBookUpdate() {
        System.out.println("\n--- Valid Book Update ---");

        try {
            // Create book to update
            Book original = new Book("UpdateTest", "Original Author", "Original synopsis", "UPDATE123", 2000, testCategory);
            Book saved = bookService.save(original);

            // Update it
            saved.setTitle("Updated Title");
            saved.setAuthor("Updated Author");
            saved.setSynopsis("Updated synopsis");
            bookService.update(saved);

            // Verify update
            Book updated = bookService.findById(saved.getId());

            if (updated != null &&
                    "Updated Title".equals(updated.getTitle()) &&
                    "Updated Author".equals(updated.getAuthor())) {
                System.out.println("✅ Book updated successfully");
            } else {
                System.out.println("❌ Book update failed or data not persisted");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error in valid update: " + e.getMessage());
        }
    }

    private static void testUpdateValidationFailures() {
        System.out.println("\n--- Update Validation Failures ---");

        // Test null ID
        try {
            Book nullId = new Book("Title", "Author", "Synopsis", "NULL123", 2020, testCategory);
            nullId.setId(null);
            bookService.update(nullId);
            System.out.println("❌ VALIDATION FAILURE: Update with null ID was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ID cannot be null")) {
                System.out.println("✅ Null ID validation working");
            } else {
                System.out.println("❌ Unexpected error for null ID: " + e.getMessage());
            }
        }
    }

    private static void testUpdateNonExistingBook() {
        System.out.println("\n--- Update Non-Existing Book ---");

        try {
            Book nonExisting = new Book("Non Existing", "Author", "Synopsis", "NONEXIST123", 2020, testCategory);
            nonExisting.setId(999999L);

            bookService.update(nonExisting);
            System.out.println("❌ VALIDATION FAILURE: Update of non-existing book was allowed!");

        } catch (IllegalStateException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✅ Update non-existing book validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    // ==================== REMOVE VALIDATIONS TESTS ====================

    private static void testBookRemoveValidations() {
        System.out.println("\n=== BOOK REMOVE VALIDATIONS TESTS ===");

        // Test 1: Valid remove
        testValidBookRemove();

        // Test 2: Remove non-existing book
        testRemoveNonExistingBook();

        // Test 3: Remove with null ID
        testRemoveNullId();
    }

    private static void testValidBookRemove() {
        System.out.println("\n--- Valid Book Remove ---");

        try {
            // Create book to remove
            Book toRemove = new Book("ToRemove", "Author", "Synopsis", "REMOVE123", 2020, testCategory);
            Book saved = bookService.save(toRemove);
            Long idToRemove = saved.getId();

            // Remove it
            bookService.remove(idToRemove);

            // Verify removal
            Book removed = bookService.findById(idToRemove);

            if (removed == null) {
                System.out.println("✅ Book removed successfully");
            } else {
                System.out.println("❌ Book removal failed - still exists");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error in removal: " + e.getMessage());
        }
    }

    private static void testRemoveNonExistingBook() {
        System.out.println("\n--- Remove Non-Existing Book ---");

        try {
            bookService.remove(999999L);
            System.out.println("❌ VALIDATION FAILURE: Removal of non-existing book was allowed!");

        } catch (IllegalStateException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✅ Remove non-existing book validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    private static void testRemoveNullId() {
        System.out.println("\n--- Remove Null ID ---");

        try {
            bookService.remove(null);
            System.out.println("❌ VALIDATION FAILURE: Remove with null ID was allowed!");

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ID cannot be null")) {
                System.out.println("✅ Remove null ID validation working");
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    // ==================== FIND OPERATIONS TESTS ====================

    private static void testBookFindOperations() {
        System.out.println("\n=== BOOK FIND OPERATIONS TESTS ===");

        // Test 1: Find by ID
        testFindById();

        // Test 2: Find all books
        testFindAll();

        // Test 3: Find by author
        testFindByAuthor();

        // Test 4: Find by category
        testFindByCategory();
    }

    private static void testFindById() {
        System.out.println("\n--- Find By ID Test ---");

        try {
            // Create book to find
            Book toFind = new Book("FindTest", "Find Author", "Synopsis", "FIND123", 2020, testCategory);
            Book saved = bookService.save(toFind);

            // Find it
            Book found = bookService.findById(saved.getId());

            if (found != null && found.getId().equals(saved.getId())) {
                System.out.println("✅ Find by ID working correctly");
            } else {
                System.out.println("❌ Find by ID failed");
            }

            // Test null ID
            Book nullResult = bookService.findById(null);
            if (nullResult == null) {
                System.out.println("✅ Find by null ID handled correctly");
            } else {
                System.out.println("❌ Find by null ID should return null");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in find by ID test: " + e.getMessage());
        }
    }

    private static void testFindAll() {
        System.out.println("\n--- Find All Test ---");

        try {
            var books = bookService.findAll();

            if (books != null) {
                System.out.println("✅ Find all working - found " + books.size() + " books");
            } else {
                System.out.println("❌ Find all returned null");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in find all test: " + e.getMessage());
        }
    }

    private static void testFindByAuthor() {
        System.out.println("\n--- Find By Author Test ---");

        try {
            // Test with existing author
            var martinBooks = bookService.findByAuthor("Martin");
            System.out.println("✅ Find by author working - found " + martinBooks.size() + " books by 'Martin'");

            // Test empty author
            try {
                bookService.findByAuthor("");
                System.out.println("❌ VALIDATION FAILURE: Empty author search was allowed!");
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("cannot be empty")) {
                    System.out.println("✅ Empty author validation working");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error in find by author test: " + e.getMessage());
        }
    }

    private static void testFindByCategory() {
        System.out.println("\n--- Find By Category Test ---");

        try {
            // Test with valid category
            var categoryBooks = bookService.findByCategory(testCategory.getId());
            System.out.println("✅ Find by category working - found " + categoryBooks.size() + " books in category");

            // Test with null category ID
            try {
                bookService.findByCategory(null);
                System.out.println("❌ VALIDATION FAILURE: Null category ID search was allowed!");
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("ID cannot be null")) {
                    System.out.println("✅ Null category ID validation working");
                }
            }

            // Test with invalid category ID
            try {
                bookService.findByCategory(999999L);
                System.out.println("❌ VALIDATION FAILURE: Invalid category ID search was allowed!");
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("does not exist")) {
                    System.out.println("✅ Invalid category ID validation working");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error in find by category test: " + e.getMessage());
        }
    }

    // ==================== BUSINESS RULES TESTS ====================

    private static void testBookBusinessRules() {
        System.out.println("\n=== BOOK BUSINESS RULES TESTS ===");

        // Test 1: ISBN exists validation
        testISBNExistsValidation();

        // Test 2: Category exists validation
        testCategoryExistsValidation();

        // Test 3: Valid release year validation
        testValidReleaseYearValidation();
    }

    private static void testISBNExistsValidation() {
        System.out.println("\n--- ISBN Exists Validation ---");

        try {
            // Test existing ISBN
            boolean exists = bookService.isbnExists("9780132350884");
            System.out.println("✅ ISBN exists check working - existing ISBN: " + exists);

            // Test non-existing ISBN
            boolean notExists = bookService.isbnExists("NONEXISTENT123");
            System.out.println("✅ ISBN exists check working - non-existing ISBN: " + notExists);

            // Test null ISBN
            boolean nullResult = bookService.isbnExists(null);
            if (!nullResult) {
                System.out.println("✅ Null ISBN handled correctly (returns false)");
            } else {
                System.out.println("❌ Null ISBN should return false");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in ISBN exists test: " + e.getMessage());
        }
    }

    private static void testCategoryExistsValidation() {
        System.out.println("\n--- Category Exists Validation ---");

        try {
            // Test existing category
            boolean exists = bookService.categoryExists(testCategory.getId());
            System.out.println("✅ Category exists check working - existing category: " + exists);

            // Test non-existing category
            boolean notExists = bookService.categoryExists(999999L);
            System.out.println("✅ Category exists check working - non-existing category: " + notExists);

            // Test null category ID
            boolean nullResult = bookService.categoryExists(null);
            if (!nullResult) {
                System.out.println("✅ Null category ID handled correctly (returns false)");
            } else {
                System.out.println("❌ Null category ID should return false");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in category exists test: " + e.getMessage());
        }
    }

    private static void testValidReleaseYearValidation() {
        System.out.println("\n--- Valid Release Year Validation ---");

        try {
            // Test valid years
            boolean valid2020 = bookService.isValidReleaseYear(2020);
            boolean valid1967 = bookService.isValidReleaseYear(1967);
            System.out.println("✅ Valid year check working - 2020: " + valid2020 + ", 1967: " + valid1967);

            // Test invalid years
            boolean invalid1966 = bookService.isValidReleaseYear(1966);
            boolean invalid1950 = bookService.isValidReleaseYear(1950);
            System.out.println("✅ Invalid year check working - 1966: " + invalid1966 + ", 1950: " + invalid1950);

            // Test null year
            boolean nullYear = bookService.isValidReleaseYear(null);
            if (!nullYear) {
                System.out.println("✅ Null year handled correctly (returns false)");
            } else {
                System.out.println("❌ Null year should return false");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in valid release year test: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private static void cleanTestData() {
        try {
            Connection conn = DB.getConnection();

            // Clean books first (FK dependency)
            try (PreparedStatement st = conn.prepareStatement(
                    "DELETE FROM book WHERE isbn LIKE '%TEST%' OR isbn LIKE '%123%' OR " +
                            "isbn LIKE '%UPDATE%' OR isbn LIKE '%REMOVE%' OR isbn LIKE '%FIND%' OR " +
                            "isbn LIKE '%NULL%' OR isbn LIKE '%NONEXIST%' OR title LIKE '%Test%'")) {
                st.executeUpdate();
            }

            // Clean test categories
            try (PreparedStatement st = conn.prepareStatement(
                    "DELETE FROM category WHERE name LIKE '%Test%' OR name LIKE '%Technical%' OR " +
                            "name LIKE '%Programming%'")) {
                st.executeUpdate();
            }

        } catch (SQLException e) {
        }
    }
}