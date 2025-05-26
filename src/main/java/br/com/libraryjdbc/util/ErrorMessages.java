package br.com.libraryjdbc.util;


public class ErrorMessages {

    // Category Error Messages
    public static final String CATEGORY_NAME_EMPTY = "Category name cannot be empty";
    public static final String CATEGORY_DESCRIPTION_EMPTY = "Category description cannot be empty";
    public static final String CATEGORY_NAME_EXISTS = "Category name already exists: %s";
    public static final String CATEGORY_ID_NULL_UPDATE = "Category ID cannot be null for update";
    public static final String CATEGORY_NOT_FOUND = "Category with ID %d not found";
    public static final String CATEGORY_HAS_BOOKS = "Cannot remove category that has associated books";

    // Book Error Messages
    public static final String BOOK_TITLE_EMPTY = "Book title cannot be empty";
    public static final String BOOK_AUTHOR_EMPTY = "Book author cannot be empty";
    public static final String BOOK_ISBN_EMPTY = "Book ISBN cannot be empty";
    public static final String BOOK_YEAR_NULL = "Book release year cannot be null";
    public static final String BOOK_YEAR_INVALID = "Book release year must be >= 1967, received: %d";
    public static final String BOOK_CATEGORY_INVALID = "Book must have a valid category";
    public static final String BOOK_ISBN_EXISTS = "ISBN already exists in the system: %s";
    public static final String BOOK_CATEGORY_NOT_EXISTS = "Category with ID %d does not exist";
    public static final String BOOK_ID_NULL_UPDATE = "Book ID cannot be null for update";
    public static final String BOOK_NOT_FOUND = "Book with ID %d not found";

    // Database Operation Error Messages
    public static final String DB_INSERT_ERROR = "Error inserting %s: %s";
    public static final String DB_UPDATE_ERROR = "Error updating %s: %s";
    public static final String DB_DELETE_ERROR = "Error removing %s: %s";
    public static final String DB_SELECT_ERROR = "Error finding %s: %s";
    public static final String DB_LIST_ERROR = "Error listing %s: %s";
    public static final String DB_VALIDATION_ERROR = "Error validating %s: %s";

    // Transaction Error Messages
    public static final String TRANSACTION_ROLLBACK = "Transaction rolled back due to error";
    public static final String TRANSACTION_COMMIT_ERROR = "Error committing transaction: %s";
    public static final String TRANSACTION_ROLLBACK_ERROR = "Error during transaction rollback: %s";

    // General Error Messages
    public static final String UNEXPECTED_ERROR = "Unexpected error occurred: %s";
    public static final String NO_ROWS_AFFECTED = "Unexpected error! No rows were affected during %s operation";

    // Success Messages
    public static final String CATEGORY_INSERTED_SUCCESS = "Category inserted successfully! ID: %d";
    public static final String BOOK_INSERTED_SUCCESS = "Book inserted successfully! ID: %d";

    private ErrorMessages() {
        // Utility class - no instantiation
    }
}