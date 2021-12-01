package com.curso.udemy.api.service;

import com.curso.udemy.api.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {

    public Book save(Book book);

    public Optional<Book> getById(Long id);

    public void delete(Book book);

    public Book update(Book book);

    public Page<Book> find(Book filter, Pageable pageRequest);

    Optional<Book> getBookByIsbn(String isbn);
}
