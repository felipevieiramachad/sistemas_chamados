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

import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.web.form.CategoriaForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/categorias")
public class CategoriaWebController {

    private final CategoriaService service;

    public CategoriaWebController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CategoriaForm());
        dadosDoFormulario(model, null);
        return "categorias/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") CategoriaForm form,
                        BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                service.criar(form.paraRequest());
                flash.addFlashAttribute("sucesso", "Categoria cadastrada.");
                return "redirect:/web/categorias";
            } catch (RegraNegocioException e) {
                resultado.rejectValue("nome", "duplicado", e.getMessage());
            }
        }
        dadosDoFormulario(model, null);
        return "categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", CategoriaForm.de(service.buscar(id)));
        dadosDoFormulario(model, id);
        return "categorias/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") CategoriaForm form,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                service.atualizar(id, form.paraRequest());
                flash.addFlashAttribute("sucesso", "Categoria atualizada.");
                return "redirect:/web/categorias";
            } catch (RegraNegocioException e) {
                resultado.rejectValue("nome", "duplicado", e.getMessage());
            }
        }
        dadosDoFormulario(model, id);
        return "categorias/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try {
            service.excluir(id);
            flash.addFlashAttribute("sucesso", "Categoria excluída.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/categorias";
    }

    private void dadosDoFormulario(Model model, Long id) {
        model.addAttribute("editando", id != null);
        model.addAttribute("acao", id == null ? "/web/categorias" : "/web/categorias/" + id);
    }
}
