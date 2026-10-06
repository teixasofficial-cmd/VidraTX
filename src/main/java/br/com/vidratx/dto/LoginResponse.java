package br.com.vidratx.dto;

import br.com.vidratx.enums.Perfil;

public class LoginResponse {

    private String token;
    private String tipo;

    private Long usuarioId;
    private Long empresaId;
    private String empresaSlug;
    private String empresaNomeFantasia;

    private String nome;
    private String email;

    private Perfil perfil;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getEmpresaSlug() {
        return empresaSlug;
    }

    public void setEmpresaSlug(String empresaSlug) {
        this.empresaSlug = empresaSlug;
    }

    public String getEmpresaNomeFantasia() {
        return empresaNomeFantasia;
    }

    public void setEmpresaNomeFantasia(String empresaNomeFantasia) {
        this.empresaNomeFantasia = empresaNomeFantasia;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

}
