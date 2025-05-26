package br.com.libraryjdbc.test.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.libraryjdbc.model.dao.BookDAO;
import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.model.impl.BookDAOImpl;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import br.com.libraryjdbc.util.ErrorMessages;
import db.DB;
import db.DbException;

/**
 * Comprehensive test class for exception handling validation.
 * US-009: Tratamento de Exceções - Testing enhanced error handling
 */
public class TestExceptionHandling {

    private static final Logger logger = Logger.getLogger(TestExceptionHandling.class.getName());
    private static CategoryDAO categoryDAO;
    private static BookDAO bookDAO;

    public static void main(String[] args) {
        try {
            // Setup logging level for detailed output
            Logger.getLogger("br.com.libraryjdbc").setLevel(Level.ALL);

            DB.getConnection();
            System.out.println("✅ Test connection established successfully!");

            categoryDAO = new CategoryDAOImpl();
            bookDAO = new BookDAOImpl();

            // Clean test data before starting
            cleanTestData();

            // Run exception handling tests
            testCategoryExceptionHandling();
            testBookExceptionHandling();
            testTransactionRollback();
            testDetailedLogging();

            System.out.println("\n🎉 All exception handling tests completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in exception handling tests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test data
            cleanTestData();
            DB.closeConnection();
            System.out.println("✅ Test connection closed and cleanup completed!");
        }
    }

    // ==================== CATEGORY EXCEPTION TESTS ====================

    private static void testCategoryExceptionHandling() {
        System.out.println("\n=== CATEGORY EXCEPTION HANDLING TESTS ===");

        // Test 1: Specific error messages
        testCategorySpecificMessages();

        // Test 2: Validation sequences
        testCategoryValidationSequence();

        // Test 3: Business rule violations
        testCategoryBusinessRules();
    }

    private static void testCategorySpecificMessages() {
        System.out.println("\n--- Category Specific Error Messages ---");

        // Test empty name
        try {
            Category emptyName = new Category("", "Valid description");
            categoryDAO.save(emptyName);
            System.out.println("❌ VALIDATION FAILURE: Empty name was allowed!");
        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.CATEGORY_NAME_EMPTY)) {
                System.out.println("✅ Specific error message for empty name: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong error message: " + e.getMessage());
            }
        }

