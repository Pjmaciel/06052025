package br.com.libraryjdbc.app;

import br.com.libraryjdbc.model.dao.BookDAO;
import br.com.libraryjdbc.model.dao.CategoryDAO;
import br.com.libraryjdbc.model.entities.Book;
import br.com.libraryjdbc.model.entities.Category;
import br.com.libraryjdbc.model.impl.BookDAOImpl;
import br.com.libraryjdbc.model.impl.CategoryDAOImpl;
import db.DB;

public class Program {

    public static void main(String[] args) {
        try {
            DB.getConnection();
            System.out.println("Database connected successfully!");

            // Create entities using constructors
            Category category = new Category("Technical", "Programming and technical books");
            Book book = new Book("Clean Code", "Robert C. Martin",
                    "A handbook of agile software craftsmanship", "9780132350884", 2008, category);

            // Create DAOs using DAO pattern
            CategoryDAO categoryDAO = new CategoryDAOImpl();
            BookDAO bookDAO = new BookDAOImpl();

            // Save using DAO interfaces
            Category savedCategory = categoryDAO.save(category);
            book.setCategory(savedCategory); // Update with saved category (has ID)
            Book savedBook = bookDAO.save(book);

            // Print information
            System.out.println("\n Category saved:");
            System.out.println(savedCategory);

            System.out.println("\n Book saved:");
            System.out.println(savedBook);

            System.out.println("\n✅ DAO Pattern working successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        } finally {
            DB.closeConnection();
            System.out.println("✅ Database connection closed.");
        }
    }
}