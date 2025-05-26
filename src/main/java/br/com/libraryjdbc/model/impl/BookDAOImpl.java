package br.com.libraryjdbc.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.libraryjdbc.model.dao.BookDAO;
import br.com.libraryjdbc.model.dao.BookFactory;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.util.ErrorMessages;
import db.DB;
import db.DbException;

/**
 * JDBC implementation of BookDAO interface with enhanced exception handling.
 * US-009: Tratamento de Exceções - Enhanced error handling and logging
 */
public class BookDAOImpl implements BookDAO {

    private static final Logger logger = Logger.getLogger(BookDAOImpl.class.getName());

    @Override
    public Book save(Book book) {
        logger.info("Attempting to save book: " + (book != null ? book.getTitle() : "null"));

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;

        try {
            conn = DB.getConnection();
            originalAutoCommit = conn.getAutoCommit();

            // Validations BEFORE starting transaction (to avoid rollback issues)
            validateBookForSave(book);

            // Start transaction AFTER validations pass
            conn.setAutoCommit(false);
            transactionStarted = true;

            if (isbnExists(book.getIsbn())) {
                String errorMsg = String.format(ErrorMessages.BOOK_ISBN_EXISTS, book.getIsbn());
                logger.warning("Book ISBN validation failed: " + errorMsg);
                throw new DbException(errorMsg);
            }

            if (!categoryExists(book.getCategory().getId())) {
                String errorMsg = String.format(ErrorMessages.BOOK_CATEGORY_NOT_EXISTS, book.getCategory().getId());
                logger.warning("Book category validation failed: " + errorMsg);
                throw new DbException(errorMsg);
            }

            String sql = "INSERT INTO book (title, author, synopsis, isbn, release_year, category_id) VALUES (?, ?, ?, ?, ?, ?)";
            st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, book.getTitle());
            st.setString(2, book.getAuthor());
            st.setString(3, book.getSynopsis());
            st.setString(4, book.getIsbn());
            st.setInt(5, book.getReleaseYear());
            st.setLong(6, book.getCategory().getId());

            logger.fine("Executing SQL: " + sql + " with parameters: [" + book.getTitle() + ", " + book.getAuthor() + ", " + book.getIsbn() + ", " + book.getReleaseYear() + ", " + book.getCategory().getId() + "]");

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    long id = rs.getLong(1);
                    book.setId(id);

                    conn.commit(); // Commit transaction

                    String successMsg = String.format(ErrorMessages.BOOK_INSERTED_SUCCESS, id);
                    logger.info(successMsg);
                    System.out.println("✅ " + successMsg);
                } else {
                    conn.rollback();
                    String errorMsg = String.format(ErrorMessages.NO_ROWS_AFFECTED, "book insert");
                    logger.severe(errorMsg);
                    throw new DbException(errorMsg);
                }
            } else {
                conn.rollback();
                String errorMsg = String.format(ErrorMessages.NO_ROWS_AFFECTED, "book insert");
                logger.severe(errorMsg);
                throw new DbException(errorMsg);
            }

            return book;

        } catch (SQLException e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.DB_INSERT_ERROR, "book", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (DbException e) {
            handleTransactionRollback(conn, transactionStarted);
            logger.log(Level.WARNING, "Business rule validation failed: " + e.getMessage(), e);
            throw e; // Re-throw DbException as-is
        } catch (Exception e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            restoreAutoCommit(conn, originalAutoCommit);
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void update(Book book) {
        logger.info("Attempting to update book ID: " + (book != null ? book.getId() : "null"));

        Connection conn = null;
        PreparedStatement st = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;

        try {
            conn = DB.getConnection();
            originalAutoCommit = conn.getAutoCommit();

            // Validations BEFORE starting transaction
            validateBookForUpdate(book);

            // Start transaction AFTER validations pass
            conn.setAutoCommit(false);
            transactionStarted = true;

            if (!categoryExists(book.getCategory().getId())) {
                String errorMsg = String.format(ErrorMessages.BOOK_CATEGORY_NOT_EXISTS, book.getCategory().getId());
                logger.warning("Book category validation failed: " + errorMsg);
                throw new DbException(errorMsg);
            }

            String sql = "UPDATE book SET title = ?, author = ?, synopsis = ?, isbn = ?, release_year = ?, category_id = ? WHERE id = ?";
            st = conn.prepareStatement(sql);

            st.setString(1, book.getTitle());
            st.setString(2, book.getAuthor());
            st.setString(3, book.getSynopsis());
            st.setString(4, book.getIsbn());
            st.setInt(5, book.getReleaseYear());
            st.setLong(6, book.getCategory().getId());
            st.setLong(7, book.getId());

            logger.fine("Executing SQL: " + sql + " with parameters: [" + book.getTitle() + ", " + book.getAuthor() + ", " + book.getIsbn() + ", " + book.getReleaseYear() + ", " + book.getCategory().getId() + ", " + book.getId() + "]");

            int rowsAffected = st.executeUpdate();

            if (rowsAffected == 0) {
                conn.rollback();
                String errorMsg = String.format(ErrorMessages.BOOK_NOT_FOUND, book.getId());
                logger.warning(errorMsg);
                throw new DbException(errorMsg);
            }

            conn.commit(); // Commit transaction
            logger.info("Book updated successfully. ID: " + book.getId());

        } catch (SQLException e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.DB_UPDATE_ERROR, "book", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (DbException e) {
            handleTransactionRollback(conn, transactionStarted);
            logger.log(Level.WARNING, "Business rule validation failed: " + e.getMessage(), e);
            throw e; // Re-throw DbException as-is
        } catch (Exception e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            restoreAutoCommit(conn, originalAutoCommit);
            DB.closeStatement(st);
        }
    }

    @Override
    public void remove(Long id) {
        logger.info("Attempting to remove book ID: " + id);

        Connection conn = null;
        PreparedStatement st = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;

        try {
            conn = DB.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            transactionStarted = true;

            String sql = "DELETE FROM book WHERE id = ?";
            st = conn.prepareStatement(sql);
            st.setLong(1, id);

            logger.fine("Executing SQL: " + sql + " with parameter: " + id);

            int rowsAffected = st.executeUpdate();

            if (rowsAffected == 0) {
                conn.rollback();
                String errorMsg = String.format(ErrorMessages.BOOK_NOT_FOUND, id);
                logger.warning(errorMsg);
                throw new DbException(errorMsg);
            }

            conn.commit(); // Commit transaction
            logger.info("Book removed successfully. ID: " + id);

        } catch (SQLException e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.DB_DELETE_ERROR, "book", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (DbException e) {
            handleTransactionRollback(conn, transactionStarted);
            logger.log(Level.WARNING, "Business rule validation failed: " + e.getMessage(), e);
            throw e; // Re-throw DbException as-is
        } catch (Exception e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            restoreAutoCommit(conn, originalAutoCommit);
            DB.closeStatement(st);
        }
    }

    @Override
    public Book findById(Long id) {
        logger.fine("Finding book by ID: " + id);

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT b.*, c.name as category_name, c.description as category_description " +
                    "FROM book b " +
                    "INNER JOIN category c ON b.category_id = c.id " +
                    "WHERE b.id = ?";

            st = conn.prepareStatement(sql);
            st.setLong(1, id);

            logger.fine("Executing SQL: " + sql + " with parameter: " + id);

            rs = st.executeQuery();

            if (rs.next()) {
                Book book = BookFactory.fromResultSetWithCategory(rs);
                logger.fine("Book found: " + book.getTitle());
                return book;
            }

            logger.fine("Book not found for ID: " + id);
            return null;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_SELECT_ERROR, "book by ID", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (Exception e) {
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<Book> findAll() {
        logger.fine("Finding all books");

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT b.*, c.name as category_name, c.description as category_description " +
                    "FROM book b " +
                    "INNER JOIN category c ON b.category_id = c.id " +
                    "ORDER BY b.title";

            st = conn.prepareStatement(sql);

            logger.fine("Executing SQL: " + sql);

            rs = st.executeQuery();

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                books.add(BookFactory.fromResultSetWithCategory(rs));
            }

            logger.info("Found " + books.size() + " books");
            return books;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_LIST_ERROR, "books", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (Exception e) {
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<Book> findByAuthor(String author) {
        logger.fine("Finding books by author: " + author);

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT b.*, c.name as category_name, c.description as category_description " +
                    "FROM book b " +
                    "INNER JOIN category c ON b.category_id = c.id " +
                    "WHERE LOWER(b.author) LIKE LOWER(?) " +
                    "ORDER BY b.title";

            st = conn.prepareStatement(sql);
            st.setString(1, "%" + author + "%");

            logger.fine("Executing SQL: " + sql + " with parameter: %" + author + "%");

            rs = st.executeQuery();

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                books.add(BookFactory.fromResultSetWithCategory(rs));
            }

            logger.info("Found " + books.size() + " books by author: " + author);
            return books;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_SELECT_ERROR, "books by author", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (Exception e) {
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<Book> findByCategory(Long categoryId) {
        logger.fine("Finding books by category ID: " + categoryId);

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT b.*, c.name as category_name, c.description as category_description " +
                    "FROM book b " +
                    "INNER JOIN category c ON b.category_id = c.id " +
                    "WHERE b.category_id = ? " +
                    "ORDER BY b.title";

            st = conn.prepareStatement(sql);
            st.setLong(1, categoryId);

            logger.fine("Executing SQL: " + sql + " with parameter: " + categoryId);

            rs = st.executeQuery();

            List<Book> books = new ArrayList<>();

            while (rs.next()) {
                books.add(BookFactory.fromResultSetWithCategory(rs));
            }

            logger.info("Found " + books.size() + " books for category ID: " + categoryId);
            return books;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_SELECT_ERROR, "books by category", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } catch (Exception e) {
            String errorMsg = String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    // Helper Methods with Enhanced Error Handling

    private void validateBookForSave(Book book) {
        if (book == null) {
            throw new DbException("Book cannot be null");
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            logger.warning("Book title validation failed: empty or null");
            throw new DbException(ErrorMessages.BOOK_TITLE_EMPTY);
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            logger.warning("Book author validation failed: empty or null");
            throw new DbException(ErrorMessages.BOOK_AUTHOR_EMPTY);
        }

        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            logger.warning("Book ISBN validation failed: empty or null");
            throw new DbException(ErrorMessages.BOOK_ISBN_EMPTY);
        }

        if (book.getReleaseYear() == null) {
            logger.warning("Book release year validation failed: null");
            throw new DbException(ErrorMessages.BOOK_YEAR_NULL);
        }

        if (book.getReleaseYear() < 1967) {
            String errorMsg = String.format(ErrorMessages.BOOK_YEAR_INVALID, book.getReleaseYear());
            logger.warning("Book release year validation failed: " + errorMsg);
            throw new DbException(errorMsg);
        }

        if (book.getCategory() == null || book.getCategory().getId() == null) {
            logger.warning("Book category validation failed: null category or category ID");
            throw new DbException(ErrorMessages.BOOK_CATEGORY_INVALID);
        }
    }

    private void validateBookForUpdate(Book book) {
        validateBookForSave(book); // Include save validations

        if (book.getId() == null) {
            logger.warning("Book ID validation failed: null ID for update");
            throw new DbException(ErrorMessages.BOOK_ID_NULL_UPDATE);
        }
    }

    private boolean isbnExists(String isbn) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            String sql = "SELECT COUNT(*) FROM book WHERE isbn = ?";
            st = conn.prepareStatement(sql);
            st.setString(1, isbn);

            rs = st.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_VALIDATION_ERROR, "ISBN existence", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    private boolean categoryExists(Long categoryId) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            String sql = "SELECT COUNT(*) FROM category WHERE id = ?";
            st = conn.prepareStatement(sql);
            st.setLong(1, categoryId);

            rs = st.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_VALIDATION_ERROR, "category existence", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    private void handleTransactionRollback(Connection conn, boolean transactionStarted) {
        if (conn != null && transactionStarted) {
            try {
                conn.rollback();
                logger.info(ErrorMessages.TRANSACTION_ROLLBACK);
            } catch (SQLException rollbackEx) {
                String errorMsg = String.format(ErrorMessages.TRANSACTION_ROLLBACK_ERROR, rollbackEx.getMessage());
                logger.log(Level.SEVERE, errorMsg, rollbackEx);
            }
        } else if (conn != null) {
            logger.fine("Skipping rollback - transaction was not started");
        }
    }

    private void restoreAutoCommit(Connection conn, boolean originalAutoCommit) {
        if (conn != null) {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to restore auto-commit: " + e.getMessage(), e);
            }
        }
    }
}