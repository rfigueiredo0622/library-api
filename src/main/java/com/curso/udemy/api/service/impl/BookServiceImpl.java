package com.curso.udemy.api.service.impl;

import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.exception.BusinessException;
import com.curso.udemy.api.repository.BookRepository;
import com.curso.udemy.api.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }


    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("ISBN já cadastrado.");

        }
        return repository.save(book);
    }
}
