package com.curso.udemy.api.tests.controller;

import com.curso.udemy.api.controller.BookController;
import com.curso.udemy.api.domain.dto.BookDTO;
import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.exception.BusinessException;
import com.curso.udemy.api.service.BookService;
import com.curso.udemy.api.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest (controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {

        BookDTO bookDTO = createBookDTO();

        Book savedBook = Book.builder()
                .id(10L)
                .author("Arthur")
                .title("As Aventuras")
                .isbn("001")
                .build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").isNotEmpty() )
                .andExpect( jsonPath("title").value(bookDTO.getTitle()) )
                .andExpect( jsonPath("author").value(bookDTO.getAuthor()) )
                .andExpect( jsonPath("isbn").value(bookDTO.getIsbn()) );
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para a criação do livro.")
    public void createInvalidBookTest() throws Exception {

        BookDTO bookDTO = new BookDTO();

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(3)));

    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com ISBN já utilizado por outro.")
    public void createBookWithDuplicatedIsbnTest() throws Exception {

        String msgErro = "ISBN já cadastrado.";

        BookDTO bookDTO = createBookDTO();

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(msgErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc
                .perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", hasSize(1)))
                .andExpect( jsonPath("errors[0]").value(msgErro));

    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception {
        //cenário (given)
        Long id = 1L;
        Book book = Book.builder().id(id)
                .title(createBookDTO().getTitle())
                .author(createBookDTO().getAuthor())
                .isbn(createBookDTO().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        //verificação (then)
        mockMvc.perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(createBookDTO().getTitle()) )
                .andExpect( jsonPath("author").value(createBookDTO().getAuthor()))
                .andExpect( jsonPath("isbn").value(createBookDTO().getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar Not Found quando o livro procurado não existir.")
    public void bookNotFoundTest() throws Exception {
        Long id = 1L;

        //cenário (given)
         BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        //verificação (then)
        mockMvc
                .perform(request)
                .andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest()  throws Exception {

        Long id = 1L;

        //cenário (given)
        BDDMockito.given(
                service
                .getById(Mockito.anyLong()))
                .willReturn(Optional.of(Book.builder().id(id).build()));

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id));

        //verificação (then)
        mockMvc
                .perform(request)
                .andExpect( status().isNoContent() );
    }

    @Test
    @DisplayName("Deve retornar Not Found ao tentar deletar um livo inexistente.")
    public void deleteInexistentBookTest()  throws Exception {

        //cenário (given)
        Long id = 1L;

        BDDMockito.given(
                        service
                            .getById(Mockito.anyLong()))
                            .willReturn(Optional.empty());

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id));

        //verificação (then)
        mockMvc
                .perform(request)
                .andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest()  throws Exception {

        //cenário (given)
        Long id = 1L;

        String json = new ObjectMapper().writeValueAsString(createBookDTO());

        Book book = Book.builder().id(id)
                .author("some author").title("some title").isbn("321").build();

        BDDMockito.given( service.getById(id)).willReturn( Optional.of(book) );

        Book updatedBook = Book.builder().id(id)
                .author("Arthur").title("As Aventuras").isbn("321").build();
        BDDMockito.given( service.update(book)).willReturn(updatedBook);

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);


        //verificação (then)
        mockMvc.perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(createBookDTO().getTitle()) )
                .andExpect( jsonPath("author").value(createBookDTO().getAuthor()))
                .andExpect( jsonPath("isbn").value("321"));
    }

    @Test
    @DisplayName("Deve retornar Not Found ao tentar atualizar um livo inexistente.")
    public void updateInexistentBookTest()  throws Exception {

        //cenário (given)
        Long id = 1L;

        String json = new ObjectMapper().writeValueAsString(createBookDTO());

        BDDMockito.given(
                        service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        //execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        //verificação (then)
        mockMvc
                .perform(request)
                .andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {
        Long id = 1L;

        Book book = Book.builder().id(id).title(createBookDTO().getTitle())
                .author(createBookDTO().getAuthor()).isbn(createBookDTO().getIsbn()).build();

        BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
                .willReturn( new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1) );

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", Matchers.hasSize(1)))
                .andExpect( jsonPath("totalElements").value(1))
                .andExpect( jsonPath("pageable.pageSize").value(100))
                .andExpect( jsonPath("pageable.pageNumber").value(0));

    }

    private BookDTO createBookDTO() {
        return BookDTO.builder()
                .author("Arthur")
                .title("As Aventuras")
                .isbn("001")
                .build();
    }
}
