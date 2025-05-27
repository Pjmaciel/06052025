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

public class TestBookDAOImpl {

    private static BookDAO bookDAO;
    private static CategoryDAO categoryDAO;
    private static Category testCategory1;
    private static Category testCategory2;

    public static void main(String[] args) {
        try {
            DB.getConnection();
            System.out.println("✅ Test connection established successfully!");

            bookDAO = new BookDAOImpl();
            categoryDAO = new CategoryDAOImpl();

            setupTestData();

            testSaveBook();
            testFindById();
            testFindAll();
            testUpdateBook();
            testRemoveBook();
            testFindByAuthor();
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


    private static void setupTestData() {
        System.out.println("\n=== SETTING UP TEST DATA ===");

        try {
            cleanupTestData();

            testCategory1 = categoryDAO.save(new Category("Programming", "Programming books for testing"));
            testCategory2 = categoryDAO.save(new Category("Fiction", "Fiction books for testing"));

            System.out.println("✅ Test categories created:");
            System.out.println("  - " + testCategory1.getName() + " (ID: " + testCategory1.getId() + ")");
            System.out.println("  - " + testCategory2.getName() + " (ID: " + testCategory2.getId() + ")");

        } catch (DbException e) {
            System.out.println("⚠️ Setup category error: " + e.getMessage());
        }
    }


    private static void testSaveBook() {
        System.out.println("\n=== TESTING SAVE BOOK ===");

        try {
            Book book1 = new Book("Clean Code", "Robert C. Martin",
                    "A handbook of agile software craftsmanship", "9780132350884", 2008, testCategory1);
            Book saved1 = bookDAO.save(book1);
            System.out.println("✅ Book saved: " + saved1.getTitle() + " (" + saved1.getCategoryName() + ")");

            Book book2 = new Book("The Clean Coder", "Robert C. Martin",
                    "A code of conduct for professional programmers", "9780137081073", 2011, testCategory1);
            Book saved2 = bookDAO.save(book2);
            System.out.println("✅ Book saved: " + saved2.getTitle() + " (" + saved2.getCategoryName() + ")");

            Book book3 = new Book("Effective Java", "Joshua Bloch",
                    "Best practices for Java", "9780134685991", 2017, testCategory1);
            Book saved3 = bookDAO.save(book3);
            System.out.println("✅ Book saved: " + saved3.getTitle() + " (" + saved3.getCategoryName() + ")");

            Book book4 = new Book("1984", "George Orwell",
                    "Dystopian social science fiction novel", "9780451524935", 1949, testCategory2);
            Book saved4 = bookDAO.save(book4);
            System.out.println("✅ Book saved: " + saved4.getTitle() + " (" + saved4.getCategoryName() + ")");

            Book book5 = new Book("Animal Farm", "George Orwell",
                    "Allegorical novella", "9780451526342", 1945, testCategory2);
            Book saved5 = bookDAO.save(book5);
            System.out.println("✅ Book saved: " + saved5.getTitle() + " (" + saved5.getCategoryName() + ")");

        } catch (DbException e) {
            System.out.println("❌ Save book error: " + e.getMessage());
        }
    }


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

            Book notFound = bookDAO.findById(999999L);
            if (notFound == null) {
                System.out.println("✅ Non-existent ID correctly returns null");
            }

        } catch (DbException e) {
            System.out.println("❌ Find by ID error: " + e.getMessage());
        }
    }


