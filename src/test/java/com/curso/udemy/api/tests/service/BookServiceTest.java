package com.curso.udemy.api.tests.service;

import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.exception.BusinessException;
import com.curso.udemy.api.repository.BookRepository;
import com.curso.udemy.api.service.BookService;
import com.curso.udemy.api.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        //cenário
        Book book = createValidBook();

        Mockito.when( repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        Mockito.when( repository.save(book) ).thenReturn(Book.builder()
                                                                .id(1L)
                                                                .isbn("123")
                                                                .author("Fulano")
                                                                .title("As aventuras")
                                                                .build());

        //execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com ISBN duplicado.")
    public void shouldNotSaveBookWithDuplicatedIsbn() {
        //cenário
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //verificação
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createValidBook() {
        return Book.builder()
                .isbn("123")
                .author("Fulano")
                .title("As aventuras")
                .build();
    }

}
