package br.com.vidratx.service;

import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.WhatsappContato;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.WhatsappContatoRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WhatsappContatoService {

    private final WhatsappContatoRepository whatsappContatoRepository;
    private final EmpresaRepository empresaRepository;
    private final WhatsappContatoService self;

    public WhatsappContatoService(
            WhatsappContatoRepository whatsappContatoRepository,
            EmpresaRepository empresaRepository,
            @Lazy WhatsappContatoService self) {

        this.whatsappContatoRepository = whatsappContatoRepository;
        this.empresaRepository = empresaRepository;
        this.self = self;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public WhatsappContato travar(Empresa empresa, String telefone) {

        garantirExistente(empresa.getId(), telefone);

        return whatsappContatoRepository
                .travar(empresa.getId(), telefone)
                .orElseThrow(() -> new IllegalStateException(
                        "Contato de WhatsApp não encontrado após criação: " + telefone
                ));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public WhatsappContato travarExistente(Long empresaId, String telefone) {

        return whatsappContatoRepository
                .travar(empresaId, telefone)
                .orElseGet(() -> travar(empresaRepository.getReferenceById(empresaId), telefone));
    }

    public void garantirExistente(Long empresaId, String telefone) {

        if (whatsappContatoRepository.findByEmpresaIdAndTelefone(empresaId, telefone).isPresent()) {
            return;
        }

        try {

            self.criarEmTransacaoPropria(empresaId, telefone);

        } catch (DataIntegrityViolationException ex) {

        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void criarEmTransacaoPropria(Long empresaId, String telefone) {

        if (whatsappContatoRepository.findByEmpresaIdAndTelefone(empresaId, telefone).isPresent()) {
            return;
        }

        WhatsappContato contato = new WhatsappContato();

        contato.setEmpresa(empresaRepository.getReferenceById(empresaId));
        contato.setTelefone(telefone);

        whatsappContatoRepository.saveAndFlush(contato);
    }
}
