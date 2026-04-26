package com.example.librasys.service;

import com.example.librasys.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LibraryService {
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    public LibraryService() {
        seedData();
    }

    public ObservableList<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(Book book) {
        books.remove(book);
    }

    public int getTotalBooks() {
        return books.size();
    }

    public int getTotalCopies() {
        return books.stream().mapToInt(Book::getCopies).sum();
    }

    private void seedData() {
        books.addAll(
                new Book("Clean Code", "Robert C. Martin", "9780132350884", "Informatique", 4),
                new Book("Effective Java", "Joshua Bloch", "9780134685991", "Informatique", 3),
                new Book("Design Patterns", "GoF", "9780201633610", "Architecture", 2),
                new Book("Le Petit Prince", "Antoine de Saint-Exupery", "9780156013987", "Litterature", 6)
        );
    }
}
