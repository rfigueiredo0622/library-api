package com.curso.udemy.api.controller;

import com.curso.udemy.api.domain.dto.BookDTO;
import com.curso.udemy.api.domain.dto.LoanDTO;
import com.curso.udemy.api.domain.dto.LoanFilterDTO;
import com.curso.udemy.api.domain.dto.ReturnedLoanDTO;
import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.domain.entity.Loan;
import com.curso.udemy.api.service.BookService;
import com.curso.udemy.api.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j //log do lombok
@Tag(name = "LOAN API", description = "LOAN API")
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a loan", description = "Creates a new book loan.")
    public Long create(@RequestBody LoanDTO loanDTO) {
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for isbn"));

        Loan loan = Loan.builder()
                .book(book)
                .customer(loanDTO.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        loan = loanService.save(loan);

        return loan.getId();
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Return a book by loan", description = "Gets a book by loan id")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = loanService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned((dto.getReturned()));

        loanService.update(loan);
    }

    @GetMapping
    @Operation(summary = "Find loans", description = "Finds loans by filters")
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
        Page<Loan> result = loanService.find(dto, pageRequest);
        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map( entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);

                    loanDTO.setBook(bookDTO);

                    return loanDTO;
                }).collect(Collectors.toList()) ;

        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());

    }
}
