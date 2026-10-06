package br.com.vidratx.service;

import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.util.TelefoneUtils;

import br.com.vidratx.dto.ClienteRequest;
import br.com.vidratx.dto.ClienteResponse;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.exception.ClienteNaoEncontradoException;
import br.com.vidratx.exception.DocumentoDuplicadoException;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.mapper.ClienteMapper;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.validator.CpfCnpjValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteMapper clienteMapper;
    private final AtendimentoWhatsappRepository atendimentoWhatsappRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            EmpresaRepository empresaRepository,
            ClienteMapper clienteMapper,
            AtendimentoWhatsappRepository atendimentoWhatsappRepository) {

        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.clienteMapper = clienteMapper;
        this.atendimentoWhatsappRepository = atendimentoWhatsappRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarPorEmpresa(
            Long empresaId,
            String nome) {

        verificarEmpresa(empresaId);

        List<Cliente> clientes =
                (nome == null || nome.isBlank())
                        ? clienteRepository
                        .findAllByEmpresaIdOrderByNomeAsc(
                                empresaId
                        )
                        : clienteRepository
                        .findAllByEmpresaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
                                empresaId,
                                nome.trim()
                        );

        return clientes
                .stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(
            Long empresaId,
            Long clienteId) {

        Cliente cliente =
                buscarCliente(
                        empresaId,
                        clienteId
                );

        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public ClienteResponse salvar(
            Long empresaId,
            ClienteRequest request) {

        Empresa empresa =
                buscarEmpresa(empresaId);

        String documento =
                normalizarDocumento(
                        request.getCpfCnpj()
                );

        validarDocumento(documento);

        if (documento != null
                && clienteRepository
                .existsByEmpresaIdAndCpfCnpj(
                        empresaId,
                        documento
                )) {

            throw new DocumentoDuplicadoException(
                    "Já existe um cliente com este CPF/CNPJ nesta empresa"
            );
        }

        Cliente cliente =
                clienteMapper.toEntity(request);

        validarWhatsapp(cliente.getWhatsapp());

        cliente.setEmpresa(empresa);

        Cliente salvo =
                clienteRepository.save(cliente);

        vincularConversasSemCliente(salvo);

        return clienteMapper.toResponse(salvo);
    }

    @Transactional
    public ClienteResponse atualizar(
            Long empresaId,
            Long clienteId,
            ClienteRequest request) {

        Cliente cliente =
                buscarCliente(
                        empresaId,
                        clienteId
                );

        String documento =
                normalizarDocumento(
                        request.getCpfCnpj()
                );

        validarDocumento(documento);

        if (documento != null
                && clienteRepository
                .existsByEmpresaIdAndCpfCnpjAndIdNot(
                        empresaId,
                        documento,
                        clienteId
                )) {

            throw new DocumentoDuplicadoException(
                    "Já existe um cliente com este CPF/CNPJ nesta empresa"
            );
        }

        clienteMapper.updateEntity(
                cliente,
                request
        );

        validarWhatsapp(cliente.getWhatsapp());

        Cliente salvo = clienteRepository.save(cliente);

        vincularConversasSemCliente(salvo);

        return clienteMapper.toResponse(salvo);
    }

    @Transactional
    public void excluir(
            Long empresaId,
            Long clienteId) {

        Cliente cliente =
                buscarCliente(
                        empresaId,
                        clienteId
                );

        clienteRepository.delete(cliente);
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

    private Cliente buscarCliente(
            Long empresaId,
            Long clienteId) {

        return clienteRepository
                .findByIdAndEmpresaId(
                        clienteId,
                        empresaId
                )
                .orElseThrow(() ->
                        new ClienteNaoEncontradoException(
                                "Cliente não encontrado"
                        )
                );
    }

    private String normalizarDocumento(String documento) {

        if (documento == null || documento.isBlank()) {
            return null;
        }

        return documento.replaceAll("\\D", "");
    }

    private void validarWhatsapp(String whatsapp) {

        if (whatsapp != null && !TelefoneUtils.pareceValido(whatsapp)) {

            throw new IllegalArgumentException(
                    "WhatsApp inválido: informe DDD + número, ex.: (11) 98888-7777"
            );
        }
    }

    private void vincularConversasSemCliente(Cliente cliente) {

        if (cliente.getWhatsapp() == null) {
            return;
        }

        List<String> variantes = TelefoneUtils.variantes(cliente.getWhatsapp());

        if (clienteRepository.findAllByEmpresaIdAndWhatsappInOrderByIdAsc(
                cliente.getEmpresa().getId(), variantes).size() > 1) {
            return;
        }

        for (AtendimentoWhatsapp atendimento
                : atendimentoWhatsappRepository.findAllByEmpresaIdAndTelefoneInAndClienteIsNullAndStatusNot(
                        cliente.getEmpresa().getId(), variantes, StatusAtendimento.ENCERRADO)) {

            atendimento.setCliente(cliente);
            atendimentoWhatsappRepository.save(atendimento);
        }
    }

    private void validarDocumento(String documento) {

        if (documento != null
                && !CpfCnpjValidator.isValid(documento)) {

            throw new IllegalArgumentException(
                    "CPF/CNPJ inválido"
            );
        }
    }
}
