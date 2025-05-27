package br.com.libraryjdbc.service;


import java.util.List;
import br.com.libraryjdbc.model.entities.Book;

public interface BookService {

    Book save(Book book);
    void update(Book book);
    void remove(Long id);
    Book findById(Long id);
    List<Book> findAll();
    List<Book> findByAuthor(String author);
    List<Book> findByCategory(Long categoryId);
    boolean isbnExists(String isbn);
    boolean categoryExists(Long categoryId);
    boolean isValidReleaseYear(Integer year);
}