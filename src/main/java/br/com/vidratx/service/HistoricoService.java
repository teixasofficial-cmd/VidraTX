package br.com.vidratx.service;

import br.com.vidratx.dto.HistoricoResponse;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Historico;
import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.mapper.HistoricoMapper;
import br.com.vidratx.repository.HistoricoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final HistoricoMapper historicoMapper;

    public HistoricoService(
            HistoricoRepository historicoRepository,
            HistoricoMapper historicoMapper) {

        this.historicoRepository = historicoRepository;
        this.historicoMapper = historicoMapper;
    }

    @Transactional
    public void registrarEventoOrcamento(
            Orcamento orcamento,
            TipoEventoHistorico tipo,
            String descricao,
            Usuario responsavel) {

        Historico historico = new Historico();

        historico.setEmpresa(orcamento.getEmpresa());
        historico.setCliente(orcamento.getCliente());
        historico.setOrcamento(orcamento);

        salvar(historico, tipo, descricao, responsavel);
    }

    @Transactional
    public void registrarEventoInstalacao(
            Empresa empresa,
            Cliente cliente,
            Instalacao instalacao,
            TipoEventoHistorico tipo,
            String descricao,
            Usuario responsavel) {

        Historico historico = new Historico();

        historico.setEmpresa(empresa);
        historico.setCliente(cliente);
        historico.setInstalacao(instalacao);

        salvar(historico, tipo, descricao, responsavel);
    }

    @Transactional
    public void registrarEventoEmpresa(
            Empresa empresa,
            TipoEventoHistorico tipo,
            String descricao) {

        Historico historico = new Historico();

        historico.setEmpresa(empresa);

        salvar(historico, tipo, descricao, null);
    }

    @Transactional
    public void registrarEventoAtendimento(
            AtendimentoWhatsapp atendimento,
            TipoEventoHistorico tipo,
            String descricao,
            Usuario responsavel) {

        Historico historico = new Historico();

        historico.setEmpresa(atendimento.getEmpresa());
        historico.setCliente(atendimento.getCliente());
        historico.setAtendimento(atendimento);

        salvar(historico, tipo, descricao, responsavel);
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listarPorCliente(Long empresaId, Long clienteId) {

        return historicoRepository
                .findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(empresaId, clienteId)
                .stream()
                .map(historicoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listarPorAtendimento(Long atendimentoId) {

        return historicoRepository
                .findAllByAtendimentoIdOrderByCriadoEmAsc(atendimentoId)
                .stream()
                .map(historicoMapper::toResponse)
                .toList();
    }

    private void salvar(
            Historico historico,
            TipoEventoHistorico tipo,
            String descricao,
            Usuario responsavel) {

        historico.setUsuario(responsavel);
        historico.setTipo(tipo);
        historico.setDescricao(descricao);

        historicoRepository.save(historico);
    }
}
