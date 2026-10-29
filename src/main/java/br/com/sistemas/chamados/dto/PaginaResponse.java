package br.com.sistemas.chamados.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginaResponse<T>(
    List<T> conteudo,
    int pagina,
    int tamanho,
    long totalElementos,
    int totalPaginas
) {
    public static <T> PaginaResponse<T> de(Page<T> p) {
        return new PaginaResponse<>(
            p.getContent(), p.getNumber(), p.getSize(),
            p.getTotalElements(), p.getTotalPages());
    }
}
