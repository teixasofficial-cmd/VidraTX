package br.com.vidratx.service;

import br.com.vidratx.conversa.InterpretadorResposta;
import br.com.vidratx.dto.ComponenteManualRequest;
import br.com.vidratx.dto.OrcamentoItemRequest;
import br.com.vidratx.dto.OrcamentoResponse;
import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.entity.Tipologia;
import br.com.vidratx.enums.CategoriaItemPreco;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.TipoComponenteCusto;
import br.com.vidratx.enums.TipoVidro;
import br.com.vidratx.event.SolicitacaoRecebidaPeloBotEvent;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import br.com.vidratx.repository.TabelaPrecoRepository;
import br.com.vidratx.repository.TipologiaRepository;
import br.com.vidratx.util.MedidaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RascunhoOrcamentoService {

    private static final Logger log = LoggerFactory.getLogger(RascunhoOrcamentoService.class);

    private static final Pattern ESPESSURA = Pattern.compile("\\b(4|5|6|8|10|12)\\s*mm\\b");

    private final SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private final TipologiaRepository tipologiaRepository;
    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final OrcamentoService orcamentoService;
    private final OrcamentoCalculoService orcamentoCalculoService;
    private final TransactionTemplate transacao;

    public RascunhoOrcamentoService(
            SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository,
            TipologiaRepository tipologiaRepository,
            TabelaPrecoRepository tabelaPrecoRepository,
            OrcamentoService orcamentoService,
            OrcamentoCalculoService orcamentoCalculoService,
            PlatformTransactionManager transactionManager) {

        this.solicitacaoOrcamentoRepository = solicitacaoOrcamentoRepository;
        this.tipologiaRepository = tipologiaRepository;
        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.orcamentoService = orcamentoService;
        this.orcamentoCalculoService = orcamentoCalculoService;
        this.transacao = new TransactionTemplate(transactionManager);
        this.transacao.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    record Pedido(String codigoTipologia, MedidaParser.MedidaParseada medida, TipoVidro tipoVidro,
                  Short espessuraMm, CorVidro cor, String chaveKit) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoReceberPedido(SolicitacaoRecebidaPeloBotEvent evento) {

        try {
            montar(evento.solicitacaoId());
        } catch (RuntimeException ex) {
            log.warn("Não foi possível montar o rascunho do orçamento da solicitação {}", evento.solicitacaoId(), ex);
        }
    }

    public void montar(Long solicitacaoId) {

        record Dados(Long empresaId, String descricao) {
        }

        Dados dados = transacao.execute(status -> solicitacaoOrcamentoRepository.findById(solicitacaoId)
                .map(s -> new Dados(s.getEmpresa().getId(), s.getDescricao()))
                .orElse(null));

        if (dados == null) {
            return;
        }

        Long empresaId = dados.empresaId();

        OrcamentoResponse orcamento = transacao.execute(status ->
                orcamentoService.criarAPartirDeSolicitacao(empresaId, solicitacaoId, null));

        if (orcamento == null) {
            return;
        }

        Optional<Pedido> pedido = entender(dados.descricao());

        if (pedido.isEmpty()) {
            return;
        }

        try {
            transacao.executeWithoutResult(status -> adicionarItem(empresaId, orcamento.getId(), pedido.get()));
        } catch (RuntimeException ex) {
            log.info("Rascunho do orçamento {} ficou sem item: {}", orcamento.getId(), ex.getMessage());
        }
    }

    private void adicionarItem(Long empresaId, Long orcamentoId, Pedido pedido) {

        Tipologia tipologia = tipologiaRepository.findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(empresaId).stream()
                .filter(t -> pedido.codigoTipologia().equals(t.getCodigo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("tipologia " + pedido.codigoTipologia() + " inativa"));

        OrcamentoItemRequest item = new OrcamentoItemRequest();

        item.setTipologiaId(tipologia.getId());
        item.setLarguraVaoMm(pedido.medida().larguraMm());
        item.setAlturaVaoMm(pedido.medida().alturaMm());
        item.setTipoVidro(pedido.tipoVidro());
        item.setEspessuraMm(pedido.espessuraMm());
        item.setCor(pedido.cor());
        item.setQuantidade(1);
        item.setObservacoes("Montado pelo WhatsApp a partir do pedido do cliente: confira medida, vidro e ferragem.");
        item.setComponentes(kitDaTabela(empresaId, pedido.chaveKit()).map(List::of).orElse(List.of()));

        var adicionado = orcamentoCalculoService.adicionarItem(empresaId, orcamentoId, item, null, false);

        if (adicionado.getLinhas() == null
                || adicionado.getLinhas().stream().noneMatch(l -> l.getTipo() == TipoComponenteCusto.VIDRO)) {
            throw new IllegalStateException("sem preço na tabela para o vidro do pedido");
        }
    }

    private Optional<ComponenteManualRequest> kitDaTabela(Long empresaId, String chaveKit) {

        if (chaveKit == null) {
            return Optional.empty();
        }

        List<TabelaPreco> kits = tabelaPrecoRepository.findAllByEmpresaIdAndAtivoTrueOrderByCategoriaAscDescricaoAsc(empresaId)
                .stream()
                .filter(t -> t.getCategoria() == CategoriaItemPreco.KIT)
                .filter(t -> InterpretadorResposta.palavras(t.getDescricao()).contains(chaveKit))
                .toList();

        if (kits.size() != 1) {
            return Optional.empty();
        }

        ComponenteManualRequest kit = new ComponenteManualRequest();

        kit.setTipo(TipoComponenteCusto.KIT);
        kit.setDescricao(kits.get(0).getDescricao());
        kit.setQuantidade(BigDecimal.ONE);
        kit.setTabelaPrecoId(kits.get(0).getId());

        return Optional.of(kit);
    }

    static Optional<Pedido> entender(String descricao) {

        if (descricao == null) {
            return Optional.empty();
        }

        String texto = " " + InterpretadorResposta.palavras(descricao) + " ";
        Optional<MedidaParser.MedidaParseada> medida = MedidaParser.tentarInterpretar(descricao);

        String codigo;
        String chaveKit = null;
        TipoVidro tipo = TipoVidro.TEMPERADO;
        short espessura = 8;

        if (texto.contains(" box ")) {

            if (texto.contains(" canto ")) {
                codigo = "box_canto_4f";
                chaveKit = "box de canto";
            } else if (texto.contains(" abrir ") || texto.contains(" pivotante ")) {
                codigo = "box_abrir_pivotante";
            } else {
                codigo = "box_frontal_2f";
                chaveKit = "box frontal";
            }

        } else if (texto.contains(" espelho ")) {
            codigo = "espelho";
            tipo = TipoVidro.ESPELHO;
            espessura = 4;
        } else if (texto.contains(" guarda corpo ")) {
            codigo = "guarda_corpo";
            tipo = TipoVidro.LAMINADO;
            espessura = 10;
        } else if (texto.contains(" sacada ")) {
            codigo = "envidracamento_sacada";
        } else if (texto.contains(" janela ")) {
            codigo = texto.contains(" 4 folhas ") ? "janela_4f" : "janela_2f";
        } else if (texto.contains(" porta ")) {
            codigo = texto.contains(" pivotante ") || texto.contains(" abrir ") ? "porta_pivotante" : "porta_correr";
            espessura = 10;
        } else if (texto.contains(" tampo ") || texto.contains(" prateleira ") || texto.contains(" mesa ")) {
            codigo = "tampo_prateleira";
        } else {
            return Optional.empty();
        }

        if (medida.isEmpty()) {
            return Optional.empty();
        }

        if (texto.contains(" laminado ")) {
            tipo = TipoVidro.LAMINADO;
        } else if (texto.contains(" temperado ") && tipo != TipoVidro.ESPELHO) {
            tipo = TipoVidro.TEMPERADO;
        }

        Matcher mm = ESPESSURA.matcher(texto);

        if (mm.find()) {
            espessura = Short.parseShort(mm.group(1));
        }

        CorVidro cor = texto.contains(" fume ") ? CorVidro.FUME
                : texto.contains(" verde ") ? CorVidro.VERDE
                : texto.contains(" bronze ") ? CorVidro.BRONZE
                : CorVidro.INCOLOR;

        return Optional.of(new Pedido(codigo, medida.get(), tipo, espessura, cor, chaveKit));
    }
}
