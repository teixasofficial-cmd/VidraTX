package br.com.vidratx.dto;

import br.com.vidratx.enums.CategoriaTipologia;

import java.time.LocalDateTime;
import java.util.List;

public class TipologiaResponse {

    private Long id;
    private Long empresaId;
    private String codigo;
    private String nome;
    private CategoriaTipologia categoria;
    private int numeroFolhas;
    private String formulaPecas;
    private int descontoLarguraMm;
    private int descontoAlturaMm;
    private int transpasseMm;
    private List<String> alertasNormativos;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaTipologia getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTipologia categoria) {
        this.categoria = categoria;
    }

    public int getNumeroFolhas() {
        return numeroFolhas;
    }

    public void setNumeroFolhas(int numeroFolhas) {
        this.numeroFolhas = numeroFolhas;
    }

    public String getFormulaPecas() {
        return formulaPecas;
    }

    public void setFormulaPecas(String formulaPecas) {
        this.formulaPecas = formulaPecas;
    }

    public int getDescontoLarguraMm() {
        return descontoLarguraMm;
    }

    public void setDescontoLarguraMm(int descontoLarguraMm) {
        this.descontoLarguraMm = descontoLarguraMm;
    }

    public int getDescontoAlturaMm() {
        return descontoAlturaMm;
    }

    public void setDescontoAlturaMm(int descontoAlturaMm) {
        this.descontoAlturaMm = descontoAlturaMm;
    }

    public int getTranspasseMm() {
        return transpasseMm;
    }

    public void setTranspasseMm(int transpasseMm) {
        this.transpasseMm = transpasseMm;
    }

    public List<String> getAlertasNormativos() {
        return alertasNormativos;
    }

    public void setAlertasNormativos(List<String> alertasNormativos) {
        this.alertasNormativos = alertasNormativos;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
