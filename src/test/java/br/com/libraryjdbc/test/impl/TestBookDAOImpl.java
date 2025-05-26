package br.com.libraryjdbc.test.impl;

import java.util.List;

import br.com.libraryjdbc.model.dao.BookDAO;
import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.model.impl.BookDAOImpl;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import db.DB;
import db.DbException;

/**
 * Test class for BookDAOImpl - US-010: Buscar Livros por Autor
 * Tests all BookDAO methods with focus on findByAuthor functionality
 */
public class TestBookDAOImpl {

    private static BookDAO bookDAO;
    private static CategoryDAO categoryDAO;
    private static Category testCategory;

    public static void main(String[] args) {
        try {
            DB.getConnection();
            System.out.println("✅ Test connection established successfully!");

            bookDAO = new BookDAOImpl();
            categoryDAO = new CategoryDAOImpl();

            // Setup test data
            setupTestData();

            // Test all BookDAO methods
            testSaveBook();
            testFindById();
            testFindAll();
            testUpdateBook();
            testRemoveBook();
            testFindByAuthor(); // US-010 focus
            testFindByCategory();

            System.out.println("\n🎉 All BookDAOImpl tests completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in BookDAOImpl tests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanupTestData();
            DB.closeConnection();
            System.out.println("✅ Test connection closed and cleanup completed!");
        }
    }

    // ==================== SETUP ====================

    private static void setupTestData() {
        System.out.println("\n=== SETTING UP TEST DATA ===");

        try {
            cleanupTestData();

            testCategory = categoryDAO.save(new Category("Programming", "Programming books for testing"));
            System.out.println("✅ Test category created: " + testCategory.getName());

        } catch (DbException e) {
            System.out.println("⚠️ Setup category error: " + e.getMessage());
        }
    }

    // ==================== TEST SAVE ====================

    private static void testSaveBook() {
        System.out.println("\n=== TESTING SAVE BOOK ===");

        try {
            Book book1 = new Book("Clean Code", "Robert C. Martin",
                    "A handbook of agile software craftsmanship", "9780132350884", 2008, testCategory);
            Book saved1 = bookDAO.save(book1);
            System.out.println("✅ Book saved: " + saved1.getTitle());

            Book book2 = new Book("The Clean Coder", "Robert C. Martin",
                    "A code of conduct for professional programmers", "9780137081073", 2011, testCategory);
            Book saved2 = bookDAO.save(book2);
            System.out.println("✅ Book saved: " + saved2.getTitle());

            Book book3 = new Book("Effective Java", "Joshua Bloch",
                    "Best practices for Java", "9780134685991", 2017, testCategory);
            Book saved3 = bookDAO.save(book3);
            System.out.println("✅ Book saved: " + saved3.getTitle());

        } catch (DbException e) {
            System.out.println("❌ Save book error: " + e.getMessage());
        }
    }

    // ==================== TEST FIND BY ID ====================

    private static void testFindById() {
        System.out.println("\n=== TESTING FIND BY ID ===");

        try {
            List<Book> allBooks = bookDAO.findAll();
            if (!allBooks.isEmpty()) {
                Book firstBook = allBooks.get(0);
                Book found = bookDAO.findById(firstBook.getId());

                if (found != null) {
                    System.out.println("✅ Book found by ID: " + found.getTitle());
                    System.out.println("   Category: " + found.getCategoryName());
                } else {
                    System.out.println("❌ Book not found by ID");
                }
            }

            // Test non-existent ID
            Book notFound = bookDAO.findById(999999L);
            if (notFound == null) {
                System.out.println("✅ Non-existent ID correctly returns null");
            }

        } catch (DbException e) {
            System.out.println("❌ Find by ID error: " + e.getMessage());
        }
    }

    // ==================== TEST FIND ALL ====================

    private static void testFindAll() {
        System.out.println("\n=== TESTING FIND ALL ===");

        try {
            List<Book> books = bookDAO.findAll();
            System.out.println("✅ Found " + books.size() + " books");

            if (!books.isEmpty()) {
                System.out.println("Books list:");
                books.forEach(book ->
                        System.out.println("  - " + book.getTitle() + " by " + book.getAuthor())
                );

                // Test ordering by title
                boolean isOrdered = true;
                for (int i = 1; i < books.size(); i++) {
                    if (books.get(i-1).getTitle().compareTo(books.get(i).getTitle()) > 0) {
                        isOrdered = false;
                        break;
                    }
                }

                if (isOrdered) {
                    System.out.println("✅ Books are correctly ordered by title");
                } else {
                    System.out.println("❌ Books are not properly ordered");
                }
            }

        } catch (DbException e) {
            System.out.println("❌ Find all error: " + e.getMessage());
        }
    }

    // ==================== TEST UPDATE ====================

    private static void testUpdateBook() {
        System.out.println("\n=== TESTING UPDATE BOOK ===");

        try {
            List<Book> books = bookDAO.findAll();
            if (!books.isEmpty()) {
                Book bookToUpdate = books.get(0);
                String originalTitle = bookToUpdate.getTitle();

                bookToUpdate.setTitle(originalTitle + " - Updated");
                bookDAO.update(bookToUpdate);

                Book updatedBook = bookDAO.findById(bookToUpdate.getId());
                if (updatedBook != null && updatedBook.getTitle().contains("Updated")) {
                    System.out.println("✅ Book updated successfully: " + updatedBook.getTitle());
                } else {
                    System.out.println("❌ Book update failed");
                }
            }

        } catch (DbException e) {
            System.out.println("❌ Update book error: " + e.getMessage());
        }
    }

    // ==================== TEST REMOVE ====================

    private static void testRemoveBook() {
        System.out.println("\n=== TESTING REMOVE BOOK ===");

        try {
            // Create a book specifically for removal test
            Book bookForRemoval = new Book("Book to Remove", "Test Author",
                    "This book will be removed", "9999999999999", 2020, testCategory);
            Book savedBook = bookDAO.save(bookForRemoval);

            Long bookId = savedBook.getId();
            bookDAO.remove(bookId);

            Book removedBook = bookDAO.findById(bookId);
            if (removedBook == null) {
                System.out.println("✅ Book removed successfully");
            } else {
                System.out.println("❌ Book removal failed");
            }

        } catch (DbException e) {
            System.out.println("❌ Remove book error: " + e.getMessage());
        }
    }

    // ==================== TEST FIND BY AUTHOR (US-010) ====================

    private static void testFindByAuthor() {
        System.out.println("\n=== TESTING FIND BY AUTHOR (US-010) ===");

        // Test exact match
        testExactAuthorMatch();

        // Test case-insensitive search
        testCaseInsensitiveSearch();

        // Test partial match
        testPartialAuthorMatch();

        // Test non-existent author
        testNonExistentAuthor();

        // Test title ordering
        testAuthorResultsOrdering();
    }

    private static void testExactAuthorMatch() {
        System.out.println("\n--- Exact Author Match ---");

        try {
            List<Book> martinBooks = bookDAO.findByAuthor("Robert C. Martin");
            System.out.println("✅ Found " + martinBooks.size() + " books by 'Robert C. Martin'");

            if (!martinBooks.isEmpty()) {
                martinBooks.forEach(book ->
                        System.out.println("  - " + book.getTitle())
                );
            }

        } catch (DbException e) {
            System.out.println("❌ Exact author match error: " + e.getMessage());
        }
    }

    private static void testCaseInsensitiveSearch() {
        System.out.println("\n--- Case-Insensitive Search ---");

        try {
            List<Book> lowerCase = bookDAO.findByAuthor("robert c. martin");
            List<Book> upperCase = bookDAO.findByAuthor("ROBERT C. MARTIN");
            List<Book> mixedCase = bookDAO.findByAuthor("RoBerT c. MaRtIn");

            System.out.println("✅ Lowercase search: " + lowerCase.size() + " books");
            System.out.println("✅ Uppercase search: " + upperCase.size() + " books");
            System.out.println("✅ Mixed case search: " + mixedCase.size() + " books");

            if (lowerCase.size() == upperCase.size() && upperCase.size() == mixedCase.size()) {
                System.out.println("✅ Case-insensitive search working correctly");
            } else {
                System.out.println("❌ Case-insensitive search inconsistent");
            }

        } catch (DbException e) {
            System.out.println("❌ Case-insensitive search error: " + e.getMessage());
        }
    }

    private static void testPartialAuthorMatch() {
        System.out.println("\n--- Partial Author Match ---");

        try {
            List<Book> martinBooks = bookDAO.findByAuthor("Martin");
            List<Book> joshuaBooks = bookDAO.findByAuthor("Joshua");

            System.out.println("✅ 'Martin' search: " + martinBooks.size() + " books");
            System.out.println("✅ 'Joshua' search: " + joshuaBooks.size() + " books");

        } catch (DbException e) {
            System.out.println("❌ Partial author match error: " + e.getMessage());
        }
    }

    private static void testNonExistentAuthor() {
        System.out.println("\n--- Non-Existent Author ---");

        try {
            List<Book> noBooks = bookDAO.findByAuthor("Stephen King");

            if (noBooks.isEmpty()) {
                System.out.println("✅ Non-existent author returns empty list");
            } else {
                System.out.println("❌ Non-existent author should return empty list, got " + noBooks.size());
            }

        } catch (DbException e) {
            System.out.println("❌ Non-existent author error: " + e.getMessage());
        }
    }

    private static void testAuthorResultsOrdering() {
        System.out.println("\n--- Author Results Ordering ---");

        try {
            List<Book> martinBooks = bookDAO.findByAuthor("Robert C. Martin");

            if (martinBooks.size() > 1) {
                boolean isOrdered = true;
                for (int i = 1; i < martinBooks.size(); i++) {
                    if (martinBooks.get(i-1).getTitle().compareTo(martinBooks.get(i).getTitle()) > 0) {
                        isOrdered = false;
                        break;
                    }
                }

                if (isOrdered) {
                    System.out.println("✅ Author search results ordered by title");
                    System.out.println("Order:");
                    martinBooks.forEach(book -> System.out.println("  " + book.getTitle()));
                } else {
                    System.out.println("❌ Author search results not properly ordered");
                }
            } else {
                System.out.println("⚠️ Not enough books to test ordering");
            }

        } catch (DbException e) {
            System.out.println("❌ Author results ordering error: " + e.getMessage());
        }
    }

    // ==================== TEST FIND BY CATEGORY ====================

    private static void testFindByCategory() {
        System.out.println("\n=== TESTING FIND BY CATEGORY ===");

        try {
            List<Book> categoryBooks = bookDAO.findByCategory(testCategory.getId());
            System.out.println("✅ Found " + categoryBooks.size() + " books in category: " + testCategory.getName());

            if (!categoryBooks.isEmpty()) {
                categoryBooks.forEach(book ->
                        System.out.println("  - " + book.getTitle() + " (" + book.getCategoryName() + ")")
                );
            }

        } catch (DbException e) {
            System.out.println("❌ Find by category error: " + e.getMessage());
        }
    }

    // ==================== CLEANUP ====================

    private static void cleanupTestData() {
        try {
            // Clean test books
            String[] testIsbns = {
                    "9780132350884", "9780137081073", "9780134685991", "9999999999999"
            };

            List<Book> allBooks = bookDAO.findAll();
            for (Book book : allBooks) {
                for (String isbn : testIsbns) {
                    if (isbn.equals(book.getIsbn()) || book.getTitle().contains("Updated")
                            || book.getTitle().contains("Remove")) {
                        try {
                            bookDAO.remove(book.getId());
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                        break;
                    }
                }
            }

            // Clean test category
            if (testCategory != null) {
                try {
                    categoryDAO.remove(testCategory.getId());
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }

        } catch (Exception e) {
            // Ignore all cleanup errors
        }
    }
}