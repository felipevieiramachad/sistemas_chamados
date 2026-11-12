package br.com.sistemas.chamados.web;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.web.form.ClienteForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/clientes")
public class ClienteWebController {

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteWebController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ClienteForm());
        dadosDoFormulario(model, null);
        return "clientes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteForm form,
                        BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                service.criar(form.paraRequest());
                flash.addFlashAttribute("sucesso", "Cliente cadastrado.");
                return "redirect:/web/clientes";
            } catch (RegraNegocioException e) {
                resultado.rejectValue("email", "duplicado", e.getMessage());
            }
        }
        dadosDoFormulario(model, null);
        return "clientes/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id,
                          @PageableDefault(size = 5, sort = "dataAbertura",
                              direction = Sort.Direction.DESC) Pageable pageable,
                          Model model) {
        model.addAttribute("cliente", service.buscar(id));
        model.addAttribute("chamados", chamadoService.listarPorCliente(id, null, null, pageable));
        return "clientes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", ClienteForm.de(service.buscar(id)));
        dadosDoFormulario(model, id);
        return "clientes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") ClienteForm form,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                service.atualizar(id, form.paraRequest());
                flash.addFlashAttribute("sucesso", "Cliente atualizado.");
                return "redirect:/web/clientes/" + id;
            } catch (RegraNegocioException e) {
                resultado.rejectValue("email", "duplicado", e.getMessage());
            }
        }
        dadosDoFormulario(model, id);
        return "clientes/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
            flash.addFlashAttribute("sucesso", "Cliente excluído.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/clientes";
    }

    private void dadosDoFormulario(Model model, Long id) {
        model.addAttribute("editando", id != null);
        model.addAttribute("acao", id == null ? "/web/clientes" : "/web/clientes/" + id);
    }
}
