package br.com.libraryjdbc.service.impl;


import java.util.List;
import java.util.logging.Logger;

import br.com.libraryjdbc.model.dao.BookDAO;
import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.impl.BookDAOImpl;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import br.com.libraryjdbc.service.BookService;


public class BookServiceImpl implements BookService {

    private static final Logger logger = Logger.getLogger(BookServiceImpl.class.getName());

    private final BookDAO bookDAO;
    private final CategoryDAO categoryDAO;


    public BookServiceImpl() {
        this.bookDAO = new BookDAOImpl();
        this.categoryDAO = new CategoryDAOImpl();
    }

    public BookServiceImpl(BookDAO bookDAO, CategoryDAO categoryDAO) {
        this.bookDAO = bookDAO;
        this.categoryDAO = categoryDAO;
    }

    @Override
    public Book save(Book book) {
        logger.info("BookService: Attempting to save book: " +
                (book != null ? book.getTitle() : "null"));

        // Business Rule Validations
        validateBookForSave(book);

        if (isbnExists(book.getIsbn())) {
            String errorMsg = "ISBN already exists in the system: " + book.getIsbn();
            logger.warning("BookService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (!categoryExists(book.getCategory().getId())) {
            String errorMsg = "Category with ID " + book.getCategory().getId() + " does not exist";
            logger.warning("BookService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Delegate to DAO for persistence
        Book savedBook = bookDAO.save(book);

        logger.info("BookService: Book saved successfully with ID: " + savedBook.getId());
        return savedBook;
    }

    @Override
    public void update(Book book) {
        logger.info("BookService: Attempting to update book ID: " +
                (book != null ? book.getId() : "null"));

        validateBookForUpdate(book);

        Book existingBook = bookDAO.findById(book.getId());
        if (existingBook == null) {
            String errorMsg = "Book with ID " + book.getId() + " not found";
            logger.warning("BookService: " + errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (!existingBook.getIsbn().equals(book.getIsbn()) && isbnExists(book.getIsbn())) {
            String errorMsg = "ISBN already exists in the system: " + book.getIsbn();
            logger.warning("BookService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (!categoryExists(book.getCategory().getId())) {
            String errorMsg = "Category with ID " + book.getCategory().getId() + " does not exist";
            logger.warning("BookService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        bookDAO.update(book);

        logger.info("BookService: Book updated successfully. ID: " + book.getId());
    }

    @Override
    public void remove(Long id) {
        logger.info("BookService: Attempting to remove book ID: " + id);

        if (id == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }

        Book existingBook = bookDAO.findById(id);
        if (existingBook == null) {
            String errorMsg = "Book with ID " + id + " not found";
            logger.warning("BookService: " + errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        bookDAO.remove(id);

        logger.info("BookService: Book removed successfully. ID: " + id);
    }

    @Override
    public Book findById(Long id) {
        logger.fine("BookService: Finding book by ID: " + id);

        if (id == null) {
            logger.warning("BookService: ID cannot be null for findById");
            return null;
        }

        return bookDAO.findById(id);
    }

    @Override
    public List<Book> findAll() {
        logger.fine("BookService: Finding all books");
        return bookDAO.findAll();
    }

    @Override
    public List<Book> findByAuthor(String author) {
        logger.fine("BookService: Finding books by author: " + author);

        if (author == null || author.trim().isEmpty()) {
            logger.warning("BookService: Author cannot be null or empty for search");
            throw new IllegalArgumentException("Author name cannot be empty");
        }

        return bookDAO.findByAuthor(author.trim());
    }

    @Override
    public List<Book> findByCategory(Long categoryId) {
        logger.fine("BookService: Finding books by category ID: " + categoryId);

        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        // Business Rule: Category must exist
        if (!categoryExists(categoryId)) {
            String errorMsg = "Category with ID " + categoryId + " does not exist";
            logger.warning("BookService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        return bookDAO.findByCategory(categoryId);
    }

    @Override
    public boolean isbnExists(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        // Check by getting all books and comparing ISBNs
        List<Book> allBooks = bookDAO.findAll();
        return allBooks.stream()
                .anyMatch(book -> book.getIsbn().equals(isbn.trim()));
    }

    @Override
    public boolean categoryExists(Long categoryId) {
        if (categoryId == null) {
            return false;
        }

        return categoryDAO.findById(categoryId) != null;
    }

    @Override
    public boolean isValidReleaseYear(Integer year) {
        return year != null && year >= 1967;
    }

    // Private validation methods

    private void validateBookForSave(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            logger.warning("BookService: Book title validation failed: empty or null");
            throw new IllegalArgumentException("Book title cannot be empty");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            logger.warning("BookService: Book author validation failed: empty or null");
            throw new IllegalArgumentException("Book author cannot be empty");
        }

        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            logger.warning("BookService: Book ISBN validation failed: empty or null");
            throw new IllegalArgumentException("Book ISBN cannot be empty");
        }

        if (book.getReleaseYear() == null) {
            logger.warning("BookService: Book release year validation failed: null");
            throw new IllegalArgumentException("Book release year cannot be null");
        }

        if (!isValidReleaseYear(book.getReleaseYear())) {
            String errorMsg = "Book release year must be >= 1967, received: " + book.getReleaseYear();
            logger.warning("BookService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        if (book.getCategory() == null || book.getCategory().getId() == null) {
            logger.warning("BookService: Book category validation failed: null category or category ID");
            throw new IllegalArgumentException("Book must have a valid category");
        }
    }

    private void validateBookForUpdate(Book book) {
        // Include save validations
        validateBookForSave(book);

        if (book.getId() == null) {
            logger.warning("BookService: Book ID validation failed: null ID for update");
            throw new IllegalArgumentException("Book ID cannot be null for update");
        }
    }
}