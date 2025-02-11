package com.curso.udemy.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Long id;

    @NotBlank
    private String isbn;

    @NotBlank
    private String customer;

    @NotBlank
    private String customerEmail;

    private BookDTO book;
}
