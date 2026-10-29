package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

public record ChamadoDetalheResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataAbertura,
    LocalDateTime dataFechamento,
    StatusChamado status,
    Prioridade prioridade,
    Long clienteId,
    String clienteNome,
    Long categoriaId,
    String categoriaNome,
    Long tecnicoId,
    String tecnicoNome,
    List<ComentarioResponse> comentarios
) {
    public static ChamadoDetalheResponse de(Chamado c, List<ComentarioResponse> comentarios) {
        ChamadoResponse r = ChamadoResponse.de(c);
        return new ChamadoDetalheResponse(
            r.id(), r.titulo(), r.descricao(),
            r.dataAbertura(), r.dataFechamento(),
            r.status(), r.prioridade(),
            r.clienteId(), r.clienteNome(),
            r.categoriaId(), r.categoriaNome(),
            r.tecnicoId(), r.tecnicoNome(),
            comentarios);
    }
}
