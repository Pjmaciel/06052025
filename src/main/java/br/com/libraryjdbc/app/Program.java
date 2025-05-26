package br.com.libraryjdbc.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.libraryjdbc.model.dao.BookDAO;
import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.model.impl.BookDAOImpl;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import db.DB;
import db.DbException;

/**
 * Demonstration of enhanced exception handling in Library Management System.
 * US-009: Tratamento de Exceções - Enhanced error handling demo
 */
public class Program {

    private static final Logger logger = Logger.getLogger(Program.class.getName());

    public static void main(String[] args) {
        // Enable detailed logging for demonstration
        Logger.getLogger("br.com.libraryjdbc").setLevel(Level.ALL);

        try {
            DB.getConnection();
            System.out.println("✅ Database connected successfully!");
            System.out.println("📝 Enhanced Exception Handling Demo");
            System.out.println("=" .repeat(50));

            CategoryDAO categoryDAO = new CategoryDAOImpl();
            BookDAO bookDAO = new BookDAOImpl();

            // Demo 1: Valid Operations (Success Path)
            demonstrateValidOperations(categoryDAO, bookDAO);

            // Demo 2: Specific Error Messages
            demonstrateSpecificErrorMessages(categoryDAO, bookDAO);

            // Demo 3: Transaction Rollback
            demonstrateTransactionRollback(categoryDAO);

            // Demo 4: Detailed Logging
            demonstrateDetailedLogging(categoryDAO);

            System.out.println("\n🎉 Exception handling demonstration completed!");
            System.out.println("📝 Check console output for detailed logging information");

        } catch (Exception e) {
            System.err.println("❌ Demo error: " + e.getMessage());
            logger.log(Level.SEVERE, "Demo execution error", e);
        } finally {
            DB.closeConnection();
            System.out.println("✅ Database connection closed.");
        }
    }

    private static void demonstrateValidOperations(CategoryDAO categoryDAO, BookDAO bookDAO) {
        System.out.println("\n🟢 DEMO 1: Valid Operations (Success Path)");
        System.out.println("-".repeat(40));

        try {
            // Create valid category - should succeed with detailed logging
            Category category = new Category("Programming", "Programming and software development books");
            Category savedCategory = categoryDAO.save(category);
            System.out.println("SUCCESS: Category created with ID " + savedCategory.getId());

            // Create valid book - should succeed with transaction logging
            Book book = new Book("Clean Architecture", "Robert C. Martin",
                    "A guide to software architecture", "9780134494166", 2017, savedCategory);
            Book savedBook = bookDAO.save(book);
            System.out.println("SUCCESS: Book created with ID " + savedBook.getId());

        } catch (DbException e) {
            System.out.println("UNEXPECTED ERROR: " + e.getMessage());
        }
    }

    private static void demonstrateSpecificErrorMessages(CategoryDAO categoryDAO, BookDAO bookDAO) {
        System.out.println("\n🔴 DEMO 2: Specific Error Messages");
        System.out.println("-".repeat(40));

        // Demo 2.1: Category duplicate name
        System.out.println("\n📌 Testing duplicate category name:");
        try {
            Category duplicate = new Category("Programming", "Different description");
            categoryDAO.save(duplicate);
            System.out.println("ERROR: Should have failed!");
        } catch (DbException e) {
            System.out.println("✅ EXPECTED ERROR: " + e.getMessage());
        }

        // Demo 2.2: Book invalid year
        System.out.println("\n📌 Testing invalid book year:");
        try {
            Category validCategory = categoryDAO.findAll().get(0); // Use existing category
            Book invalidYear = new Book("Old Book", "Author", "Synopsis", "OLD123", 1950, validCategory);
            bookDAO.save(invalidYear);
            System.out.println("ERROR: Should have failed!");
        } catch (DbException e) {
            System.out.println("✅ EXPECTED ERROR: " + e.getMessage());
        }

        // Demo 2.3: Book duplicate ISBN
        System.out.println("\n📌 Testing duplicate ISBN:");
        try {
            Category validCategory = categoryDAO.findAll().get(0); // Use existing category
            Book duplicateIsbn = new Book("Another Book", "Author", "Synopsis", "9780134494166", 2020, validCategory);
            bookDAO.save(duplicateIsbn);
            System.out.println("ERROR: Should have failed!");
        } catch (DbException e) {
            System.out.println("✅ EXPECTED ERROR: " + e.getMessage());
        }

        // Demo 2.4: Empty category name
        System.out.println("\n📌 Testing empty category name:");
        try {
            Category emptyName = new Category("", "Valid description");
            categoryDAO.save(emptyName);
            System.out.println("ERROR: Should have failed!");
        } catch (DbException e) {
            System.out.println("✅ EXPECTED ERROR: " + e.getMessage());
        }
    }

    private static void demonstrateTransactionRollback(CategoryDAO categoryDAO) {
        System.out.println("\n🔄 DEMO 3: Transaction Rollback");
        System.out.println("-".repeat(40));

        System.out.println("\n📌 Testing automatic rollback on validation failure:");

        // Count categories before failed operation
        int categoriesBefore = categoryDAO.findAll().size();
        System.out.println("Categories before failed operation: " + categoriesBefore);

        try {
            // This should fail validation and trigger rollback
            Category invalidCategory = new Category(null, "This should fail");
            categoryDAO.save(invalidCategory);
            System.out.println("ERROR: Should have failed!");
        } catch (DbException e) {
            System.out.println("✅ EXPECTED ERROR: " + e.getMessage());

            // Verify no partial data was saved (transaction rolled back)
            int categoriesAfter = categoryDAO.findAll().size();
            System.out.println("Categories after failed operation: " + categoriesAfter);

            if (categoriesBefore == categoriesAfter) {
                System.out.println("✅ TRANSACTION ROLLBACK CONFIRMED: No partial data saved");
            } else {
                System.out.println("❌ ROLLBACK FAILED: Partial data detected");
            }
        }
    }

    private static void demonstrateDetailedLogging(CategoryDAO categoryDAO) {
        System.out.println("\n📝 DEMO 4: Detailed Logging");
        System.out.println("-".repeat(40));

        System.out.println("\n📌 Performing operations with detailed logging:");
        System.out.println("(Check console output for INFO, FINE, WARNING, SEVERE logs)");

        try {
            // This operation will generate multiple log levels:
            // INFO: Operation start
            // FINE: SQL execution with parameters
            // INFO: Success confirmation
            Category logDemo = new Category("Logging Demo", "Category for logging demonstration");
            categoryDAO.save(logDemo);
            System.out.println("SUCCESS: Logging demo category created");

            // This will generate WARNING and SEVERE logs
            try {
                Category duplicate = new Category("Logging Demo", "Duplicate for error logging");
                categoryDAO.save(duplicate);
            } catch (DbException e) {
                System.out.println("EXPECTED: Error logged with WARNING/SEVERE levels");
            }

        } catch (DbException e) {
            System.out.println("Error in logging demo: " + e.getMessage());
        }

        System.out.println("\n📋 Log Levels Demonstrated:");
        System.out.println("• INFO: Operation starts, success confirmations");
        System.out.println("• FINE: SQL queries and parameters");
        System.out.println("• WARNING: Business rule validations");
        System.out.println("• SEVERE: Database and system errors");
        System.out.println("• ALL: Complete debugging information");
    }
}