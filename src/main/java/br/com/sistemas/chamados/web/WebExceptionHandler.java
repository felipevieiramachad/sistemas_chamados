package br.com.sistemas.chamados.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;

/**
 * Trata os erros das telas (pacote web) mostrando uma página HTML.
 * Os controllers REST continuam sendo atendidos pelo GlobalExceptionHandler (JSON).
 */
@ControllerAdvice(basePackages = "br.com.sistemas.chamados.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(RecursoNaoEncontradoException ex, Model model) {
        model.addAttribute("tituloErro", "Não encontrado");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String regraNegocio(RegraNegocioException ex, Model model) {
        model.addAttribute("tituloErro", "Não foi possível concluir");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }
}
