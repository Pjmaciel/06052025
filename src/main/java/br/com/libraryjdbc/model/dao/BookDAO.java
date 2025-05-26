package br.com.libraryjdbc.model.dao;

import java.util.List;
import br.com.libraryjdbc.model.entities.Book;

public interface BookDAO {

    Book save(Book book);
    void update(Book book);
    void remove(Long id);
    Book findById(Long id);
    List<Book> findAll();
    List<Book> findByAuthor(String author);
    List<Book> findByCategory(Long categoryId);
}