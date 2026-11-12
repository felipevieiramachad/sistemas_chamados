package br.com.sistemas.chamados.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import br.com.sistemas.chamados.web.form.TecnicoForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/tecnicos")
public class TecnicoWebController {

    private final TecnicoService service;

    public TecnicoWebController(TecnicoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new TecnicoForm());
        dadosDoFormulario(model, null);
        return "tecnicos/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") TecnicoForm form,
                        BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                service.criar(form.paraRequest());
                flash.addFlashAttribute("sucesso", "Técnico cadastrado.");
                return "redirect:/web/tecnicos";
            } catch (RegraNegocioException e) {
                resultado.rejectValue("email", "duplicado", e.getMessage());
            }
        }
        dadosDoFormulario(model, null);
        return "tecnicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", TecnicoForm.de(service.buscar(id)));
        dadosDoFormulario(model, id);
        return "tecnicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") TecnicoForm form,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                service.atualizar(id, form.paraRequest());
                flash.addFlashAttribute("sucesso", "Técnico atualizado.");
                return "redirect:/web/tecnicos";
            } catch (RegraNegocioException e) {
                resultado.rejectValue("email", "duplicado", e.getMessage());
            }
        }
        dadosDoFormulario(model, id);
        return "tecnicos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
            flash.addFlashAttribute("sucesso", "Técnico excluído.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/tecnicos";
    }

    private void dadosDoFormulario(Model model, Long id) {
        model.addAttribute("editando", id != null);
        model.addAttribute("acao", id == null ? "/web/tecnicos" : "/web/tecnicos/" + id);
        model.addAttribute("perfis", Perfil.values());
    }
}
