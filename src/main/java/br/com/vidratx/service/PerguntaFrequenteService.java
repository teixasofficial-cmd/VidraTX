package br.com.vidratx.service;

import br.com.vidratx.conversa.InterpretadorResposta;
import br.com.vidratx.dto.PerguntaFrequenteRequest;
import br.com.vidratx.dto.PerguntaFrequenteResponse;
import br.com.vidratx.entity.PerguntaFrequente;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.PerguntaFrequenteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PerguntaFrequenteService {

    private final PerguntaFrequenteRepository perguntaFrequenteRepository;
    private final EmpresaRepository empresaRepository;

    public PerguntaFrequenteService(
            PerguntaFrequenteRepository perguntaFrequenteRepository,
            EmpresaRepository empresaRepository) {

        this.perguntaFrequenteRepository = perguntaFrequenteRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<PerguntaFrequenteResponse> listar(Long empresaId) {

        return perguntaFrequenteRepository.findAllByEmpresaIdOrderByPerguntaAsc(empresaId).stream()
                .map(PerguntaFrequenteResponse::de)
                .toList();
    }

    @Transactional
    public PerguntaFrequenteResponse criar(Long empresaId, PerguntaFrequenteRequest request) {

        PerguntaFrequente pergunta = new PerguntaFrequente();

        pergunta.setEmpresa(empresaRepository.getReferenceById(empresaId));
        aplicar(pergunta, request);

        return PerguntaFrequenteResponse.de(perguntaFrequenteRepository.save(pergunta));
    }

    @Transactional
    public PerguntaFrequenteResponse atualizar(Long empresaId, Long id, PerguntaFrequenteRequest request) {

        PerguntaFrequente pergunta = buscar(empresaId, id);

        aplicar(pergunta, request);

        return PerguntaFrequenteResponse.de(pergunta);
    }

    @Transactional
    public void excluir(Long empresaId, Long id) {
        perguntaFrequenteRepository.delete(buscar(empresaId, id));
    }

    @Transactional(readOnly = true)
    public Optional<PerguntaFrequente> encontrar(Long empresaId, String mensagem) {

        String texto = " " + InterpretadorResposta.palavras(mensagem) + " ";

        if (texto.isBlank()) {
            return Optional.empty();
        }

        return perguntaFrequenteRepository.findAllByEmpresaIdAndAtivaTrue(empresaId).stream()
                .map(p -> new Combinacao(p, chavesPresentes(p, texto)))
                .filter(c -> !c.chaves().isEmpty())
                .max(Comparator.comparingInt((Combinacao c) -> c.chaves().size())
                        .thenComparingInt(c -> c.chaves().stream().mapToInt(String::length).max().orElse(0)))
                .map(Combinacao::pergunta);
    }

    private record Combinacao(PerguntaFrequente pergunta, List<String> chaves) {
    }

    private static List<String> chavesPresentes(PerguntaFrequente pergunta, String texto) {

        return Arrays.stream(pergunta.getPalavrasChave().split("[,;\\n]"))
                .map(InterpretadorResposta::palavras)
                .filter(chave -> !chave.isBlank() && texto.contains(" " + chave + " "))
                .toList();
    }

    private PerguntaFrequente buscar(Long empresaId, Long id) {

        return perguntaFrequenteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Dúvida frequente não encontrada"));
    }

    private static void aplicar(PerguntaFrequente pergunta, PerguntaFrequenteRequest request) {

        pergunta.setPergunta(request.getPergunta().trim());
        pergunta.setPalavrasChave(request.getPalavrasChave().trim());
        pergunta.setResposta(request.getResposta().trim());
        pergunta.setAtiva(request.getAtiva() == null || request.getAtiva());
    }
}
