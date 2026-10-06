package br.com.vidratx.service;

import br.com.vidratx.dto.ServicoRequest;
import br.com.vidratx.dto.ServicoResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Servico;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.ServicoDuplicadoException;
import br.com.vidratx.exception.ServicoNaoEncontradoException;
import br.com.vidratx.mapper.ServicoMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final EmpresaRepository empresaRepository;
    private final ServicoMapper servicoMapper;

    public ServicoService(
            ServicoRepository servicoRepository,
            EmpresaRepository empresaRepository,
            ServicoMapper servicoMapper) {

        this.servicoRepository = servicoRepository;
        this.empresaRepository = empresaRepository;
        this.servicoMapper = servicoMapper;
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listarPorEmpresa(
            Long empresaId,
            boolean apenasAtivos) {

        verificarEmpresa(empresaId);

        List<Servico> servicos =
                apenasAtivos
                        ? servicoRepository
                        .findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(
                                empresaId
                        )
                        : servicoRepository
                        .findAllByEmpresaIdOrderByNomeAsc(
                                empresaId
                        );

        return servicos
                .stream()
                .map(servicoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(
            Long empresaId,
            Long servicoId) {

        Servico servico =
                buscarServico(
                        empresaId,
                        servicoId
                );

        return servicoMapper.toResponse(servico);
    }

    @Transactional
    public ServicoResponse salvar(
            Long empresaId,
            ServicoRequest request) {

        Empresa empresa =
                buscarEmpresa(empresaId);

        String nome =
                request.getNome().trim();

        if (servicoRepository
                .existsByEmpresaIdAndNome(
                        empresaId,
                        nome
                )) {

            throw new ServicoDuplicadoException(
                    "Já existe um serviço com este nome nesta empresa"
            );
        }

        Servico servico =
                servicoMapper.toEntity(request);

        servico.setEmpresa(empresa);

        Servico salvo =
                servicoRepository.save(servico);

        return servicoMapper.toResponse(salvo);
    }

    @Transactional
    public ServicoResponse atualizar(
            Long empresaId,
            Long servicoId,
            ServicoRequest request) {

        Servico servico =
                buscarServico(
                        empresaId,
                        servicoId
                );

        String nome =
                request.getNome().trim();

        if (servicoRepository
                .existsByEmpresaIdAndNomeAndIdNot(
                        empresaId,
                        nome,
                        servicoId
                )) {

            throw new ServicoDuplicadoException(
                    "Já existe um serviço com este nome nesta empresa"
            );
        }

        servicoMapper.updateEntity(
                servico,
                request
        );

        return servicoMapper.toResponse(
                servicoRepository.save(servico)
        );
    }

    @Transactional
    public void excluir(
            Long empresaId,
            Long servicoId) {

        Servico servico =
                buscarServico(
                        empresaId,
                        servicoId
                );

        servicoRepository.delete(servico);
    }

    private Empresa buscarEmpresa(Long empresaId) {

        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() ->
                        new EmpresaNaoEncontradaException(
                                "Empresa não encontrada"
                        )
                );
    }

    private void verificarEmpresa(Long empresaId) {
        buscarEmpresa(empresaId);
    }

    private Servico buscarServico(
            Long empresaId,
            Long servicoId) {

        return servicoRepository
                .findByIdAndEmpresaId(
                        servicoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new ServicoNaoEncontradoException(
                                "Serviço não encontrado"
                        )
                );
    }
}
