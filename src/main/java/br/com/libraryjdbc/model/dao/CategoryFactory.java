package br.com.libraryjdbc.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import br.com.libraryjdbc.model.entities.Category;

public class CategoryFactory {

    public static Category fromResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }

    public static Category create(String name, String description) {
        return new Category(name, description);
    }

    public static Category create(Long id, String name, String description) {
        return new Category(id, name, description);
    }
}