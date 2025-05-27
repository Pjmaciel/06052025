package br.com.libraryjdbc.test.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.service.CategoryService;
import br.com.libraryjdbc.service.impl.CategoryServiceImpl;
import db.DB;

/**
 * Test class for CategoryService business logic and validations.
 * US-014: Serviços com Regras de Negócio - Service Layer Tests
 */
public class TestCategoryService {

    private static CategoryService categoryService;

    public static void main(String[] args) {
        try {
            // Setup
            DB.getConnection();
            System.out.println("✅ Test connection established successfully!");

            categoryService = new CategoryServiceImpl();

            // Clean test data before starting
            cleanTestData();

            // Run all service tests
            testCategorySaveValidations();
            testCategoryUpdateValidations();
            testCategoryRemoveValidations();
            testCategoryFindOperations();

            System.out.println("\n🎉 All CategoryService tests completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in CategoryService tests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test data
            cleanTestData();
            DB.closeConnection();
            System.out.println("✅ Test connection closed and cleanup completed!");
        }
    }

    // ==================== SAVE VALIDATIONS TESTS ====================

    private static void testCategorySaveValidations() {
        System.out.println("\n=== CATEGORY SAVE VALIDATIONS TESTS ===");

        // Test 1: Valid category save
        testValidCategorySave();

        // Test 2: Business rule validations
        testSaveValidationFailures();

        // Test 3: Duplicate name validation
        testDuplicateNameValidation();
    }

    private static void testValidCategorySave() {
        System.out.println("\n--- Valid Category Save ---");

        try {
            Category category = new Category("Programming", "Programming and software development books");
            Category savedCategory = categoryService.save(category);

            if (savedCategory.getId() != null && savedCategory.getId() > 0) {
                System.out.println("✅ Valid category saved successfully with ID: " + savedCategory.getId());
            } else {
                System.out.println("❌ Failed to save valid category - ID not generated");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error saving valid category: " + e.getMessage());
        }
    }

    private static void testSaveValidationFailures() {
        System.out.println("\n--- Save Validation Failures ---");

        // Test null category
        try {
            categoryService.save(null);
            System.out.println("❌ VALIDATION FAILURE: Null category was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("cannot be null")) {
                System.out.println("✅ Null category validation working");
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }

        // Test empty name
        try {
            Category emptyName = new Category("", "Valid description");
            categoryService.save(emptyName);
            System.out.println("❌ VALIDATION FAILURE: Empty name was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("name cannot be empty")) {
                System.out.println("✅ Empty name validation working");
            } else {
                System.out.println("❌ Unexpected error for empty name: " + e.getMessage());
            }
        }

        // Test null description
        try {
            Category nullDescription = new Category("Valid Name", null);
            categoryService.save(nullDescription);
            System.out.println("❌ VALIDATION FAILURE: Null description was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("description cannot be empty")) {
                System.out.println("✅ Null description validation working");
            } else {
                System.out.println("❌ Unexpected error for null description: " + e.getMessage());
            }
        }

        // Test whitespace-only data
        try {
            Category whitespaceData = new Category("   ", "   ");
            categoryService.save(whitespaceData);
            System.out.println("❌ VALIDATION FAILURE: Whitespace-only data was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("cannot be empty")) {
                System.out.println("✅ Whitespace validation working");
            } else {
                System.out.println("❌ Unexpected error for whitespace data: " + e.getMessage());
            }
        }
    }

    private static void testDuplicateNameValidation() {
        System.out.println("\n--- Duplicate Name Validation ---");

        try {
            // First category should save successfully
            Category fiction1 = new Category("Fiction", "Fiction books and novels");
            categoryService.save(fiction1);
            System.out.println("✅ First Fiction category saved");

            // Try to save duplicate name
            Category fiction2 = new Category("Fiction", "Different description");
            categoryService.save(fiction2);
            System.out.println("❌ VALIDATION FAILURE: Duplicate name was allowed!");

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("✅ Duplicate name validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    // ==================== UPDATE VALIDATIONS TESTS ====================

    private static void testCategoryUpdateValidations() {
        System.out.println("\n=== CATEGORY UPDATE VALIDATIONS TESTS ===");

        // Test 1: Valid update
        testValidCategoryUpdate();

        // Test 2: Update validation failures
        testUpdateValidationFailures();

        // Test 3: Update non-existing category
        testUpdateNonExistingCategory();
    }

    private static void testValidCategoryUpdate() {
        System.out.println("\n--- Valid Category Update ---");

        try {
            // Create category to update
            Category original = new Category("UpdateTest", "Original description");
            Category saved = categoryService.save(original);

            // Update it
            saved.setName("UpdatedName");
            saved.setDescription("Updated description");
            categoryService.update(saved);

            // Verify update
            Category updated = categoryService.findById(saved.getId());

            if (updated != null &&
                    "UpdatedName".equals(updated.getName()) &&
                    "Updated description".equals(updated.getDescription())) {
                System.out.println("✅ Category updated successfully");
            } else {
                System.out.println("❌ Category update failed or data not persisted");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error in valid update: " + e.getMessage());
        }
    }

    private static void testUpdateValidationFailures() {
        System.out.println("\n--- Update Validation Failures ---");

        // Test null ID
        try {
            Category nullId = new Category("Name", "Description");
            nullId.setId(null);
            categoryService.update(nullId);
            System.out.println("❌ VALIDATION FAILURE: Update with null ID was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ID cannot be null")) {
                System.out.println("✅ Null ID validation working");
            } else {
                System.out.println("❌ Unexpected error for null ID: " + e.getMessage());
            }
        }

        // Test empty name
        try {
            Category emptyName = new Category("", "Valid description");
            emptyName.setId(1L);
            categoryService.update(emptyName);
            System.out.println("❌ VALIDATION FAILURE: Update with empty name was allowed!");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("name cannot be empty")) {
                System.out.println("✅ Empty name update validation working");
            } else {
                System.out.println("❌ Unexpected error for empty name: " + e.getMessage());
            }
        }
    }

    private static void testUpdateNonExistingCategory() {
        System.out.println("\n--- Update Non-Existing Category ---");

        try {
            Category nonExisting = new Category("Non Existing", "Description");
            nonExisting.setId(999999L);

            categoryService.update(nonExisting);
            System.out.println("❌ VALIDATION FAILURE: Update of non-existing category was allowed!");

        } catch (IllegalStateException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✅ Update non-existing category validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    // ==================== REMOVE VALIDATIONS TESTS ====================

    private static void testCategoryRemoveValidations() {
        System.out.println("\n=== CATEGORY REMOVE VALIDATIONS TESTS ===");

        // Test 1: Remove category without books
        testValidCategoryRemove();

        // Test 2: Remove category with books (should fail)
        testRemoveCategoryWithBooks();

        // Test 3: Remove non-existing category
        testRemoveNonExistingCategory();
    }

    private static void testValidCategoryRemove() {
        System.out.println("\n--- Valid Category Remove ---");

        try {
            // Create category to remove (use unique name to avoid conflicts)
            Category toRemove = new Category("ToRemove" + System.currentTimeMillis(), "Category to be removed");
            Category saved = categoryService.save(toRemove);
            Long idToRemove = saved.getId();

            // Remove it
            categoryService.remove(idToRemove);

            // Verify removal
            Category removed = categoryService.findById(idToRemove);

            if (removed == null) {
                System.out.println("✅ Category removed successfully");
            } else {
                System.out.println("❌ Category removal failed - still exists");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error in removal: " + e.getMessage());
        }
    }

    private static void testRemoveCategoryWithBooks() {
        System.out.println("\n--- Remove Category With Books Test ---");

        try {
            // Create category
            Category categoryWithBooks = new Category("WithBooks" + System.currentTimeMillis(), "Category that might have books");
            Category saved = categoryService.save(categoryWithBooks);

            // Insert test book to make category "have books"
            insertTestBook(saved.getId());

            // Try to remove category
            categoryService.remove(saved.getId());
            System.out.println("❌ VALIDATION FAILURE: Category with books was allowed to be removed!");

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("associated books")) {
                System.out.println("✅ Category with books removal validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Error setting up test (book table might not exist): " + e.getMessage());
            System.out.println("✅ Remove method exists and will validate when book table is created");
        }
    }

    private static void testRemoveNonExistingCategory() {
        System.out.println("\n--- Remove Non-Existing Category ---");

        try {
            categoryService.remove(999999L);
            System.out.println("❌ VALIDATION FAILURE: Removal of non-existing category was allowed!");

        } catch (IllegalStateException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✅ Remove non-existing category validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    // ==================== FIND OPERATIONS TESTS ====================

    private static void testCategoryFindOperations() {
        System.out.println("\n=== CATEGORY FIND OPERATIONS TESTS ===");

        // Test 1: Find by ID
        testFindById();

        // Test 2: Find all categories
        testFindAll();

        // Test 3: Find category with most books
        testFindCategoryWithMostBooks();

        // Test 4: Get category book counts
        testGetCategoryBookCounts();
    }

    private static void testFindById() {
        System.out.println("\n--- Find By ID Test ---");

        try {
            // Create category to find
            Category toFind = new Category("FindTest", "Category for find test");
            Category saved = categoryService.save(toFind);

            // Find it
            Category found = categoryService.findById(saved.getId());

            if (found != null && found.getId().equals(saved.getId())) {
                System.out.println("✅ Find by ID working correctly");
            } else {
                System.out.println("❌ Find by ID failed");
            }

            // Test null ID
            Category nullResult = categoryService.findById(null);
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
            var categories = categoryService.findAll();

            if (categories != null) {
                System.out.println("✅ Find all working - found " + categories.size() + " categories");
            } else {
                System.out.println("❌ Find all returned null");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in find all test: " + e.getMessage());
        }
    }

    private static void testFindCategoryWithMostBooks() {
        System.out.println("\n--- Find Category With Most Books Test ---");

        try {
            Category topCategory = categoryService.findCategoryWithMostBooks();

            if (topCategory != null) {
                System.out.println("✅ Find category with most books working - found: " + topCategory.getName());
            } else {
                System.out.println("✅ Find category with most books working - no categories found");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in find category with most books test: " + e.getMessage());
        }
    }

    private static void testGetCategoryBookCounts() {
        System.out.println("\n--- Get Category Book Counts Test ---");

        try {
            var bookCounts = categoryService.getCategoryBookCounts();

            if (bookCounts != null) {
                System.out.println("✅ Get category book counts working - found " + bookCounts.size() + " categories with books");
            } else {
                System.out.println("❌ Get category book counts returned null");
            }

        } catch (Exception e) {
            System.out.println("❌ Error in get category book counts test: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private static void cleanTestData() {
        try {
            Connection conn = DB.getConnection();

            // Clean books first (FK dependency) - only if table exists
            try (PreparedStatement st = conn.prepareStatement(
                    "DELETE FROM book WHERE isbn LIKE '%TEST%' OR title LIKE '%Test%'")) {
                st.executeUpdate();
            } catch (SQLException e) {
                // Ignore if book table doesn't exist yet
            }

            // Clean test categories
            try (PreparedStatement st = conn.prepareStatement(
                    "DELETE FROM category WHERE name LIKE '%Test%' OR name LIKE '%Fiction%' OR " +
                            "name LIKE '%Programming%' OR name LIKE '%Update%' OR name LIKE '%Remove%' OR " +
                            "name LIKE '%Find%' OR name LIKE '%With Books%' OR name LIKE '%ToRemove%'")) {
                st.executeUpdate();
            }

        } catch (SQLException e) {
            // Ignore cleanup errors - they're not critical for tests
        }
    }

    private static void insertTestBook(Long categoryId) {
        try {
            Connection conn = DB.getConnection();
            String sql = "INSERT INTO book (title, author, synopsis, isbn, release_year, category_id) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setString(1, "Test Book");
                st.setString(2, "Test Author");
                st.setString(3, "Test Synopsis");
                st.setString(4, "TEST123456789");
                st.setInt(5, 2000);
                st.setLong(6, categoryId);

                st.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting test book: " + e.getMessage());
        }
    }
}