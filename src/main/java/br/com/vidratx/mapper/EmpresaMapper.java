package br.com.vidratx.mapper;

import br.com.vidratx.dto.EmpresaPublicResponse;
import br.com.vidratx.dto.EmpresaRequest;
import br.com.vidratx.dto.EmpresaResponse;
import br.com.vidratx.entity.Empresa;
import org.springframework.stereotype.Component;

@Component
public class EmpresaMapper {

    public Empresa toEntity(EmpresaRequest request) {

        Empresa empresa = new Empresa();

        empresa.setRazaoSocial(
                normalizarObrigatorio(
                        request.getRazaoSocial()
                )
        );

        empresa.setNomeFantasia(
                normalizarObrigatorio(
                        request.getNomeFantasia()
                )
        );

        empresa.setCnpj(
                normalizarCnpj(
                        request.getCnpj()
                )
        );

        aplicarCamposDeMarca(empresa, request);

        return empresa;
    }

    public void updateEntity(
            Empresa empresa,
            EmpresaRequest request) {

        empresa.setRazaoSocial(
                normalizarObrigatorio(
                        request.getRazaoSocial()
                )
        );

        empresa.setNomeFantasia(
                normalizarObrigatorio(
                        request.getNomeFantasia()
                )
        );

        empresa.setCnpj(
                normalizarCnpj(
                        request.getCnpj()
                )
        );

        aplicarCamposDeMarca(empresa, request);

    }

    public EmpresaResponse toResponse(
            Empresa empresa) {

        EmpresaResponse response =
                new EmpresaResponse();

        response.setId(empresa.getId());
        response.setRazaoSocial(
                empresa.getRazaoSocial()
        );
        response.setNomeFantasia(
                empresa.getNomeFantasia()
        );
        response.setCnpj(
                empresa.getCnpj()
        );
        response.setSlug(
                empresa.getSlug()
        );
        response.setEmail(
                empresa.getEmail()
        );
        response.setTelefone(
                empresa.getTelefone()
        );
        response.setWhatsapp(
                empresa.getWhatsapp()
        );
        response.setLogoUrl(
                empresa.getLogoUrl()
        );
        response.setCorPrimaria(
                empresa.getCorPrimaria()
        );
        response.setCorSecundaria(
                empresa.getCorSecundaria()
        );
        response.setEndereco(
                empresa.getEndereco()
        );
        response.setSobre(
                empresa.getSobre()
        );
        response.setAtiva(
                empresa.getAtiva()
        );
        response.setCriadoEm(
                empresa.getCriadoEm()
        );
        response.setAtualizadoEm(
                empresa.getAtualizadoEm()
        );

        return response;
    }

    public EmpresaPublicResponse toPublicResponse(
            Empresa empresa) {

        EmpresaPublicResponse response =
                new EmpresaPublicResponse();

        response.setSlug(
                empresa.getSlug()
        );
        response.setNomeFantasia(
                empresa.getNomeFantasia()
        );
        response.setLogoUrl(
                empresa.getLogoUrl()
        );
        response.setCorPrimaria(
                empresa.getCorPrimaria()
        );
        response.setCorSecundaria(
                empresa.getCorSecundaria()
        );
        response.setWhatsapp(
                empresa.getWhatsapp()
        );
        response.setTelefone(
                empresa.getTelefone()
        );
        response.setEmail(
                empresa.getEmail()
        );
        response.setEndereco(
                empresa.getEndereco()
        );
        response.setSobre(
                empresa.getSobre()
        );

        return response;
    }

    private void aplicarCamposDeMarca(
            Empresa empresa,
            EmpresaRequest request) {

        empresa.setEmail(
                normalizar(request.getEmail())
        );

        empresa.setTelefone(
                normalizar(request.getTelefone())
        );

        empresa.setWhatsapp(
                normalizar(request.getWhatsapp())
        );

        empresa.setLogoUrl(
                normalizar(request.getLogoUrl())
        );

        empresa.setCorPrimaria(
                normalizarCor(request.getCorPrimaria())
        );

        empresa.setCorSecundaria(
                normalizarCor(request.getCorSecundaria())
        );

        empresa.setEndereco(
                normalizar(request.getEndereco())
        );

        empresa.setSobre(
                normalizar(request.getSobre())
        );
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarCor(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim().toUpperCase();
    }

    private String normalizarObrigatorio(
            String valor) {

        return valor.trim();
    }

    private String normalizarCnpj(
            String cnpj) {

        if (cnpj == null || cnpj.isBlank()) {
            return null;
        }

        return cnpj.replaceAll("\\D", "");
    }
}
