package br.com.vidratx.dto;

public class SuperAdminResumoResponse {

    private long totalEmpresas;
    private long empresasAtivas;
    private long empresasInativas;
    private long whatsappDesconectados;

    public SuperAdminResumoResponse() {
    }

    public SuperAdminResumoResponse(
            long totalEmpresas,
            long empresasAtivas,
            long empresasInativas,
            long whatsappDesconectados) {

        this.totalEmpresas = totalEmpresas;
        this.empresasAtivas = empresasAtivas;
        this.empresasInativas = empresasInativas;
        this.whatsappDesconectados = whatsappDesconectados;
    }

    public long getTotalEmpresas() {
        return totalEmpresas;
    }

    public void setTotalEmpresas(long totalEmpresas) {
        this.totalEmpresas = totalEmpresas;
    }

    public long getEmpresasAtivas() {
        return empresasAtivas;
    }

    public void setEmpresasAtivas(long empresasAtivas) {
        this.empresasAtivas = empresasAtivas;
    }

    public long getEmpresasInativas() {
        return empresasInativas;
    }

    public void setEmpresasInativas(long empresasInativas) {
        this.empresasInativas = empresasInativas;
    }

    public long getWhatsappDesconectados() {
        return whatsappDesconectados;
    }

    public void setWhatsappDesconectados(long whatsappDesconectados) {
        this.whatsappDesconectados = whatsappDesconectados;
    }
}
