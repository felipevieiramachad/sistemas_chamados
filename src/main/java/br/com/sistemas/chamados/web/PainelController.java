package br.com.sistemas.chamados.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.ChamadoService;

@Controller
public class PainelController {

    public record TotalStatus(StatusChamado status, long total) { }

    private final ChamadoService chamadoService;

    public PainelController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/web";
    }

    @GetMapping({"/web", "/web/"})
    public String painel(Model model) {
        // pageSize 1: só interessa o total de cada status
        List<TotalStatus> totais = Arrays.stream(StatusChamado.values())
            .map(s -> new TotalStatus(s,
                chamadoService.listar(s, null, null, PageRequest.of(0, 1)).totalElementos()))
            .toList();
        model.addAttribute("totais", totais);
        model.addAttribute("recentes", chamadoService.listar(null, null, null,
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dataAbertura"))).conteudo());
        return "painel";
    }
}
