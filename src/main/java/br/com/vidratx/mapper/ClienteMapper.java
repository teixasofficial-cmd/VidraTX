package br.com.vidratx.mapper;

import br.com.vidratx.dto.ClienteRequest;
import br.com.vidratx.dto.ClienteResponse;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.util.TelefoneUtils;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {

        Cliente cliente = new Cliente();

        aplicarCampos(cliente, request);

        return cliente;
    }

    public void updateEntity(
            Cliente cliente,
            ClienteRequest request) {

        aplicarCampos(cliente, request);
    }

    public ClienteResponse toResponse(
            Cliente cliente) {

        ClienteResponse response =
                new ClienteResponse();

        response.setId(
                cliente.getId()
        );

        if (cliente.getEmpresa() != null) {

            response.setEmpresaId(
                    cliente.getEmpresa().getId()
            );
        }

        response.setNome(
                cliente.getNome()
        );

        response.setTelefone(
                cliente.getTelefone()
        );

        response.setWhatsapp(
                cliente.getWhatsapp()
        );

        response.setEmail(
                cliente.getEmail()
        );

        response.setCpfCnpj(
                cliente.getCpfCnpj()
        );

        response.setEndereco(
                cliente.getEndereco()
        );

        response.setObservacoes(
                cliente.getObservacoes()
        );

        response.setCriadoEm(
                cliente.getCriadoEm()
        );

        response.setAtualizadoEm(
                cliente.getAtualizadoEm()
        );

        return response;
    }

    private void aplicarCampos(
            Cliente cliente,
            ClienteRequest request) {

        cliente.setNome(
                request.getNome().trim()
        );

        cliente.setTelefone(
                normalizarTelefone(request.getTelefone())
        );

        cliente.setWhatsapp(
                normalizarTelefone(request.getWhatsapp())
        );

        cliente.setEmail(
                normalizarEmail(request.getEmail())
        );

        cliente.setCpfCnpj(
                normalizarDocumento(request.getCpfCnpj())
        );

        cliente.setEndereco(
                normalizar(request.getEndereco())
        );

        cliente.setObservacoes(
                normalizar(request.getObservacoes())
        );
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarTelefone(String telefone) {
        return TelefoneUtils.normalizar(telefone);
    }

    private String normalizarEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    private String normalizarDocumento(String documento) {

        if (documento == null || documento.isBlank()) {
            return null;
        }

        return documento.replaceAll("\\D", "");
    }
}
