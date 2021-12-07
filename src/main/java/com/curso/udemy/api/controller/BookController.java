package com.curso.udemy.api.controller;

import com.curso.udemy.api.domain.dto.BookDTO;
import com.curso.udemy.api.domain.dto.LoanDTO;
import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.domain.entity.Loan;
import com.curso.udemy.api.service.BookService;
import com.curso.udemy.api.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "BOOK API", description = "BOOK API")
@Slf4j //log do lombok
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;
    private final LoanService loanService;


    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Detail Book", description = "Gets a book details by id")
    public BookDTO getDetailBook(@PathVariable Long id)  {
        log.info(" Obtaining details for book id: {}", id);
        return bookService
                .getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND ));

    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes a book", description = "Deletes a book by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book succesfully deleted")
    })
    public void delete(@PathVariable Long id)  {

        log.info(" Delete book by id: {}", id);

        Book book = bookService
                .getById(id)
                .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND ));

        bookService.delete(book);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a book", description = "Creates a book")
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO) {
        log.info(" Creating a book for isbn: {}", bookDTO.getIsbn());
        Book entity = modelMapper.map(bookDTO, Book.class);
        entity = bookService.save(entity);
        return modelMapper.map(entity, BookDTO.class);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Updates a book", description = "Updates a book by id")
    public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO bookDTO) {

        log.info(" Updating book by id: {}", id);

        return bookService
                .getById(id)
                .map( book -> {
                    book.setAuthor(bookDTO.getAuthor());
                    book.setTitle(bookDTO.getTitle());
                    book = bookService.update(book);
                    return modelMapper.map(book, BookDTO.class);
                } )
                .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND ));

    }

    @GetMapping
    @Operation(summary = "Find books", description = "Finds books")
    public Page<BookDTO> find(BookDTO bookDTO, Pageable pageRequest) {
        Book filter = modelMapper.map(bookDTO, Book.class);
        Page<Book> result = bookService.find(filter, pageRequest);
        List<BookDTO> list = result.getContent().stream()
                .map( entity -> modelMapper.map(entity, BookDTO.class ))
                .collect( Collectors.toList() );

        return new PageImpl<BookDTO>( list, pageRequest, result.getTotalElements() );
    }

    @GetMapping("{id}/loans")
    @Operation(summary = "Find book loans", description = "Find book loans by id")
    public Page<LoanDTO> loansByBook( @PathVariable Long id, Pageable pageable) {

        Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map( loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList()) ;

        return new PageImpl<LoanDTO>(loans, pageable, result.getTotalElements());
    }

}
