package br.com.vidratx.dto;

import br.com.vidratx.enums.TipoEventoHistorico;

import java.time.LocalDateTime;

public class HistoricoResponse {

    private Long id;
    private TipoEventoHistorico tipo;
    private String descricao;
    private Long usuarioId;
    private String usuarioNome;
    private LocalDateTime criadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoEventoHistorico getTipo() {
        return tipo;
    }

    public void setTipo(TipoEventoHistorico tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
