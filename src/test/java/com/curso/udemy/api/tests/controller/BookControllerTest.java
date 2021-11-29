package com.curso.udemy.api.tests.controller;

import com.curso.udemy.api.domain.dto.BookDTO;
import com.curso.udemy.api.domain.entity.Book;
import com.curso.udemy.api.exception.BusinessException;
import com.curso.udemy.api.service.BookService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService service;

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


    private BookDTO createBookDTO() {
        return BookDTO.builder()
                .author("Arthur")
                .title("As Aventuras")
                .isbn("001")
                .build();
    }
}
