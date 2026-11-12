package br.com.sistemas.chamados.web.form;

import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChamadoForm {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
    private String titulo;

    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    @NotNull(message = "Prioridade é obrigatória")
    private Prioridade prioridade;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaId;

    public static ChamadoForm de(ChamadoDetalheResponse d) {
        ChamadoForm form = new ChamadoForm();
        form.titulo = d.titulo();
        form.descricao = d.descricao();
        form.prioridade = d.prioridade();
        form.clienteId = d.clienteId();
        form.categoriaId = d.categoriaId();
        return form;
    }

    public ChamadoRequest paraRequest() {
        String desc = (descricao == null || descricao.isBlank()) ? null : descricao;
        return new ChamadoRequest(titulo, desc, prioridade, clienteId, categoriaId);
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
