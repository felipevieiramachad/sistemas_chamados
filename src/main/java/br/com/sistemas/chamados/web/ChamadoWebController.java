package br.com.sistemas.chamados.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import br.com.sistemas.chamados.web.form.ChamadoForm;
import br.com.sistemas.chamados.web.form.ComentarioForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/chamados")
public class ChamadoWebController {

    private static final String ORDEM_PADRAO = "dataAbertura,desc";

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;
    private final ComentarioService comentarioService;

    public ChamadoWebController(ChamadoService chamadoService,
                                ClienteService clienteService,
                                CategoriaService categoriaService,
                                TecnicoService tecnicoService,
                                ComentarioService comentarioService) {
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
        this.comentarioService = comentarioService;
    }

    // ---------- lista com filtros, ordenação e paginação ----------

    @GetMapping
    public String listar(@RequestParam(required = false) StatusChamado status,
                         @RequestParam(required = false) Prioridade prioridade,
                         @RequestParam(required = false) Long clienteId,
                         @RequestParam(required = false) String sort,
                         @PageableDefault(size = 10, sort = "dataAbertura",
                             direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {
        model.addAttribute("chamados", chamadoService.listar(status, prioridade, clienteId, pageable));
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("statusOpcoes", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("filtroStatus", status);
        model.addAttribute("filtroPrioridade", prioridade);
        model.addAttribute("filtroCliente", clienteId);
        model.addAttribute("ordem", (sort == null || sort.isBlank()) ? ORDEM_PADRAO : sort);
        model.addAttribute("filtrando", status != null || prioridade != null || clienteId != null);
        model.addAttribute("consulta", consulta(status, prioridade, clienteId, sort));
        return "chamados/lista";
    }

    // monta "status=ABERTO&prioridade=ALTA&" para os links da paginação manterem os filtros
    private String consulta(StatusChamado status, Prioridade prioridade, Long clienteId, String sort) {
        StringBuilder q = new StringBuilder();
        if (status != null) q.append("status=").append(status.name()).append('&');
        if (prioridade != null) q.append("prioridade=").append(prioridade.name()).append('&');
        if (clienteId != null) q.append("clienteId=").append(clienteId).append('&');
        if (sort != null && !sort.isBlank()) {
            q.append("sort=").append(URLEncoder.encode(sort, StandardCharsets.UTF_8)).append('&');
        }
        return q.toString();
    }

    // ---------- novo / editar ----------

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId, Model model) {
        ChamadoForm form = new ChamadoForm();
        form.setPrioridade(Prioridade.MEDIA);
        form.setClienteId(clienteId);
        model.addAttribute("form", form);
        dadosDoFormulario(model, null);
        return "chamados/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ChamadoForm form,
                        BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                ChamadoResponse criado = chamadoService.criar(form.paraRequest());
                flash.addFlashAttribute("sucesso", "Chamado aberto.");
                return "redirect:/web/chamados/" + criado.id();
            } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
                resultado.reject("regra", e.getMessage());
            }
        }
        dadosDoFormulario(model, null);
        return "chamados/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        ChamadoDetalheResponse chamado = chamadoService.buscar(id);
        if (chamado.status() == StatusChamado.FECHADO) {
            flash.addFlashAttribute("erro", "Chamado fechado não pode ser alterado.");
            return "redirect:/web/chamados/" + id;
        }
        model.addAttribute("form", ChamadoForm.de(chamado));
        dadosDoFormulario(model, id);
        return "chamados/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ChamadoForm form,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                chamadoService.atualizar(id, form.paraRequest());
                flash.addFlashAttribute("sucesso", "Chamado atualizado.");
                return "redirect:/web/chamados/" + id;
            } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
                resultado.reject("regra", e.getMessage());
            }
        }
        dadosDoFormulario(model, id);
        return "chamados/form";
    }

    private void dadosDoFormulario(Model model, Long id) {
        model.addAttribute("id", id);
        model.addAttribute("editando", id != null);
        model.addAttribute("acao", id == null ? "/web/chamados" : "/web/chamados/" + id);
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("prioridades", Prioridade.values());
    }

    // ---------- detalhe, status, técnico e comentários ----------

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        dadosDoDetalhe(id, model);
        if (!model.containsAttribute("comentarioForm")) {
            model.addAttribute("comentarioForm", new ComentarioForm());
        }
        return "chamados/detalhe";
    }

    private void dadosDoDetalhe(Long id, Model model) {
        model.addAttribute("chamado", chamadoService.buscar(id));
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("statusOpcoes", StatusChamado.values());
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam StatusChamado status,
                                RedirectAttributes flash) {
        try {
            chamadoService.alterarStatus(id, new AlterarStatusRequest(status));
            flash.addFlashAttribute("sucesso", "Status alterado para " + status.getRotulo() + ".");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id,
                           @RequestParam(required = false) Long tecnicoId,
                           RedirectAttributes flash) {
        if (tecnicoId == null) {
            flash.addFlashAttribute("erro", "Selecione um técnico.");
            return "redirect:/web/chamados/" + id;
        }
        try {
            chamadoService.atribuirTecnico(id, new AtribuirTecnicoRequest(tecnicoId));
            flash.addFlashAttribute("sucesso", "Técnico atribuído.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/chamados/" + id;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id,
                           @Valid @ModelAttribute("comentarioForm") ComentarioForm form,
                           BindingResult resultado, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            dadosDoDetalhe(id, model);
            return "chamados/detalhe";
        }
        comentarioService.criar(id, form.paraRequest());
        flash.addFlashAttribute("sucesso", "Comentário publicado.");
        return "redirect:/web/chamados/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        chamadoService.excluir(id);
        flash.addFlashAttribute("sucesso", "Chamado excluído.");
        return "redirect:/web/chamados";
    }
}