        // Test empty description
        try {
            Category emptyDesc = new Category("Valid Name", "");
            categoryDAO.save(emptyDesc);
            System.out.println("❌ VALIDATION FAILURE: Empty description was allowed!");
        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.CATEGORY_DESCRIPTION_EMPTY)) {
                System.out.println("✅ Specific error message for empty description: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong error message: " + e.getMessage());
            }
        }

        // Test duplicate name
        try {
            // First, create a valid category
            Category first = categoryDAO.save(new Category("Exception Test", "First category"));

            // Then try to create duplicate
            Category duplicate = new Category("Exception Test", "Different description");
            categoryDAO.save(duplicate);
            System.out.println("❌ VALIDATION FAILURE: Duplicate name was allowed!");
        } catch (DbException e) {
            String expectedMsg = String.format(ErrorMessages.CATEGORY_NAME_EXISTS, "Exception Test");
            if (e.getMessage().equals(expectedMsg)) {
                System.out.println("✅ Specific error message for duplicate name: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong error message: " + e.getMessage());
            }
        }
    }

    private static void testCategoryValidationSequence() {
        System.out.println("\n--- Category Validation Sequence ---");

        // Test null category
        try {
            categoryDAO.save(null);
            System.out.println("❌ VALIDATION FAILURE: Null category was allowed!");
        } catch (DbException e) {
            if (e.getMessage().contains("cannot be null")) {
                System.out.println("✅ Null category validation: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected null validation message: " + e.getMessage());
            }
        }

        // Test update with null ID
        try {
            Category nullId = new Category("Valid Name", "Valid Description");
            nullId.setId(null);
            categoryDAO.update(nullId);
            System.out.println("❌ VALIDATION FAILURE: Update with null ID was allowed!");
        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.CATEGORY_ID_NULL_UPDATE)) {
                System.out.println("✅ Null ID update validation: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong null ID message: " + e.getMessage());
            }
        }
    }

    private static void testCategoryBusinessRules() {
        System.out.println("\n--- Category Business Rules ---");

        try {
            // Create category with books to test removal constraint
            Category categoryWithBooks = categoryDAO.save(new Category("HasBooks", "Category that will have books"));

            // Create a book for this category
            Book testBook = new Book("Test Book", "Test Author", "Test Synopsis", "TEST12345", 2000, categoryWithBooks);
            bookDAO.save(testBook);

            // Try to remove category with books
            categoryDAO.remove(categoryWithBooks.getId());
            System.out.println("❌ BUSINESS RULE FAILURE: Category with books was removed!");

        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.CATEGORY_HAS_BOOKS)) {
                System.out.println("✅ Category with books protection: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong business rule message: " + e.getMessage());
            }
        }

        // Test remove non-existing category
        try {
            categoryDAO.remove(999999L);
            System.out.println("❌ VALIDATION FAILURE: Remove non-existing category was allowed!");
        } catch (DbException e) {
            String expectedMsg = String.format(ErrorMessages.CATEGORY_NOT_FOUND, 999999L);
            if (e.getMessage().equals(expectedMsg)) {
                System.out.println("✅ Non-existing category removal: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong non-existing message: " + e.getMessage());
            }
        }
    }

    // ==================== BOOK EXCEPTION TESTS ====================

    private static void testBookExceptionHandling() {
        System.out.println("\n=== BOOK EXCEPTION HANDLING TESTS ===");

        // Test 1: Specific error messages
        testBookSpecificMessages();

        // Test 2: Business rule validations
        testBookBusinessRules();

        // Test 3: Complex validations
        testBookComplexValidations();
    }

    private static void testBookSpecificMessages() {
        System.out.println("\n--- Book Specific Error Messages ---");

        Category validCategory = null;
        try {
            validCategory = categoryDAO.save(new Category("Book Test Category", "For book testing"));
        } catch (Exception e) {
            System.out.println("Setup error: " + e.getMessage());
            return;
        }

        // Test empty title
        try {
            Book emptyTitle = new Book("", "Valid Author", "Synopsis", "ISBN123", 2000, validCategory);
            bookDAO.save(emptyTitle);
            System.out.println("❌ VALIDATION FAILURE: Empty title was allowed!");
        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.BOOK_TITLE_EMPTY)) {
                System.out.println("✅ Specific error message for empty title: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong error message: " + e.getMessage());
            }
        }

        // Test invalid year
        try {
            Book invalidYear = new Book("Valid Title", "Valid Author", "Synopsis", "ISBN456", 1950, validCategory);
            bookDAO.save(invalidYear);
            System.out.println("❌ VALIDATION FAILURE: Invalid year was allowed!");
        } catch (DbException e) {
            String expectedMsg = String.format(ErrorMessages.BOOK_YEAR_INVALID, 1950);
            if (e.getMessage().equals(expectedMsg)) {
                System.out.println("✅ Specific error message for invalid year: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong error message: " + e.getMessage());
            }
        }

        // Test null year
        try {
            Book nullYear = new Book("Valid Title", "Valid Author", "Synopsis", "ISBN789", null, validCategory);
            bookDAO.save(nullYear);
            System.out.println("❌ VALIDATION FAILURE: Null year was allowed!");
        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.BOOK_YEAR_NULL)) {
                System.out.println("✅ Specific error message for null year: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong error message: " + e.getMessage());
            }
        }
    }

    private static void testBookBusinessRules() {
        System.out.println("\n--- Book Business Rules ---");

        Category validCategory = null;
        try {
            validCategory = categoryDAO.save(new Category("Business Rule Category", "For business rule testing"));
        } catch (Exception e) {
            System.out.println("Setup error: " + e.getMessage());
            return;
        }

        // Test duplicate ISBN
        try {
            Book first = bookDAO.save(new Book("First Book", "Author", "Synopsis", "DUPLICATE123", 2000, validCategory));
            Book duplicate = new Book("Second Book", "Author", "Synopsis", "DUPLICATE123", 2001, validCategory);
            bookDAO.save(duplicate);
            System.out.println("❌ BUSINESS RULE FAILURE: Duplicate ISBN was allowed!");
        } catch (DbException e) {
            String expectedMsg = String.format(ErrorMessages.BOOK_ISBN_EXISTS, "DUPLICATE123");
            if (e.getMessage().equals(expectedMsg)) {
                System.out.println("✅ Duplicate ISBN protection: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong duplicate ISBN message: " + e.getMessage());
            }
        }

        // Test invalid category
        try {
            Category invalidCategory = new Category();
            invalidCategory.setId(999999L);
            Book invalidCategoryBook = new Book("Title", "Author", "Synopsis", "INVALID456", 2000, invalidCategory);
            bookDAO.save(invalidCategoryBook);
            System.out.println("❌ BUSINESS RULE FAILURE: Invalid category was allowed!");
        } catch (DbException e) {
            String expectedMsg = String.format(ErrorMessages.BOOK_CATEGORY_NOT_EXISTS, 999999L);
            if (e.getMessage().equals(expectedMsg)) {
                System.out.println("✅ Invalid category protection: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong invalid category message: " + e.getMessage());
            }
        }
    }

    private static void testBookComplexValidations() {
        System.out.println("\n--- Book Complex Validations ---");

        // Test null book
        try {
            bookDAO.save(null);
            System.out.println("❌ VALIDATION FAILURE: Null book was allowed!");
        } catch (DbException e) {
            if (e.getMessage().contains("cannot be null")) {
                System.out.println("✅ Null book validation: " + e.getMessage());
            } else {
                System.out.println("❌ Unexpected null validation message: " + e.getMessage());
            }
        }

        // Test book with null category
        try {
            Book nullCategory = new Book("Title", "Author", "Synopsis", "NULL789", 2000, null);
            bookDAO.save(nullCategory);
            System.out.println("❌ VALIDATION FAILURE: Book with null category was allowed!");
        } catch (DbException e) {
            if (e.getMessage().equals(ErrorMessages.BOOK_CATEGORY_INVALID)) {
                System.out.println("✅ Null category validation: " + e.getMessage());
            } else {
                System.out.println("❌ Wrong null category message: " + e.getMessage());
            }
        }
    }

    // ==================== TRANSACTION ROLLBACK TESTS ====================

    private static void testTransactionRollback() {
        System.out.println("\n=== TRANSACTION ROLLBACK TESTS ===");

        System.out.println("\n--- Testing Automatic Rollback ---");

        try {
            // Attempt to save invalid category - should rollback
            Category invalidCategory = new Category("", ""); // Both empty
            categoryDAO.save(invalidCategory);
            System.out.println("❌ TRANSACTION FAILURE: Invalid save completed without rollback!");
        } catch (DbException e) {
            System.out.println("✅ Transaction rolled back for invalid category: " + e.getMessage());

            // Verify no partial data was saved
            try {
                var categories = categoryDAO.findAll();
                boolean hasEmptyName = categories.stream().anyMatch(c -> c.getName() == null || c.getName().isEmpty());
                if (!hasEmptyName) {
                    System.out.println("✅ Rollback confirmed - no partial data saved");
                } else {
                    System.out.println("❌ Rollback failed - partial data found");
                }
            } catch (Exception verifyEx) {
                System.out.println("⚠️ Cannot verify rollback: " + verifyEx.getMessage());
            }
        }
    }

    // ==================== DETAILED LOGGING TESTS ====================

    private static void testDetailedLogging() {
        System.out.println("\n=== DETAILED LOGGING TESTS ===");

        System.out.println("\n--- Testing Log Levels ---");

        try {
            // This should generate INFO, FINE, and potentially WARNING logs
            Category logTest = categoryDAO.save(new Category("Log Test", "Testing logging functionality"));
            System.out.println("✅ Logging test category created (check logs for detailed output)");

            // This should generate WARNING and SEVERE logs
            try {
                Category duplicate = new Category("Log Test", "Duplicate for logging");
                categoryDAO.save(duplicate);
            } catch (DbException e) {
                System.out.println("✅ Logging test exception caught (check logs for detailed error logging)");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Logging test setup error: " + e.getMessage());
        }

        System.out.println("📝 Note: Check console output for detailed logging information");
        System.out.println("📝 Expected log levels: INFO (operations), FINE (SQL), WARNING (validations), SEVERE (errors)");
    }

    // ==================== HELPER METHODS ====================

    private static void cleanTestData() {
        try {
            // Clean books first (FK dependency)
            var books = bookDAO.findAll();
            books.stream()
                    .filter(book -> book.getIsbn().contains("TEST") || book.getIsbn().contains("DUPLICATE") || book.getIsbn().contains("INVALID") || book.getIsbn().contains("NULL"))
                    .forEach(book -> {
                        try {
                            bookDAO.remove(book.getId());
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    });

            // Clean test categories
            var categories = categoryDAO.findAll();
            categories.stream()
                    .filter(cat -> cat.getName().contains("Test") || cat.getName().contains("Exception") || cat.getName().contains("HasBooks") || cat.getName().contains("Business") || cat.getName().contains("Log"))
                    .forEach(cat -> {
                        try {
                            categoryDAO.remove(cat.getId());
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    });

        } catch (Exception e) {
            // Ignore cleanup errors - they're not critical for tests
        }
    }
}