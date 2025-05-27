package br.com.libraryjdbc.model.dao;

import java.util.List;
import java.util.Map;

import br.com.libraryjdbc.model.entities.Category;

public interface CategoryDAO {

    Category save(Category category);
    void update(Category category);
    void remove(Long id);
    Category findById(Long id);
    List<Category> findAll();
    Category findCategoryWithMostBooks();
    Map<String, Integer> getCategoryBookCounts();

}