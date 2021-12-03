package com.curso.udemy.api.repository;

import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.domain.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookAndReturnedIsFalse( @Param("book") Book book);

    @Query( value = " SELECT l FROM Loan l JOIN l.book b WHERE b.isbn =?1 OR l.customer =?2 ")
    Page<Loan> findByBookIsbnOrCustomer(String isbn,
                                        String customer,
                                        Pageable pageable);

    Page<Loan> findByBook(Book book, Pageable pageable);

    @Query(value = "SELECT l FROM Loan l WHERE l.loanDate <= ?1 " +
            " AND l.returned IS NULL OR l.returned IS FALSE")
    List<Loan> findByLoansDateLessThanAndNotReturned(LocalDate threeDaysAgo);
}
