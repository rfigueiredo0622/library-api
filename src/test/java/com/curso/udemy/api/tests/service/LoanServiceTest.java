package com.curso.udemy.api.tests.service;

import com.curso.udemy.api.domain.dto.LoanFilterDTO;
import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.domain.entity.Loan;
import com.curso.udemy.api.exception.BusinessException;
import com.curso.udemy.api.repository.LoanRepository;
import com.curso.udemy.api.service.LoanService;
import com.curso.udemy.api.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService service;

    @MockBean
    private LoanRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest() {

        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when( repository.existsByBookAndReturnedIsFalse(book) ).thenReturn( false );
        when( repository.save( savingLoan ) ).thenReturn( savedLoan );

        Loan loan = service.save(savingLoan);

        assertThat( loan.getId() ).isEqualTo( savedLoan.getId() );
        assertThat( loan.getBook().getId() ).isEqualTo( savedLoan.getBook().getId());
        assertThat( loan.getCustomer() ).isEqualTo( savedLoan.getCustomer() );
        assertThat( loan.getLoanDate() ).isEqualTo( savedLoan.getLoanDate() );
    }

    @Test
    @DisplayName("Deve lançar erro ao salvar um empréstimo de livro já emprestado.")
    public void loanedBookTest() {

        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when( repository.existsByBookAndReturnedIsFalse(book) ).thenReturn( true );

        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetailsTest() {

        //cenário
        Long id = 1L;

        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when( repository.findById(id) ).thenReturn(Optional.of(loan));

        //execução
        Optional<Loan> result = service.getById(id);

        //verificação
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify( repository ).findById(id);

    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest() {
        //cenário
        Long id = 1L;

        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        when( repository.save(loan) ).thenReturn( loan );

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();

        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest() {
        //cenário
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder()
                .isbn("321")
                .customer("Fulano")
                .build();

        Loan loan = createLoan();
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, 1);
        when(repository.findByBookIsbnOrCustomer(Mockito.anyString(),
                                                 Mockito.anyString(),
                                                 Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execução
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        //verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    private Loan createLoan() {
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan loan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        return loan;
    }
}
