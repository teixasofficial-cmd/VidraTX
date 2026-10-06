package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.dto.AgendaEmpresaDto;
import br.com.vidratx.dto.EmpresaPublicResponse;
import br.com.vidratx.dto.EmpresaRequest;
import br.com.vidratx.dto.EmpresaResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.exception.CnpjDuplicadoException;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.mapper.EmpresaMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.validator.CnpjValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            EmpresaMapper empresaMapper) {

        this.empresaRepository = empresaRepository;
        this.empresaMapper = empresaMapper;
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(Long id) {

        Empresa empresa =
                buscarEmpresa(id);

        return empresaMapper.toResponse(
                empresa
        );
    }

    @Transactional(readOnly = true)
    public EmpresaPublicResponse buscarPublicaPorSlug(String slug) {

        String slugNormalizado =
                normalizarSlug(slug);

        Empresa empresa =
                empresaRepository
                        .findBySlug(slugNormalizado)
                        .filter(e ->
                                Boolean.TRUE.equals(e.getAtiva())
                        )
                        .orElseThrow(() ->
                                new EmpresaNaoEncontradaException(
                                        "Empresa não encontrada"
                                )
                        );

        return empresaMapper.toPublicResponse(empresa);
    }

    @Transactional
    public EmpresaResponse atualizar(
            Long id,
            EmpresaRequest request) {

        Empresa empresa =
                buscarEmpresa(id);

        String cnpj =
                normalizarCnpj(
                        request.getCnpj()
                );

        validarCnpj(cnpj);

        if (empresaRepository
                .existsByCnpjAndIdNot(
                        cnpj,
                        id
                )) {

            throw new CnpjDuplicadoException(
                    "CNPJ já cadastrado"
            );
        }

        empresaMapper.updateEntity(
                empresa,
                request
        );

        return empresaMapper.toResponse(
                empresaRepository.save(empresa)
        );
    }

    @Transactional(readOnly = true)
    public AgendaEmpresaDto buscarAgenda(Long id) {
        return agendaDe(buscarEmpresa(id));
    }

    @Transactional
    public AgendaEmpresaDto atualizarAgenda(Long id, AgendaEmpresaDto request) {

        if (request.getHoraInicioMensagens() >= request.getHoraFimMensagens()) {
            throw new IllegalArgumentException(
                    "O horário de mensagens automáticas precisa começar antes de terminar"
            );
        }

        Empresa empresa = buscarEmpresa(id);

        empresa.setDuracaoMedicaoMinutos(request.getDuracaoMedicaoMinutos());
        empresa.setDuracaoInstalacaoMinutos(request.getDuracaoInstalacaoMinutos());
        empresa.setMedicoesSimultaneas(request.getMedicoesSimultaneas());
        empresa.setHoraInicioMensagens(request.getHoraInicioMensagens());
        empresa.setHoraFimMensagens(request.getHoraFimMensagens());

        if (request.getFusoHorario() != null) {
            empresa.setFusoHorario(FusoEmpresa.validar(request.getFusoHorario()));
        }

        if (request.getAntecedenciaMinimaMinutos() != null) {
            empresa.setAntecedenciaMinimaMinutos(request.getAntecedenciaMinimaMinutos());
        }

        if (request.getPrazoRespostaAtendenteMinutos() != null) {
            empresa.setPrazoRespostaAtendenteMinutos(request.getPrazoRespostaAtendenteMinutos());
        }

        if (request.getMensagemForaHorario() != null) {
            empresa.setMensagemForaHorario(request.getMensagemForaHorario().isBlank()
                    ? null : request.getMensagemForaHorario().trim());
        }

        if (request.getCondicoesPagamento() != null) {
            empresa.setCondicoesPagamento(request.getCondicoesPagamento().isBlank()
                    ? null : request.getCondicoesPagamento().trim());
        }

        if (request.getLembreteOrcamentoAtivo() != null) {
            empresa.setLembreteOrcamentoAtivo(request.getLembreteOrcamentoAtivo());
        }

        return agendaDe(empresaRepository.save(empresa));
    }

    private AgendaEmpresaDto agendaDe(Empresa empresa) {

        AgendaEmpresaDto dto = new AgendaEmpresaDto();

        dto.setDuracaoMedicaoMinutos(empresa.getDuracaoMedicaoMinutos());
        dto.setDuracaoInstalacaoMinutos(empresa.getDuracaoInstalacaoMinutos());
        dto.setMedicoesSimultaneas(empresa.getMedicoesSimultaneas());
        dto.setHoraInicioMensagens(empresa.getHoraInicioMensagens());
        dto.setHoraFimMensagens(empresa.getHoraFimMensagens());
        dto.setFusoHorario(empresa.getFusoHorario() != null ? empresa.getFusoHorario() : "");
        dto.setAntecedenciaMinimaMinutos(empresa.getAntecedenciaMinimaMinutos());
        dto.setPrazoRespostaAtendenteMinutos(empresa.getPrazoRespostaAtendenteMinutos());
        dto.setMensagemForaHorario(empresa.getMensagemForaHorario() != null ? empresa.getMensagemForaHorario() : "");
        dto.setCondicoesPagamento(empresa.getCondicoesPagamento() != null ? empresa.getCondicoesPagamento() : "");
        dto.setLembreteOrcamentoAtivo(!Boolean.FALSE.equals(empresa.getLembreteOrcamentoAtivo()));

        return dto;
    }

    private Empresa buscarEmpresa(Long id) {

        return empresaRepository
                .findById(id)
                .orElseThrow(() ->
                        new EmpresaNaoEncontradaException(
                                "Empresa não encontrada"
                        )
                );
    }

    private String normalizarCnpj(
            String cnpj) {

        if (cnpj == null) {
            return null;
        }

        return cnpj.replaceAll(
                "\\D",
                ""
        );
    }

    private String normalizarSlug(String slug) {

        if (slug == null) {
            return null;
        }

        return slug.trim().toLowerCase();
    }

    private void validarCnpj(
            String cnpj) {

        if (!CnpjValidator.isValid(cnpj)) {

            throw new IllegalArgumentException(
                    "CNPJ inválido"
            );
        }
    }
}