    private static void testFindAll() {
        System.out.println("\n=== TESTING FIND ALL ===");

        try {
            List<Book> books = bookDAO.findAll();
            System.out.println("✅ Found " + books.size() + " books");

            if (!books.isEmpty()) {
                System.out.println("Books list:");
                books.forEach(book ->
                        System.out.println("  - " + book.getTitle() + " by " + book.getAuthor() + " (" + book.getCategoryName() + ")")
                );

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
                    "This book will be removed", "9999999999999", 2020, testCategory1);
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


    private static void testFindByAuthor() {
        System.out.println("\n=== TESTING FIND BY AUTHOR (US-010) ===");

        testExactAuthorMatch();

        testCaseInsensitiveSearch();

        testPartialAuthorMatch();

        testNonExistentAuthor();

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
            List<Book> orwellBooks = bookDAO.findByAuthor("Orwell");

            System.out.println("✅ 'Martin' search: " + martinBooks.size() + " books");
            System.out.println("✅ 'Joshua' search: " + joshuaBooks.size() + " books");
            System.out.println("✅ 'Orwell' search: " + orwellBooks.size() + " books");

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

    // ==================== TEST FIND BY CATEGORY (US-011) - ENHANCED ====================

    private static void testFindByCategory() {
        System.out.println("\n=== TESTING FIND BY CATEGORY (US-011) ===");

        testValidCategoryWithBooks();

        testValidCategoryWithoutBooks();

        testNonExistentCategory();

        testCategoryDataInclusion();

        testCategoryResultsOrdering();

        testMultipleCategoriesComparison();
    }

    private static void testValidCategoryWithBooks() {
        System.out.println("\n--- Valid Category With Books ---");

        try {
            List<Book> programmingBooks = bookDAO.findByCategory(testCategory1.getId());
            System.out.println("✅ Found " + programmingBooks.size() + " books in category: " + testCategory1.getName());

            if (!programmingBooks.isEmpty()) {
                System.out.println("Programming books:");
                programmingBooks.forEach(book ->
                        System.out.println("  - " + book.getTitle() + " by " + book.getAuthor())
                );
            } else {
                System.out.println("❌ Expected books in Programming category but found none");
            }

        } catch (DbException e) {
            System.out.println("❌ Valid category with books error: " + e.getMessage());
        }
    }

    private static void testValidCategoryWithoutBooks() {
        System.out.println("\n--- Valid Category Without Books ---");

        try {
            Category emptyCategory = categoryDAO.save(new Category("Empty Category", "Category with no books"));

            List<Book> emptyBooks = bookDAO.findByCategory(emptyCategory.getId());

            if (emptyBooks.isEmpty()) {
                System.out.println("✅ Valid category with no books returns empty list");
            } else {
                System.out.println("❌ Empty category should return empty list, got " + emptyBooks.size() + " books");
            }

            categoryDAO.remove(emptyCategory.getId());

        } catch (DbException e) {
            System.out.println("❌ Valid category without books error: " + e.getMessage());
        }
    }

    private static void testNonExistentCategory() {
        System.out.println("\n--- Non-Existent Category ---");

        try {
            List<Book> noBooks = bookDAO.findByCategory(999999L);

            if (noBooks.isEmpty()) {
                System.out.println("✅ Non-existent category returns empty list");
            } else {
                System.out.println("❌ Non-existent category should return empty list, got " + noBooks.size() + " books");
            }

        } catch (DbException e) {
            System.out.println("❌ Non-existent category error: " + e.getMessage());
        }
    }

    private static void testCategoryDataInclusion() {
        System.out.println("\n--- Category Data Inclusion (JOIN Validation) ---");

        try {
            List<Book> booksWithCategory = bookDAO.findByCategory(testCategory1.getId());

            if (!booksWithCategory.isEmpty()) {
                Book firstBook = booksWithCategory.get(0);

                boolean hasCategory = firstBook.getCategory() != null;
                boolean hasCategoryId = hasCategory && firstBook.getCategory().getId() != null;
                boolean hasCategoryName = hasCategory && firstBook.getCategory().getName() != null;
                boolean hasCategoryDescription = hasCategory && firstBook.getCategory().getDescription() != null;

                if (hasCategory && hasCategoryId && hasCategoryName && hasCategoryDescription) {
                    System.out.println("✅ JOIN working - category data included:");
                    System.out.println("   Category ID: " + firstBook.getCategory().getId());
                    System.out.println("   Category Name: " + firstBook.getCategory().getName());
                    System.out.println("   Category Description: " + firstBook.getCategory().getDescription());
                } else {
                    System.out.println("❌ JOIN failed - category data missing:");
                    System.out.println("   Has Category: " + hasCategory);
                    System.out.println("   Has Category ID: " + hasCategoryId);
                    System.out.println("   Has Category Name: " + hasCategoryName);
                    System.out.println("   Has Category Description: " + hasCategoryDescription);
                }
            } else {
                System.out.println("⚠️ No books found to test category data inclusion");
            }

        } catch (DbException e) {
            System.out.println("❌ Category data inclusion error: " + e.getMessage());
        }
    }

    private static void testCategoryResultsOrdering() {
        System.out.println("\n--- Category Results Ordering ---");

        try {
            List<Book> programmingBooks = bookDAO.findByCategory(testCategory1.getId());

            if (programmingBooks.size() > 1) {
                boolean isOrdered = true;
                for (int i = 1; i < programmingBooks.size(); i++) {
                    if (programmingBooks.get(i-1).getTitle().compareTo(programmingBooks.get(i).getTitle()) > 0) {
                        isOrdered = false;
                        break;
                    }
                }

                if (isOrdered) {
                    System.out.println("✅ Category search results ordered by title");
                    System.out.println("Order:");
                    programmingBooks.forEach(book -> System.out.println("  " + book.getTitle()));
                } else {
                    System.out.println("❌ Category search results not properly ordered");
                    System.out.println("Actual order:");
                    programmingBooks.forEach(book -> System.out.println("  " + book.getTitle()));
                }
            } else {
                System.out.println("⚠️ Not enough books in category to test ordering");
            }

        } catch (DbException e) {
            System.out.println("❌ Category results ordering error: " + e.getMessage());
        }
    }

    private static void testMultipleCategoriesComparison() {
        System.out.println("\n--- Multiple Categories Comparison ---");

        try {
            List<Book> programmingBooks = bookDAO.findByCategory(testCategory1.getId());
            List<Book> fictionBooks = bookDAO.findByCategory(testCategory2.getId());

            System.out.println("✅ Programming books: " + programmingBooks.size());
            System.out.println("✅ Fiction books: " + fictionBooks.size());

            boolean programmingCategoryCorrect = programmingBooks.stream()
                    .allMatch(book -> testCategory1.getName().equals(book.getCategoryName()));

            boolean fictionCategoryCorrect = fictionBooks.stream()
                    .allMatch(book -> testCategory2.getName().equals(book.getCategoryName()));

            if (programmingCategoryCorrect && fictionCategoryCorrect) {
                System.out.println("✅ Books correctly filtered by category");
            } else {
                System.out.println("❌ Books not properly filtered by category");
                System.out.println("   Programming category correct: " + programmingCategoryCorrect);
                System.out.println("   Fiction category correct: " + fictionCategoryCorrect);
            }

            // Show breakdown
            if (!programmingBooks.isEmpty()) {
                System.out.println("Programming books:");
                programmingBooks.forEach(book ->
                        System.out.println("  - " + book.getTitle() + " (" + book.getCategoryName() + ")")
                );
            }

            if (!fictionBooks.isEmpty()) {
                System.out.println("Fiction books:");
                fictionBooks.forEach(book ->
                        System.out.println("  - " + book.getTitle() + " (" + book.getCategoryName() + ")")
                );
            }

        } catch (DbException e) {
            System.out.println("❌ Multiple categories comparison error: " + e.getMessage());
        }
    }

    // ==================== CLEANUP ====================

    private static void cleanupTestData() {
        try {
            String[] testIsbns = {
                    "9780132350884", "9780137081073", "9780134685991",
                    "9780451524935", "9780451526342", "9999999999999"
            };

            List<Book> allBooks = bookDAO.findAll();
            for (Book book : allBooks) {
                for (String isbn : testIsbns) {
                    if (isbn.equals(book.getIsbn()) || book.getTitle().contains("Updated")
                            || book.getTitle().contains("Remove")) {
                        try {
                            bookDAO.remove(book.getId());
                        } catch (Exception e) {
                        }
                        break;
                    }
                }
            }

            if (testCategory1 != null) {
                try {
                    categoryDAO.remove(testCategory1.getId());
                } catch (Exception e) {
                }
            }

            if (testCategory2 != null) {
                try {
                    categoryDAO.remove(testCategory2.getId());
                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
        }
    }
}