package br.com.libraryjdbc.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;

public class BookFactory {

    public static Book fromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setSynopsis(rs.getString("synopsis"));
        book.setIsbn(rs.getString("isbn"));
        book.setReleaseYear(rs.getInt("release_year"));
        return book;
    }

    public static Book fromResultSetWithCategory(ResultSet rs) throws SQLException {
        Book book = fromResultSet(rs);

        Category category = new Category();
        category.setId(rs.getLong("category_id"));
        category.setName(rs.getString("category_name"));
        category.setDescription(rs.getString("category_description"));

        book.setCategory(category);
        return book;
    }

    public static Book create(String title, String author, String synopsis, String isbn, Integer releaseYear, Category category) {
        return new Book(title, author, synopsis, isbn, releaseYear, category);
    }

    public static Book create(Long id, String title, String author, String synopsis, String isbn, Integer releaseYear, Category category) {
        return new Book(id, title, author, synopsis, isbn, releaseYear, category);
    }
}