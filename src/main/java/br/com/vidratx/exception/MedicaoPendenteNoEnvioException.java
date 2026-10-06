package br.com.vidratx.exception;

public class MedicaoPendenteNoEnvioException extends RuntimeException {

    private final String statusMedicao;
    private final String dataMedicao;

    public MedicaoPendenteNoEnvioException(String mensagem, String statusMedicao, String dataMedicao) {
        super(mensagem);
        this.statusMedicao = statusMedicao;
        this.dataMedicao = dataMedicao;
    }

    public String getStatusMedicao() {
        return statusMedicao;
    }

    public String getDataMedicao() {
        return dataMedicao;
    }
}
