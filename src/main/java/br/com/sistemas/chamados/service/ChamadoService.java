package br.com.sistemas.chamados.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.dto.PaginaResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import jakarta.persistence.criteria.Predicate;

@Service
public class ChamadoService {

    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ComentarioRepository comentarioRepository;

    public ChamadoService(ChamadoRepository repository,
                          ClienteRepository clienteRepository,
                          CategoriaRepository categoriaRepository,
                          TecnicoRepository tecnicoRepository,
                          ComentarioRepository comentarioRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.comentarioRepository = comentarioRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Cliente cliente = buscarCliente(dto.clienteId());
        Categoria categoria = buscarCategoria(dto.categoriaId());
        Chamado salvo = repository.save(new Chamado(
            dto.titulo(), dto.descricao(), dto.prioridade(), cliente, categoria));
        return ChamadoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade,
                                                  Long clienteId, Pageable pageable) {
        Specification<Chamado> filtros = (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (status != null) {
                predicados.add(cb.equal(root.get("status"), status));
            }
            if (prioridade != null) {
                predicados.add(cb.equal(root.get("prioridade"), prioridade));
            }
            if (clienteId != null) {
                predicados.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
        Page<ChamadoResponse> pagina = repository.findAll(filtros, pageable)
            .map(ChamadoResponse::de);
        return PaginaResponse.de(pagina);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ChamadoResponse> listarPorCliente(Long clienteId, StatusChamado status,
                                                            Prioridade prioridade, Pageable pageable) {
        buscarCliente(clienteId); // 404 se o cliente não existir
        return listar(status, prioridade, clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponse buscar(Long id) {
        Chamado chamado = buscarEntity(id);
        List<ComentarioResponse> comentarios = comentarioRepository
            .findByChamadoIdOrderByDataAsc(id)
            .stream().map(ComentarioResponse::de).toList();
        return ChamadoDetalheResponse.de(chamado, comentarios);
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest dto) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);
        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCliente(buscarCliente(dto.clienteId()));
        chamado.setCategoria(buscarCategoria(dto.categoriaId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, AlterarStatusRequest dto) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);
        chamado.setStatus(dto.status());
        if (dto.status() == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse atribuirTecnico(Long id, AtribuirTecnicoRequest dto) {
        Chamado chamado = buscarEntity(id);
        garantirNaoFechado(chamado);
        Tecnico tecnico = tecnicoRepository.findById(dto.tecnicoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico " + dto.tecnicoId() + " não encontrado"));
        chamado.setTecnico(tecnico);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        Chamado chamado = buscarEntity(id);
        comentarioRepository.deleteByChamadoId(id);
        repository.delete(chamado);
    }

    private void garantirNaoFechado(Chamado chamado) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Chamado " + chamado.getId() + " já está fechado");
        }
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Cliente " + id + " não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Categoria " + id + " não encontrada"));
    }

    private Chamado buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Chamado " + id + " não encontrado"));
    }
}
