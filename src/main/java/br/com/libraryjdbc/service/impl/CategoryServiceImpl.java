package br.com.libraryjdbc.service.impl;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import br.com.libraryjdbc.service.CategoryService;


public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = Logger.getLogger(CategoryServiceImpl.class.getName());

    private final CategoryDAO categoryDAO;


    public CategoryServiceImpl() {
        this.categoryDAO = new CategoryDAOImpl();
    }

    public CategoryServiceImpl(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    public Category save(Category category) {
        logger.info("CategoryService: Attempting to save category: " +
                (category != null ? category.getName() : "null"));

        // Business Rule Validations
        validateCategoryForSave(category);

        if (categoryNameExists(category.getName())) {
            String errorMsg = "Category name already exists: " + category.getName();
            logger.warning("CategoryService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // Delegate to DAO for persistence
        Category savedCategory = categoryDAO.save(category);

        logger.info("CategoryService: Category saved successfully with ID: " + savedCategory.getId());
        return savedCategory;
    }

    @Override
    public void update(Category category) {
        logger.info("CategoryService: Attempting to update category ID: " +
                (category != null ? category.getId() : "null"));

        validateCategoryForUpdate(category);

        Category existingCategory = categoryDAO.findById(category.getId());
        if (existingCategory == null) {
            String errorMsg = "Category with ID " + category.getId() + " not found";
            logger.warning("CategoryService: " + errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (!existingCategory.getName().equalsIgnoreCase(category.getName()) &&
                categoryNameExists(category.getName())) {
            String errorMsg = "Category name already exists: " + category.getName();
            logger.warning("CategoryService: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        categoryDAO.update(category);

        logger.info("CategoryService: Category updated successfully. ID: " + category.getId());
    }

    @Override
    public void remove(Long id) {
        logger.info("CategoryService: Attempting to remove category ID: " + id);

        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        Category existingCategory = categoryDAO.findById(id);
        if (existingCategory == null) {
            String errorMsg = "Category with ID " + id + " not found";
            logger.warning("CategoryService: " + errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        if (!canRemoveCategory(id)) {
            String errorMsg = "Cannot remove category that has associated books";
            logger.warning("CategoryService: " + errorMsg + " (ID: " + id + ")");
            throw new IllegalArgumentException(errorMsg);
        }

        categoryDAO.remove(id);

        logger.info("CategoryService: Category removed successfully. ID: " + id);
    }

    @Override
    public Category findById(Long id) {
        logger.fine("CategoryService: Finding category by ID: " + id);

        if (id == null) {
            logger.warning("CategoryService: ID cannot be null for findById");
            return null;
        }

        return categoryDAO.findById(id);
    }

    @Override
    public List<Category> findAll() {
        logger.fine("CategoryService: Finding all categories");
        return categoryDAO.findAll();
    }

    @Override
    public Category findCategoryWithMostBooks() {
        logger.fine("CategoryService: Finding category with most books");
        return categoryDAO.findCategoryWithMostBooks();
    }

    @Override
    public Map<String, Integer> getCategoryBookCounts() {
        logger.fine("CategoryService: Getting book counts for all categories");
        return categoryDAO.getCategoryBookCounts();
    }

    @Override
    public boolean canRemoveCategory(Long categoryId) {
        if (categoryId == null) {
            return false;
        }

        try {

            Map<String, Integer> bookCounts = categoryDAO.getCategoryBookCounts();

            Category category = categoryDAO.findById(categoryId);
            if (category == null) {
                return false;
            }

            Integer bookCount = bookCounts.get(category.getName());
            return bookCount == null || bookCount == 0;

        } catch (Exception e) {
            logger.warning("CategoryService: Error checking if category can be removed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean categoryNameExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        List<Category> allCategories = categoryDAO.findAll();
        return allCategories.stream()
                .anyMatch(category -> category.getName().equalsIgnoreCase(name.trim()));
    }

    // Private validation methods

    private void validateCategoryForSave(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            logger.warning("CategoryService: Category name validation failed: empty or null");
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        if (category.getDescription() == null || category.getDescription().trim().isEmpty()) {
            logger.warning("CategoryService: Category description validation failed: empty or null");
            throw new IllegalArgumentException("Category description cannot be empty");
        }
    }

    private void validateCategoryForUpdate(Category category) {
        validateCategoryForSave(category);

        if (category.getId() == null) {
            logger.warning("CategoryService: Category ID validation failed: null ID for update");
            throw new IllegalArgumentException("Category ID cannot be null for update");
        }
    }
}
