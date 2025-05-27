package br.com.libraryjdbc.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.dao.CategoryFactory;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.util.ErrorMessages;
import db.DB;
import db.DbException;

public class CategoryDAOImpl implements CategoryDAO {

    private static final Logger logger = Logger.getLogger(CategoryDAOImpl.class.getName());

    @Override
    public Category save(Category category) {
        logger.info("DAO: Saving category: " + (category != null ? category.getName() : "null"));

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;

        try {
            conn = DB.getConnection();
            originalAutoCommit = conn.getAutoCommit();

            // Start transaction
            conn.setAutoCommit(false);
            transactionStarted = true;

            String sql = "INSERT INTO category (name, description) VALUES (?, ?)";
            st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, category.getName());
            st.setString(2, category.getDescription());

            logger.fine("Executing SQL: " + sql + " with parameters: [" + category.getName() + ", " + category.getDescription() + "]");

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    long id = rs.getLong(1);
                    category.setId(id);

                    conn.commit(); // Commit transaction

                    String successMsg = String.format(ErrorMessages.CATEGORY_INSERTED_SUCCESS, id);
                    logger.info(successMsg);
                    System.out.println("✅ " + successMsg);
                } else {
                    conn.rollback();
                    String errorMsg = String.format(ErrorMessages.NO_ROWS_AFFECTED, "category insert");
                    logger.severe(errorMsg);
                    throw new DbException(errorMsg);
                }
            } else {
                conn.rollback();
                String errorMsg = String.format(ErrorMessages.NO_ROWS_AFFECTED, "category insert");
                logger.severe(errorMsg);
                throw new DbException(errorMsg);
            }

            return category;

        } catch (SQLException e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.DB_INSERT_ERROR, "category", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
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
    public void update(Category category) {
        logger.info("DAO: Updating category ID: " + (category != null ? category.getId() : "null"));

        Connection conn = null;
        PreparedStatement st = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;

        try {
            conn = DB.getConnection();
            originalAutoCommit = conn.getAutoCommit();

            // Start transaction
            conn.setAutoCommit(false);
            transactionStarted = true;

            String sql = "UPDATE category SET name = ?, description = ? WHERE id = ?";
            st = conn.prepareStatement(sql);

            st.setString(1, category.getName());
            st.setString(2, category.getDescription());
            st.setLong(3, category.getId());

            logger.fine("Executing SQL: " + sql + " with parameters: [" + category.getName() + ", " + category.getDescription() + ", " + category.getId() + "]");

            int rowsAffected = st.executeUpdate();

            if (rowsAffected == 0) {
                conn.rollback();
                String errorMsg = String.format(ErrorMessages.CATEGORY_NOT_FOUND, category.getId());
                logger.warning(errorMsg);
                throw new DbException(errorMsg);
            }

            conn.commit(); // Commit transaction
            logger.info("DAO: Category updated successfully. ID: " + category.getId());

        } catch (SQLException e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.DB_UPDATE_ERROR, "category", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
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
        logger.info("DAO: Removing category ID: " + id);

        Connection conn = null;
        PreparedStatement st = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;

        try {
            conn = DB.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            transactionStarted = true;

            String sql = "DELETE FROM category WHERE id = ?";
            st = conn.prepareStatement(sql);
            st.setLong(1, id);

            logger.fine("Executing SQL: " + sql + " with parameter: " + id);

            int rowsAffected = st.executeUpdate();

            if (rowsAffected == 0) {
                conn.rollback();
                String errorMsg = String.format(ErrorMessages.CATEGORY_NOT_FOUND, id);
                logger.warning(errorMsg);
                throw new DbException(errorMsg);
            }

            conn.commit();
            logger.info("DAO: Category removed successfully. ID: " + id);

        } catch (SQLException e) {
            handleTransactionRollback(conn, transactionStarted);
            String errorMsg = String.format(ErrorMessages.DB_DELETE_ERROR, "category", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
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
    public Category findById(Long id) {
        logger.fine("DAO: Finding category by ID: " + id);
        String sql = "SELECT * FROM category WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setLong(1, id);
            logger.fine("Executing SQL: " + sql + " with parameter: [ID redacted]");

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Category category = CategoryFactory.fromResultSet(rs);
                    logger.fine("DAO: Category found: " + category.getName());
                    return category;
                }
            }

            logger.fine("DAO: Category not found for ID: " + id);
            return null;
        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_SELECT_ERROR, "category by ID", e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            throw new DbException(errorMsg);
        }
    }

    @Override
    public List<Category> findAll() {
        logger.fine("DAO: Finding all categories");

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT * FROM category ORDER BY name";
            st = conn.prepareStatement(sql);

            logger.fine("Executing SQL: " + sql);

            rs = st.executeQuery();

            List<Category> categories = new ArrayList<>();

            while (rs.next()) {
                categories.add(CategoryFactory.fromResultSet(rs));
            }

            logger.info("DAO: Found " + categories.size() + " categories");
            return categories;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_LIST_ERROR, "categories", e.getMessage());
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
    public Category findCategoryWithMostBooks() {
        logger.fine("DAO: Finding category with most books");

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT c.*, COUNT(b.id) as book_count " +
                    "FROM category c " +
                    "LEFT JOIN book b ON c.id = b.category_id " +
                    "GROUP BY c.id, c.name, c.description " +
                    "ORDER BY book_count DESC " +
                    "LIMIT 1";

            st = conn.prepareStatement(sql);

            logger.fine("Executing SQL: " + sql);

            rs = st.executeQuery();

            if (rs.next()) {
                Category category = CategoryFactory.fromResultSet(rs);
                int bookCount = rs.getInt("book_count");
                logger.info("DAO: Category with most books: " + category.getName() + " (" + bookCount + " books)");
                return category;
            }

            logger.info("DAO: No categories found");
            return null;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_SELECT_ERROR, "category with most books", e.getMessage());
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

    public Map<String, Integer> getCategoryBookCounts() {
        logger.fine("DAO: Getting book count for each category");

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();

            String sql = "SELECT c.name, COUNT(b.id) as book_count " +
                    "FROM category c " +
                    "INNER JOIN book b ON c.id = b.category_id " +
                    "GROUP BY c.name " +
                    "ORDER BY book_count DESC";

            st = conn.prepareStatement(sql);

            logger.fine("Executing SQL: " + sql);

            rs = st.executeQuery();

            Map<String, Integer> categoryBookCounts = new HashMap<>();

            while (rs.next()) {
                String categoryName = rs.getString("name");
                int bookCount = rs.getInt("book_count");
                categoryBookCounts.put(categoryName, bookCount);
            }

            logger.info("DAO: Found book counts for " + categoryBookCounts.size() + " categories");
            return categoryBookCounts;

        } catch (SQLException e) {
            String errorMsg = String.format(ErrorMessages.DB_SELECT_ERROR, "category book counts", e.getMessage());
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

    // Helper Methods - Only for transaction management

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