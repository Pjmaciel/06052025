package br.com.libraryjdbc.service;

import java.util.List;
import java.util.Map;
import br.com.libraryjdbc.model.entities.Category;


public interface CategoryService {

    Category save(Category category);
    void update(Category category);
    void remove(Long id);
    Category findById(Long id);
    List<Category> findAll();
    Category findCategoryWithMostBooks();
    Map<String, Integer> getCategoryBookCounts();
    boolean canRemoveCategory(Long categoryId);
    boolean categoryNameExists(String name);
}
