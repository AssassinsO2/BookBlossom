package com.example.bookblossom.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import java.util.ArrayList;

public class MyBooksViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Book>> booksLiveData = new MutableLiveData<>();
    private ArrayList<Book> bookList = new ArrayList<>();

    public LiveData<ArrayList<Book>> getBooks() {
        return booksLiveData;
    }

    public void setBooks(ArrayList<Book> books) {
        bookList = books;
        booksLiveData.setValue(bookList);
    }

    public ArrayList<Book> getBookList() {
        return bookList;
    }
}
