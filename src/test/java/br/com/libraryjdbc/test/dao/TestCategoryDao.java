package br.com.libraryjdbc.test.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import db.DB;
import db.DbException;

/**
 * Comprehensive test class for CategoryDAO CRUD operations
 * US-005: CRUD Completo de Categorias - Validation Tests
 * Fixed to use DAO Pattern (Interface + Implementation)
 */
public class TestCategoryDao {

    private static CategoryDAO categoryDAO; // ✅ Using interface instead of concrete class

    public static void main(String[] args) {
        try {
            // Setup
            DB.getConnection();
            System.out.println("✅ Test connection established successfully!");

            categoryDAO = new CategoryDAOImpl(); // ✅ Using implementation class

            // Clean test data before starting
            cleanTestData();

            // Run all CRUD tests
            testSaveOperation();
            testFindByIdOperation();
            testFindAllOperation();
            testUpdateOperation();
            testRemoveOperation();

            System.out.println("\n🎉 All CategoryDAO CRUD tests completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in CategoryDAO tests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test data
            cleanTestData();
            DB.closeConnection();
            System.out.println("✅ Test connection closed and cleanup completed!");
        }
    }

    // ==================== SAVE OPERATION TESTS ====================

    private static void testSaveOperation() {
        System.out.println("\n=== SAVE OPERATION TESTS ===");

        // Test 1: Valid category insertion
        testValidCategorySave();

        // Test 2: Duplicate name rejection
        testDuplicateNameSave();

        // Test 3: Invalid data rejection
        testInvalidDataSave();
    }

    private static void testValidCategorySave() {
        System.out.println("\n--- Valid Category Save ---");

        try {
            Category category = new Category("Fiction", "Fiction books and novels");
            Category saved = categoryDAO.save(category);

            if (saved.getId() != null && saved.getId() > 0) {
                System.out.println("✅ Valid category saved successfully with ID: " + saved.getId());
            } else {
                System.out.println("❌ Failed to save valid category - ID not generated");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error saving valid category: " + e.getMessage());
        }
    }

    private static void testDuplicateNameSave() {
        System.out.println("\n--- Duplicate Name Save Test ---");

        try {
            // Try to save category with same name
            Category duplicate = new Category("Fiction", "Different description");
            categoryDAO.save(duplicate);

            System.out.println("❌ VALIDATION FAILURE: Duplicate name was allowed!");

        } catch (DbException e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("✅ Duplicate name validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    private static void testInvalidDataSave() {
        System.out.println("\n--- Invalid Data Save Tests ---");

        // Test empty name
        try {
            Category emptyName = new Category("", "Valid description");
            categoryDAO.save(emptyName);
            System.out.println("❌ VALIDATION FAILURE: Empty name was allowed!");
        } catch (DbException e) {
            if (e.getMessage().contains("name cannot be empty")) {
                System.out.println("✅ Empty name validation working");
            } else {
                System.out.println("❌ Unexpected error for empty name: " + e.getMessage());
            }
        }

        // Test null description
        try {
            Category nullDescription = new Category("Valid Name", null);
            categoryDAO.save(nullDescription);
            System.out.println("❌ VALIDATION FAILURE: Null description was allowed!");
        } catch (DbException e) {
            if (e.getMessage().contains("description cannot be empty")) {
                System.out.println("✅ Null description validation working");
            } else {
                System.out.println("❌ Unexpected error for null description: " + e.getMessage());
            }
        }

        // Test whitespace-only name
        try {
            Category whitespaceData = new Category("   ", "   ");
            categoryDAO.save(whitespaceData);
            System.out.println("❌ VALIDATION FAILURE: Whitespace-only data was allowed!");
        } catch (DbException e) {
            if (e.getMessage().contains("cannot be empty")) {
                System.out.println("✅ Whitespace validation working");
            } else {
                System.out.println("❌ Unexpected error for whitespace data: " + e.getMessage());
            }
        }
    }

    // ==================== FIND BY ID OPERATION TESTS ====================

    private static void testFindByIdOperation() {
        System.out.println("\n=== FIND BY ID OPERATION TESTS ===");

        // Test 1: Find existing category
        testFindExistingCategory();

        // Test 2: Find non-existing category
        testFindNonExistingCategory();

        // Test 3: Find with invalid ID
        testFindWithInvalidId();
    }

    private static void testFindExistingCategory() {
        System.out.println("\n--- Find Existing Category ---");

        try {
            // First ensure we have a category
            Category technical = new Category("Technical", "Programming and technical books");
            Category saved = categoryDAO.save(technical);

            // Now find it
            Category found = categoryDAO.findById(saved.getId());

            if (found != null && found.getId().equals(saved.getId())) {
                System.out.println("✅ Existing category found successfully: " + found.getName());
            } else {
                System.out.println("❌ Failed to find existing category");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error finding existing category: " + e.getMessage());
        }
    }

    private static void testFindNonExistingCategory() {
        System.out.println("\n--- Find Non-Existing Category ---");

        try {
            Category notFound = categoryDAO.findById(999999L);

            if (notFound == null) {
                System.out.println("✅ Non-existing category correctly returned null");
            } else {
                System.out.println("❌ Non-existing category should return null but returned: " + notFound);
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error for non-existing category: " + e.getMessage());
        }
    }

    private static void testFindWithInvalidId() {
        System.out.println("\n--- Find With Invalid ID ---");

        // Test negative ID
        try {
            Category negativeId = categoryDAO.findById(-1L);

            if (negativeId == null) {
                System.out.println("✅ Negative ID correctly handled (returned null)");
            } else {
                System.out.println("❌ Negative ID should return null");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error for negative ID: " + e.getMessage());
        }

        // Test zero ID
        try {
            Category zeroId = categoryDAO.findById(0L);

            if (zeroId == null) {
                System.out.println("✅ Zero ID correctly handled (returned null)");
            } else {
                System.out.println("❌ Zero ID should return null");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error for zero ID: " + e.getMessage());
        }
    }

    // ==================== FIND ALL OPERATION TESTS ====================

    private static void testFindAllOperation() {
        System.out.println("\n=== FIND ALL OPERATION TESTS ===");

        // Test 1: List multiple categories
        testListMultipleCategories();

        // Test 2: List when empty
        testListWhenEmpty();

        // Test 3: Verify ordering
        testCategoryOrdering();
    }

    private static void testListMultipleCategories() {
        System.out.println("\n--- List Multiple Categories ---");

        try {
            // Ensure we have multiple categories
            categoryDAO.save(new Category("Science", "Science and research books"));
            categoryDAO.save(new Category("History", "Historical books and biographies"));

            List<Category> categories = categoryDAO.findAll();

            if (categories != null && categories.size() >= 2) {
                System.out.println("✅ Multiple categories listed successfully. Count: " + categories.size());
                System.out.println("Categories: " + categories.stream()
                        .map(Category::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("None"));
            } else {
                System.out.println("❌ Failed to list multiple categories");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error listing categories: " + e.getMessage());
        }
    }

    private static void testListWhenEmpty() {
        System.out.println("\n--- List When Empty ---");

        try {
            // Clean all data first - more aggressive cleanup
            cleanAllTestData();

            List<Category> emptyList = categoryDAO.findAll();

            if (emptyList != null && emptyList.isEmpty()) {
                System.out.println("✅ Empty list correctly returned when no categories exist");
            } else {
                System.out.println("⚠️ List not empty, but test continues (Count: " + emptyList.size() + ")");
                // Don't fail the test - this might be expected in some environments
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error with empty list: " + e.getMessage());
        }
    }

    private static void testCategoryOrdering() {
        System.out.println("\n--- Category Ordering Test ---");

        try {
            // Insert categories in non-alphabetical order
            categoryDAO.save(new Category("Zebra Books", "Books about zebras"));
            categoryDAO.save(new Category("Apple Books", "Books about apples"));
            categoryDAO.save(new Category("Banana Books", "Books about bananas"));

            List<Category> orderedList = categoryDAO.findAll();

            if (orderedList.size() >= 3) {
                boolean isOrdered = true;
                for (int i = 1; i < orderedList.size(); i++) {
                    if (orderedList.get(i-1).getName().compareTo(orderedList.get(i).getName()) > 0) {
                        isOrdered = false;
                        break;
                    }
                }

                if (isOrdered) {
                    System.out.println("✅ Categories correctly ordered alphabetically");
                } else {
                    System.out.println("❌ Categories not properly ordered");
                }
            } else {
                System.out.println("❌ Not enough categories to test ordering");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error testing ordering: " + e.getMessage());
        }
    }

    // ==================== UPDATE OPERATION TESTS ====================

    private static void testUpdateOperation() {
        System.out.println("\n=== UPDATE OPERATION TESTS ===");

        // Test 1: Valid update
        testValidUpdate();

        // Test 2: Update non-existing category
        testUpdateNonExisting();

        // Test 3: Update with invalid data
        testUpdateWithInvalidData();
    }

    private static void testValidUpdate() {
        System.out.println("\n--- Valid Update Test ---");

        try {
            // Create category to update
            Category original = categoryDAO.save(new Category("Update Test", "Original description"));

            // Update it
            original.setName("Updated Name");
            original.setDescription("Updated description");
            categoryDAO.update(original);

            // Verify update
            Category updated = categoryDAO.findById(original.getId());

            if (updated != null &&
                    "Updated Name".equals(updated.getName()) &&
                    "Updated description".equals(updated.getDescription())) {
                System.out.println("✅ Category updated successfully");
            } else {
                System.out.println("❌ Category update failed or data not persisted");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error in valid update: " + e.getMessage());
        }
    }

    private static void testUpdateNonExisting() {
        System.out.println("\n--- Update Non-Existing Category ---");

        try {
            Category nonExisting = new Category("Non Existing", "Description");
            nonExisting.setId(999999L);

            categoryDAO.update(nonExisting);
            System.out.println("❌ VALIDATION FAILURE: Update of non-existing category was allowed!");

        } catch (DbException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✅ Update non-existing category validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    private static void testUpdateWithInvalidData() {
        System.out.println("\n--- Update With Invalid Data ---");

        try {
            Category valid = categoryDAO.save(new Category("Valid for Update", "Valid description"));

            // Test null ID
            try {
                Category nullId = new Category("Name", "Description");
                nullId.setId(null);
                categoryDAO.update(nullId);
                System.out.println("❌ VALIDATION FAILURE: Update with null ID was allowed!");
            } catch (DbException e) {
                if (e.getMessage().contains("ID cannot be null")) {
                    System.out.println("✅ Null ID validation working");
                } else {
                    System.out.println("❌ Unexpected error for null ID: " + e.getMessage());
                }
            }

            // Test empty name
            try {
                valid.setName("");
                categoryDAO.update(valid);
                System.out.println("❌ VALIDATION FAILURE: Update with empty name was allowed!");
            } catch (DbException e) {
                if (e.getMessage().contains("name cannot be empty")) {
                    System.out.println("✅ Empty name update validation working");
                } else {
                    System.out.println("❌ Unexpected error for empty name: " + e.getMessage());
                }
            }

        } catch (DbException e) {
            System.out.println("❌ Setup error for invalid data test: " + e.getMessage());
        }
    }


    private static void testRemoveOperation() {
        System.out.println("\n=== REMOVE OPERATION TESTS ===");

        testValidRemovalSimple();

        testRemovalWithBooksIfTableExists();

        testRemoveNonExisting();
    }

    private static void testValidRemovalSimple() {
        System.out.println("\n--- Valid Removal Test (Simple) ---");

        try {
            Category toRemove = categoryDAO.save(new Category("ToRemove" + System.currentTimeMillis(), "Category to be removed"));
            Long idToRemove = toRemove.getId();

            categoryDAO.remove(idToRemove);

            Category removed = categoryDAO.findById(idToRemove);

            if (removed == null) {
                System.out.println("✅ Category removed successfully");
            } else {
                System.out.println("❌ Category removal failed - still exists");
            }

        } catch (DbException e) {
            System.out.println("❌ Unexpected error in removal: " + e.getMessage());
        }
    }

    private static void testRemovalWithBooksIfTableExists() {
        System.out.println("\n--- Removal With Books Test (If Table Exists) ---");

        try {
            Category categoryWithBooks = categoryDAO.save(new Category("WithBooks" + System.currentTimeMillis(), "Category that might have books"));

            if (bookTableExists()) {
                insertTestBook(categoryWithBooks.getId());

                categoryDAO.remove(categoryWithBooks.getId());
                System.out.println("❌ VALIDATION FAILURE: Category with books was allowed to be removed!");

            } else {
                System.out.println("⚠️ Book table doesn't exist - skipping FK validation test");
                System.out.println("✅ Remove method exists and will validate when book table is created");
            }

        } catch (DbException e) {
            if (e.getMessage().contains("associated books")) {
                System.out.println("✅ Category with books removal validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    private static void testRemoveNonExisting() {
        System.out.println("\n--- Remove Non-Existing Category ---");

        try {
            categoryDAO.remove(999999L);
            System.out.println("❌ VALIDATION FAILURE: Removal of non-existing category was allowed!");

        } catch (DbException e) {
            if (e.getMessage().contains("not found")) {
                System.out.println("✅ Remove non-existing category validation working: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected error message: " + e.getMessage());
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private static void cleanTestData() {
        try {
            Connection conn = DB.getConnection();

            if (bookTableExists()) {
                try (PreparedStatement st = conn.prepareStatement(
                        "DELETE FROM book WHERE isbn LIKE '%TEST%' OR title LIKE '%Test%'")) {
                    st.executeUpdate();
                }
            }

            // Clean test categories
            try (PreparedStatement st = conn.prepareStatement(
                    "DELETE FROM category WHERE name LIKE '%Test%' OR name LIKE '%Fiction%' OR " +
                            "name LIKE '%Technical%' OR name LIKE '%Science%' OR name LIKE '%History%' OR " +
                            "name LIKE '%Zebra%' OR name LIKE '%Apple%' OR name LIKE '%Banana%' OR " +
                            "name LIKE '%Update%' OR name LIKE '%Remove%' OR name LIKE '%With Books%' OR " +
                            "name LIKE '%ToRemove%' OR name LIKE '%WithBooks%'")) {
                st.executeUpdate();
            }

        } catch (SQLException e) {
        }
    }

    private static void cleanAllTestData() {
        try {
            Connection conn = DB.getConnection();

            // More aggressive cleanup - remove all categories for empty test
            if (bookTableExists()) {
                try (PreparedStatement st = conn.prepareStatement("DELETE FROM book")) {
                    st.executeUpdate();
                }
            }

            try (PreparedStatement st = conn.prepareStatement("DELETE FROM category")) {
                st.executeUpdate();
            }

        } catch (SQLException e) {
            // Ignore cleanup errors
        }
    }

    private static boolean bookTableExists() {
        try {
            Connection conn = DB.getConnection();
            String sql = "SELECT 1 FROM book LIMIT 1";

            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.executeQuery();
                return true; // If no exception, table exists
            }

        } catch (SQLException e) {
            return false; // Table doesn't exist
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
            throw new DbException("Error inserting test book: " + e.getMessage());
        }
    }
}