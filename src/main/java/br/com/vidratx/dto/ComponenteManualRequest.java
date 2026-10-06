package br.com.vidratx.dto;

import br.com.vidratx.enums.TipoComponenteCusto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ComponenteManualRequest {

    @NotNull(message = "Tipo do componente é obrigatório")
    private TipoComponenteCusto tipo;

    @Size(min = 1, max = 200, message = "Descrição deve ter entre 1 e 200 caracteres")
    private String descricao;

    @NotNull(message = "Quantidade é obrigatória")
    @Positive(message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    private Long tabelaPrecoId;

    @DecimalMin(value = "0.00", message = "Valor unitário não pode ser negativo")
    private BigDecimal valorUnitario;

    public TipoComponenteCusto getTipo() {
        return tipo;
    }

    public void setTipo(TipoComponenteCusto tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public Long getTabelaPrecoId() {
        return tabelaPrecoId;
    }

    public void setTabelaPrecoId(Long tabelaPrecoId) {
        this.tabelaPrecoId = tabelaPrecoId;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
}
