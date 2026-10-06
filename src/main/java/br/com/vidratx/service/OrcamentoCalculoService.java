package br.com.vidratx.service;

import br.com.vidratx.calculo.ComponenteAdicional;
import br.com.vidratx.calculo.FormulaPecas;
import br.com.vidratx.calculo.ItemCalculoInput;
import br.com.vidratx.calculo.LinhaCalculada;
import br.com.vidratx.calculo.MotorCalculoOrcamento;
import br.com.vidratx.calculo.ParametrosCalculoInput;
import br.com.vidratx.calculo.PecaCalculada;
import br.com.vidratx.calculo.ResultadoItemCalculo;
import br.com.vidratx.calculo.TipologiaRegras;
import br.com.vidratx.dto.AlertaCalculoResponse;
import br.com.vidratx.dto.AjustePrecoFinalRequest;
import br.com.vidratx.dto.ComponenteManualRequest;
import br.com.vidratx.dto.OrcamentoItemRequest;
import br.com.vidratx.dto.OrcamentoItemResponse;
import br.com.vidratx.dto.OrcamentoTotaisResponse;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrcamentoItem;
import br.com.vidratx.entity.OrcamentoLinha;
import br.com.vidratx.entity.OrcamentoPeca;
import br.com.vidratx.entity.ParametroCalculo;
import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.entity.Tipologia;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.OrigemSugestao;
import br.com.vidratx.enums.RegraDeslocamento;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoComponenteCusto;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.event.ParametrosCalculoAlteradosEvent;
import br.com.vidratx.exception.OrcamentoItemNaoEncontradoException;
import br.com.vidratx.exception.OrcamentoNaoEditavelException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.exception.TabelaPrecoNaoEncontradaException;
import br.com.vidratx.exception.TipologiaNaoEncontradaException;
import br.com.vidratx.mapper.OrcamentoItemMapper;
import br.com.vidratx.mapper.TipologiaMapper;
import br.com.vidratx.repository.OrcamentoItemRepository;
import br.com.vidratx.repository.OrcamentoLinhaRepository;
import br.com.vidratx.repository.OrcamentoPecaRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.TabelaPrecoRepository;
import br.com.vidratx.repository.TipologiaRepository;
import br.com.vidratx.util.DinheiroUtils;
import br.com.vidratx.util.MedidaParser;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class OrcamentoCalculoService {

    private static final Set<StatusOrcamento> STATUS_EDITAVEL = EnumSet.of(
            StatusOrcamento.NOVO_CONTATO, StatusOrcamento.PRE_ORCAMENTO,
            StatusOrcamento.VISITA_AGENDADA, StatusOrcamento.MEDIDO, StatusOrcamento.ORCAMENTO_FINAL
    );

    public static final String ALERTA_VIDRO_SEM_PRECO = "VIDRO_SEM_PRECO";
    public static final String ALERTA_VIDRO_NAO_INFORMADO = "VIDRO_NAO_INFORMADO";
    public static final String ALERTA_MEDIDAS_AUSENTES = "MEDIDAS_AUSENTES";
    public static final String ALERTA_AJUSTE_DESCARTADO = "AJUSTE_DESCARTADO";
    public static final String ALERTA_PRECO_MANUAL = "PRECO_MANUAL_DIVERGENTE";

    public static final Set<String> ALERTAS_PENDENCIA_ITEM = Set.of(
            ALERTA_VIDRO_SEM_PRECO, ALERTA_VIDRO_NAO_INFORMADO, ALERTA_MEDIDAS_AUSENTES
    );

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoItemRepository orcamentoItemRepository;
    private final OrcamentoPecaRepository orcamentoPecaRepository;
    private final OrcamentoLinhaRepository orcamentoLinhaRepository;
    private final TipologiaRepository tipologiaRepository;
    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final TipologiaMapper tipologiaMapper;
    private final OrcamentoItemMapper orcamentoItemMapper;
    private final ParametroCalculoService parametroCalculoService;
    private final HistoricoService historicoService;
    private final Clock clock;

    public OrcamentoCalculoService(
            OrcamentoRepository orcamentoRepository,
            OrcamentoItemRepository orcamentoItemRepository,
            OrcamentoPecaRepository orcamentoPecaRepository,
            OrcamentoLinhaRepository orcamentoLinhaRepository,
            TipologiaRepository tipologiaRepository,
            TabelaPrecoRepository tabelaPrecoRepository,
            TipologiaMapper tipologiaMapper,
            OrcamentoItemMapper orcamentoItemMapper,
            ParametroCalculoService parametroCalculoService,
            HistoricoService historicoService,
            Clock clock) {

        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoItemRepository = orcamentoItemRepository;
        this.orcamentoPecaRepository = orcamentoPecaRepository;
        this.orcamentoLinhaRepository = orcamentoLinhaRepository;
        this.tipologiaRepository = tipologiaRepository;
        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.tipologiaMapper = tipologiaMapper;
        this.orcamentoItemMapper = orcamentoItemMapper;
        this.parametroCalculoService = parametroCalculoService;
        this.historicoService = historicoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoItemResponse> listarItens(Long empresaId, Long orcamentoId) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        return orcamentoItemRepository.findAllByOrcamentoIdOrderByOrdemAsc(orcamento.getId())
                .stream()
                .map(item -> montarResponseSemRecalcular(empresaId, item))
                .toList();
    }

    @Transactional
    public OrcamentoItemResponse adicionarItem(
            Long empresaId, Long orcamentoId, OrcamentoItemRequest request,
            Usuario responsavel, boolean podeDefinirPrecoLivre) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        exigirPermissaoPrecoLivre(request.getComponentes(), List.of(), podeDefinirPrecoLivre);

        Tipologia tipologia = buscarTipologia(empresaId, request.getTipologiaId());

        OrcamentoItem item = new OrcamentoItem();
        item.setOrcamento(orcamento);
        item.setTipologia(tipologia);
        item.setOrdem((int) orcamentoItemRepository.countByOrcamentoId(orcamento.getId()));

        aplicarCamposBasicos(item, request);

        OrcamentoItem salvo = orcamentoItemRepository.save(item);

        OrcamentoItemResponse response = recalcularItem(empresaId, salvo, request.getComponentes(), responsavel);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_ITEM_ADICIONADO,
                "Item adicionado: " + tipologia.getNome(), responsavel
        );

        recalcularTotaisOrcamento(orcamento);

        return response;
    }

    @Transactional
    public OrcamentoItemResponse atualizarItem(
            Long empresaId, Long orcamentoId, Long itemId, OrcamentoItemRequest request,
            Usuario responsavel, boolean podeDefinirPrecoLivre) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        OrcamentoItem item = buscarItem(orcamento.getId(), itemId);

        exigirPermissaoPrecoLivre(
                request.getComponentes(),
                orcamentoLinhaRepository.findAllByOrcamentoItemIdOrderByIdAsc(item.getId()),
                podeDefinirPrecoLivre
        );

        if (!request.getTipologiaId().equals(item.getTipologia().getId())) {
            item.setTipologia(buscarTipologia(empresaId, request.getTipologiaId()));
        }

        aplicarCamposBasicos(item, request);

        OrcamentoItem salvo = orcamentoItemRepository.save(item);

        OrcamentoItemResponse response = recalcularItem(empresaId, salvo, request.getComponentes(), responsavel);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_LINHA_EDITADA,
                "Item atualizado: " + salvo.getTipologia().getNome(), responsavel
        );

        recalcularTotaisOrcamento(orcamento);

        return response;
    }

    @Transactional
    public void removerItem(Long empresaId, Long orcamentoId, Long itemId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        OrcamentoItem item = buscarItem(orcamento.getId(), itemId);

        orcamentoLinhaRepository.deleteAllByOrcamentoItemId(item.getId());
        orcamentoPecaRepository.deleteAllByOrcamentoItemId(item.getId());
        orcamentoItemRepository.delete(item);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_ITEM_REMOVIDO,
                "Item removido: " + item.getTipologia().getNome(), responsavel
        );

        recalcularTotaisOrcamento(orcamento);
    }

    @Transactional
    public OrcamentoTotaisResponse ajustarLinha(
            Long empresaId, Long orcamentoId, Long itemId, Long linhaId, BigDecimal valorFinal, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        OrcamentoItem item = buscarItem(orcamento.getId(), itemId);

        OrcamentoLinha linha = orcamentoLinhaRepository.findByIdAndOrcamentoItemId(linhaId, item.getId())
                .orElseThrow(() -> new OrcamentoItemNaoEncontradoException("Linha de custo não encontrada"));

        if (valorFinal == null) {

            linha.setValorFinalCentavos(null);
            linha.setEditado(false);
            linha.setEditadoPor(null);
            linha.setEditadoEm(null);

        } else {

            long valorFinalCentavos = DinheiroUtils.paraCentavos(valorFinal);

            linha.setValorFinalCentavos(valorFinalCentavos);
            linha.setEditado(valorFinalCentavos != linha.getValorSugeridoCentavos());
            linha.setEditadoPor(responsavel);
            linha.setEditadoEm(LocalDateTime.now(clock));
        }

        orcamentoLinhaRepository.save(linha);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_LINHA_EDITADA,
                "Valor da linha \"" + linha.getDescricao() + "\" revisado"
                        + (valorFinal != null ? " para " + valorFinal : " (voltou ao sugerido)"),
                responsavel
        );

        return recalcularTotaisOrcamento(orcamento);
    }

    @Transactional
    public OrcamentoTotaisResponse aceitarTodasSugestoes(Long empresaId, Long orcamentoId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        List<OrcamentoLinha> linhas = orcamentoLinhaRepository
                .findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(orcamento.getId());

        for (OrcamentoLinha linha : linhas) {

            if (linha.getValorFinalCentavos() == null) {

                linha.setValorFinalCentavos(linha.getValorSugeridoCentavos());
                linha.setEditadoPor(responsavel);
                linha.setEditadoEm(LocalDateTime.now(clock));
                orcamentoLinhaRepository.save(linha);
            }
        }

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_LINHA_EDITADA,
                "Todas as sugestões de valor foram revisadas e aceitas", responsavel
        );

        return recalcularTotaisOrcamento(orcamento);
    }

    @Transactional
    public OrcamentoTotaisResponse ajustarPrecoFinal(
            Long empresaId, Long orcamentoId, AjustePrecoFinalRequest request, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        if (request.getValorFinal() == null) {

            orcamento.setPrecoFinalManualCentavos(null);

            historicoService.registrarEventoOrcamento(
                    orcamento, TipoEventoHistorico.ORCAMENTO_ALTERADO,
                    "Preço final manual removido — volta a valer o preço sugerido", responsavel
            );

        } else {

            orcamento.setPrecoFinalManualCentavos(DinheiroUtils.paraCentavos(request.getValorFinal()));

            historicoService.registrarEventoOrcamento(
                    orcamento, TipoEventoHistorico.ORCAMENTO_ALTERADO,
                    "Preço final fixado manualmente em " + request.getValorFinal(), responsavel
            );
        }

        return recalcularTotaisOrcamento(orcamento);
    }

    @Transactional
    public OrcamentoTotaisResponse definirParcelas(
            Long empresaId, Long orcamentoId, Integer parcelas, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);
        garantirEditavel(orcamento);

        ParametroCalculo parametro = parametroCalculoService.buscarOuCriarPadrao(empresaId);

        if (!parametroCalculoService.taxaConfigurada(parametro, parcelas)) {
            throw new IllegalArgumentException(
                    "Não há taxa de cartão configurada para " + parcelas
                            + "x nos Parâmetros de Cálculo. Configure a taxa antes de oferecer esse parcelamento."
            );
        }

        orcamento.setParcelasCartao(parcelas);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_ALTERADO,
                parcelas == null
                        ? "Condição de pagamento: sem parcelamento no cartão"
                        : "Condição de pagamento: " + parcelas + "x no cartão",
                responsavel
        );

        return recalcularTotaisOrcamento(orcamento);
    }

    @Transactional(readOnly = true)
    public OrcamentoTotaisResponse buscarTotais(Long empresaId, Long orcamentoId) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        if (!STATUS_EDITAVEL.contains(orcamento.getStatus())) {
            return totaisCongelados(orcamento);
        }

        return calcularTotais(orcamento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public OrcamentoTotaisResponse prepararEnvio(Orcamento orcamento) {
        return recalcularTotaisOrcamento(orcamento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public OrcamentoTotaisResponse recalcular(Orcamento orcamento) {
        return recalcularTotaisOrcamento(orcamento);
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void aoAlterarParametros(ParametrosCalculoAlteradosEvent evento) {
        recalcularOrcamentosEditaveis(evento.empresaId());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recalcularOrcamentosEditaveis(Long empresaId) {

        for (Orcamento orcamento : orcamentoRepository.findAllByEmpresaIdAndStatusIn(empresaId, STATUS_EDITAVEL)) {
            recalcularTotaisOrcamento(orcamento);
        }
    }

    private OrcamentoTotaisResponse totaisCongelados(Orcamento orcamento) {

        long naoRevisados = orcamentoLinhaRepository
                .findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(orcamento.getId())
                .stream()
                .filter(l -> l.getValorFinalCentavos() == null)
                .count();

        OrcamentoTotaisResponse response = new OrcamentoTotaisResponse();

        response.setCustoTotal(DinheiroUtils.paraReais(orcamento.getCustoTotalCentavos()));
        response.setPrecoSugerido(DinheiroUtils.paraReais(orcamento.getPrecoSugeridoCentavos()));
        response.setAjusteComercial(DinheiroUtils.paraReais(orcamento.getAjusteComercialCentavos()));
        response.setValorFinal(DinheiroUtils.paraReais(orcamento.getValorTotalCentavos()));
        response.setMargemReal(orcamento.getMargemRealPercentual());
        response.setCustoRealTotal(
                orcamento.getCustoRealTotalCentavos() != null
                        ? DinheiroUtils.paraReais(orcamento.getCustoRealTotalCentavos())
                        : null
        );
        response.setMargemSobreCustoReal(orcamento.getMargemSobreCustoRealPercentual());
        response.setCustoRealIncompleto(Boolean.TRUE.equals(orcamento.getCustoRealIncompleto()));
        response.setValoresNaoRevisados((int) naoRevisados);
        response.setAlertas(new ArrayList<>());
        response.setPrecoFinalManual(orcamento.getPrecoFinalManualCentavos() != null
                ? DinheiroUtils.paraReais(orcamento.getPrecoFinalManualCentavos()) : null);
        response.setParcelasCartao(orcamento.getParcelasCartao());

        return response;
    }

    private OrcamentoItemResponse recalcularItem(
            Long empresaId, OrcamentoItem item, List<ComponenteManualRequest> componentesRequest, Usuario responsavel) {

        ItemCalculoInput input = construirInput(empresaId, item, componentesRequest);

        ResultadoItemCalculo resultado = MotorCalculoOrcamento.calcularItem(
                input, parametroCalculoService.paraMotor(
                        parametroCalculoService.buscarOuCriarPadrao(empresaId), item.getOrcamento().getParcelasCartao()
                )
        );

        Map<String, OrcamentoLinha> anteriores = new HashMap<>();

        for (OrcamentoLinha anterior : orcamentoLinhaRepository.findAllByOrcamentoItemIdOrderByIdAsc(item.getId())) {
            anteriores.putIfAbsent(chaveLinha(anterior.getTipoComponente(), descricaoBase(anterior), anterior.getTabelaPreco() != null
                    ? anterior.getTabelaPreco().getId() : null), anterior);
        }

        orcamentoLinhaRepository.deleteAllByOrcamentoItemId(item.getId());
        orcamentoPecaRepository.deleteAllByOrcamentoItemId(item.getId());
        orcamentoLinhaRepository.flush();

        for (PecaCalculada pecaCalculada : resultado.pecas()) {

            OrcamentoPeca peca = new OrcamentoPeca();
            peca.setOrcamentoItem(item);
            peca.setDescricao(pecaCalculada.descricao());
            peca.setLarguraCorteMm(pecaCalculada.larguraCorteMm());
            peca.setAlturaCorteMm(pecaCalculada.alturaCorteMm());
            peca.setQuantidade(pecaCalculada.quantidade());
            peca.setExcedeTamanhoMaximo(pecaCalculada.excedeTamanhoMaximo());

            orcamentoPecaRepository.save(peca);
        }

        List<Boolean> autoAceitarPorComponente = new ArrayList<>();

        if (componentesRequest != null) {

            for (ComponenteManualRequest componente : componentesRequest) {
                autoAceitarPorComponente.add(componente.getTabelaPrecoId() == null);
            }
        }

        List<AlertaCalculoResponse> alertasExtras = new ArrayList<>();
        int indiceComponente = 0;

        for (LinhaCalculada linhaCalculada : resultado.linhas()) {

            OrcamentoLinha linha = new OrcamentoLinha();
            linha.setOrcamentoItem(item);
            linha.setTipoComponente(linhaCalculada.tipo());
            linha.setDescricao(linhaCalculada.descricao());
            linha.setComponenteDescricao(linhaCalculada.descricaoComponente());
            linha.setComponenteQuantidade(linhaCalculada.quantidadeComponente());
            linha.setQuantidade(linhaCalculada.quantidade());
            linha.setValorUnitarioCentavos(linhaCalculada.valorUnitarioCentavos());
            linha.setValorSugeridoCentavos(linhaCalculada.valorSugeridoCentavos());
            linha.setOrigemSugestao(linhaCalculada.origemSugestao());

            if (linhaCalculada.tabelaPrecoId() != null) {
                linha.setTabelaPreco(tabelaPrecoRepository.getReferenceById(linhaCalculada.tabelaPrecoId()));
            }

            boolean ehComponenteManual = linhaCalculada.tipo() != TipoComponenteCusto.VIDRO
                    && linhaCalculada.tipo() != TipoComponenteCusto.PERDAS;

            if (ehComponenteManual) {

                boolean autoAceitar = indiceComponente < autoAceitarPorComponente.size()
                        && autoAceitarPorComponente.get(indiceComponente);

                if (autoAceitar) {
                    linha.setValorFinalCentavos(linhaCalculada.valorSugeridoCentavos());
                    linha.setEditadoPor(responsavel);
                    linha.setEditadoEm(LocalDateTime.now(clock));
                }

                indiceComponente++;
            }

            OrcamentoLinha anterior = anteriores.get(chaveLinha(
                    linhaCalculada.tipo(),
                    linhaCalculada.descricaoComponente() != null ? linhaCalculada.descricaoComponente() : linhaCalculada.descricao(),
                    linhaCalculada.tabelaPrecoId()
            ));

            if (anterior != null && anterior.getValorFinalCentavos() != null && linha.getValorFinalCentavos() == null) {

                if (Objects.equals(anterior.getValorSugeridoCentavos(), linhaCalculada.valorSugeridoCentavos())) {

                    linha.setValorFinalCentavos(anterior.getValorFinalCentavos());
                    linha.setEditado(anterior.getEditado());
                    linha.setEditadoPor(anterior.getEditadoPor());
                    linha.setEditadoEm(anterior.getEditadoEm());

                } else if (Boolean.TRUE.equals(anterior.getEditado())) {

                    alertasExtras.add(new AlertaCalculoResponse(
                            ALERTA_AJUSTE_DESCARTADO,
                            "O valor ajustado à mão da linha \"" + linhaCalculada.descricao() + "\" ("
                                    + DinheiroUtils.paraReais(anterior.getValorFinalCentavos())
                                    + ") foi descartado porque o valor sugerido mudou de "
                                    + DinheiroUtils.paraReais(anterior.getValorSugeridoCentavos()) + " para "
                                    + DinheiroUtils.paraReais(linhaCalculada.valorSugeridoCentavos())
                                    + ". Revise a linha."
                    ));
                }
            }

            orcamentoLinhaRepository.save(linha);
        }

        List<OrcamentoPeca> pecasSalvas = orcamentoPecaRepository.findAllByOrcamentoItemIdOrderByIdAsc(item.getId());
        List<OrcamentoLinha> linhasSalvas = orcamentoLinhaRepository.findAllByOrcamentoItemIdOrderByIdAsc(item.getId());

        List<br.com.vidratx.calculo.AlertaCalculo> alertasMotor = new ArrayList<>(resultado.alertas());

        OrcamentoItemResponse response = orcamentoItemMapper.toResponse(
                item, pecasSalvas, linhasSalvas, resultado.custoItemCentavos(), alertasMotor
        );

        List<AlertaCalculoResponse> alertas = new ArrayList<>(response.getAlertas() != null ? response.getAlertas() : List.of());
        alertas.addAll(pendenciasDoItem(item, linhasSalvas));
        alertas.addAll(alertasExtras);
        response.setAlertas(alertas);

        return response;
    }

    private OrcamentoItemResponse montarResponseSemRecalcular(Long empresaId, OrcamentoItem item) {

        List<OrcamentoPeca> pecas = orcamentoPecaRepository.findAllByOrcamentoItemIdOrderByIdAsc(item.getId());
        List<OrcamentoLinha> linhas = orcamentoLinhaRepository.findAllByOrcamentoItemIdOrderByIdAsc(item.getId());

        long custoItemCentavos = linhas.stream().mapToLong(OrcamentoLinha::getValorExibidoCentavos).sum();

        OrcamentoItemResponse response = orcamentoItemMapper.toResponse(item, pecas, linhas, custoItemCentavos, List.of());

        response.setAlertas(new ArrayList<>(pendenciasDoItem(item, linhas)));

        return response;
    }

    private List<AlertaCalculoResponse> pendenciasDoItem(OrcamentoItem item, List<OrcamentoLinha> linhas) {

        List<AlertaCalculoResponse> pendencias = new ArrayList<>();

        TipologiaRegras regras = tipologiaMapper.paraRegrasCalculo(item.getTipologia());

        if (regras.formulaPecas() == FormulaPecas.SEM_PECAS) {
            return pendencias;
        }

        String nome = item.getTipologia().getNome()
                + (item.getAmbiente() != null ? " (" + item.getAmbiente() + ")" : "");

        if (item.getLarguraVaoMm() == null || item.getAlturaVaoMm() == null) {

            pendencias.add(new AlertaCalculoResponse(
                    ALERTA_MEDIDAS_AUSENTES,
                    "Item \"" + nome + "\": informe as medidas do vão — sem elas o vidro não entra no valor."
            ));

            return pendencias;
        }

        if (item.getTipoVidro() == null || item.getEspessuraMm() == null || item.getCor() == null) {

            pendencias.add(new AlertaCalculoResponse(
                    ALERTA_VIDRO_NAO_INFORMADO,
                    "Item \"" + nome + "\": informe tipo, espessura e cor do vidro — sem isso o vidro não entra no valor."
            ));

            return pendencias;
        }

        boolean temLinhaVidro = linhas.stream().anyMatch(l -> l.getTipoComponente() == TipoComponenteCusto.VIDRO);

        if (!temLinhaVidro) {

            pendencias.add(new AlertaCalculoResponse(
                    ALERTA_VIDRO_SEM_PRECO,
                    "Item \"" + nome + "\": não há preço na Tabela de Preços para vidro "
                            + item.getTipoVidro() + " " + item.getEspessuraMm() + "mm " + item.getCor()
                            + (item.getAcabamento() != null ? " " + item.getAcabamento() : "")
                            + " — o vidro ficou fora do valor. Cadastre o preço e edite o item."
            ));
        }

        return pendencias;
    }

    private ItemCalculoInput construirInput(
            Long empresaId, OrcamentoItem item, List<ComponenteManualRequest> componentesRequest) {

        TipologiaRegras regras = tipologiaMapper.paraRegrasCalculo(item.getTipologia());

        Long precoVidroCentavos = null;
        Long custoVidroCentavos = null;

        TabelaPreco tabela = buscarPrecoVidro(empresaId, item).orElse(null);

        if (tabela != null) {
            precoVidroCentavos = tabela.getPrecoVendaCentavos();
            custoVidroCentavos = tabela.getCustoCentavos();
        }

        List<ComponenteAdicional> componentes = new ArrayList<>();

        if (componentesRequest != null) {

            for (ComponenteManualRequest componenteRequest : componentesRequest) {

                if (componenteRequest.getTipo() == TipoComponenteCusto.VIDRO
                        || componenteRequest.getTipo() == TipoComponenteCusto.PERDAS) {

                    throw new IllegalArgumentException(
                            "Vidro e perdas são calculados automaticamente pelo motor, não podem ser adicionados manualmente"
                    );
                }

                Long tabelaPrecoId = componenteRequest.getTabelaPrecoId();
                long valorUnitarioCentavos;

                if (tabelaPrecoId != null) {

                    TabelaPreco tabelaPreco = tabelaPrecoRepository
                            .findByIdAndEmpresaId(tabelaPrecoId, empresaId)
                            .orElseThrow(() -> new TabelaPrecoNaoEncontradaException(
                                    "Item da tabela de preços não encontrado"
                            ));

                    valorUnitarioCentavos = tabelaPreco.getPrecoVendaCentavos();

                } else {

                    if (componenteRequest.getValorUnitario() == null) {

                        throw new IllegalArgumentException(
                                "Informe um valor unitário ou selecione um item da tabela de preços"
                        );
                    }

                    valorUnitarioCentavos = DinheiroUtils.paraCentavos(componenteRequest.getValorUnitario());
                }

                componentes.add(new ComponenteAdicional(
                        componenteRequest.getTipo(),
                        componenteRequest.getDescricao().trim(),
                        componenteRequest.getQuantidade(),
                        valorUnitarioCentavos,
                        OrigemSugestao.TABELA,
                        tabelaPrecoId
                ));
            }
        }

        return new ItemCalculoInput(
                regras,
                item.getLarguraVaoMm(), item.getAlturaVaoMm(),
                item.getLarguraVao2Mm(), item.getAlturaVao2Mm(),
                item.getQuantidade(),
                precoVidroCentavos, custoVidroCentavos,
                item.getTipoVidro(),
                componentes
        );
    }

    private OrcamentoTotaisResponse recalcularTotaisOrcamento(Orcamento orcamento) {

        OrcamentoTotaisResponse totais = calcularTotais(orcamento);

        orcamento.setCustoTotalCentavos(DinheiroUtils.paraCentavos(totais.getCustoTotal()));
        orcamento.setPrecoSugeridoCentavos(DinheiroUtils.paraCentavos(totais.getPrecoSugerido()));
        orcamento.setAjusteComercialCentavos(DinheiroUtils.paraCentavos(totais.getAjusteComercial()));
        orcamento.setValorTotalCentavos(DinheiroUtils.paraCentavos(totais.getValorFinal()));
        orcamento.setValorTotal(totais.getValorFinal());
        orcamento.setMargemRealPercentual(totais.getMargemReal());
        orcamento.setCustoRealTotalCentavos(
                totais.getCustoRealTotal() != null ? DinheiroUtils.paraCentavos(totais.getCustoRealTotal()) : null
        );
        orcamento.setMargemSobreCustoRealPercentual(totais.getMargemSobreCustoReal());
        orcamento.setCustoRealIncompleto(totais.isCustoRealIncompleto());

        orcamentoRepository.save(orcamento);

        return totais;
    }

    static long deslocamentoFixoCentavos(ParametroCalculo parametro) {

        Long valor = parametro.getValorDeslocamentoCentavos();

        return valor != null && valor > 0
                && (parametro.getRegraDeslocamento() == null || parametro.getRegraDeslocamento() == RegraDeslocamento.FIXO)
                ? valor : 0;
    }

    private OrcamentoTotaisResponse calcularTotais(Orcamento orcamento) {

        List<OrcamentoLinha> linhas = orcamentoLinhaRepository
                .findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(orcamento.getId());

        ParametroCalculo parametroCalculo = parametroCalculoService.buscarOuCriarPadrao(orcamento.getEmpresa().getId());
        ParametrosCalculoInput parametros = parametroCalculoService.paraMotor(parametroCalculo, orcamento.getParcelasCartao());

        long deslocamentoCentavos = linhas.isEmpty() ? 0 : deslocamentoFixoCentavos(parametroCalculo);

        long custoTotalCentavos = linhas.stream().mapToLong(OrcamentoLinha::getValorExibidoCentavos).sum()
                + deslocamentoCentavos;

        var totaisMotor = MotorCalculoOrcamento.fecharTotais(custoTotalCentavos, parametros);

        long precoSugeridoCentavos = totaisMotor.precoExibidoCentavos();
        Long precoManualCentavos = orcamento.getPrecoFinalManualCentavos();

        long valorFinalCentavos = precoManualCentavos != null ? precoManualCentavos : precoSugeridoCentavos;
        long ajusteComercialCentavos = valorFinalCentavos - precoSugeridoCentavos;

        CustoRealCalculado custoReal = calcularCustoReal(orcamento.getEmpresa().getId(), linhas);

        BigDecimal margemSobreCustoReal = custoReal.algumConhecido() && !custoReal.incompleto()
                ? MotorCalculoOrcamento.calcularMargemReal(valorFinalCentavos, custoReal.totalCentavos(), parametros)
                : null;

        BigDecimal margemReal = parametros.valoresSaoPrecoDeVenda()
                ? margemSobreCustoReal
                : MotorCalculoOrcamento.calcularMargemReal(valorFinalCentavos, custoTotalCentavos, parametros);

        long naoRevisados = linhas.stream().filter(l -> l.getValorFinalCentavos() == null).count();

        List<AlertaCalculoResponse> alertas = new ArrayList<>();

        if (margemReal != null) {
            MotorCalculoOrcamento.avaliarMargemMinima(margemReal, parametros)
                    .ifPresent(a -> alertas.add(new AlertaCalculoResponse(a.codigo(), a.mensagem())));
        }

        if (precoManualCentavos != null && precoManualCentavos != precoSugeridoCentavos) {
            alertas.add(new AlertaCalculoResponse(
                    ALERTA_PRECO_MANUAL,
                    "Preço final fixado manualmente em " + DinheiroUtils.paraReais(precoManualCentavos)
                            + "; o preço sugerido com os itens e parâmetros atuais é "
                            + DinheiroUtils.paraReais(precoSugeridoCentavos) + "."
            ));
        }

        List<String> bloqueios = new ArrayList<>();
        List<OrcamentoItem> itens = orcamentoItemRepository.findAllByOrcamentoIdOrderByOrdemAsc(orcamento.getId());

        if (itens.isEmpty()) {
            bloqueios.add("Adicione pelo menos um item ao orçamento.");
        }

        for (OrcamentoItem item : itens) {

            List<OrcamentoLinha> linhasItem = linhas.stream()
                    .filter(l -> l.getOrcamentoItem().getId().equals(item.getId()))
                    .toList();

            for (AlertaCalculoResponse pendencia : pendenciasDoItem(item, linhasItem)) {
                bloqueios.add(pendencia.getMensagem());
                alertas.add(pendencia);
            }
        }

        if (naoRevisados > 0) {
            bloqueios.add("Revise " + naoRevisados + (naoRevisados == 1 ? " valor sugerido" : " valores sugeridos")
                    + " (ou use \"Aceitar sugestões\") antes de enviar.");
        }

        if (valorFinalCentavos <= 0) {
            bloqueios.add("O valor final do orçamento está zerado.");
        }

        OrcamentoTotaisResponse response = new OrcamentoTotaisResponse();

        response.setCustoTotal(DinheiroUtils.paraReais(custoTotalCentavos));
        response.setDeslocamento(DinheiroUtils.paraReais(deslocamentoCentavos));
        response.setPrecoSugerido(DinheiroUtils.paraReais(precoSugeridoCentavos));
        response.setAjusteComercial(DinheiroUtils.paraReais(ajusteComercialCentavos));
        response.setValorFinal(DinheiroUtils.paraReais(valorFinalCentavos));
        response.setMargemReal(margemReal);
        response.setCustoRealTotal(
                custoReal.algumConhecido() ? DinheiroUtils.paraReais(custoReal.totalCentavos()) : null
        );
        response.setMargemSobreCustoReal(margemSobreCustoReal);
        response.setCustoRealIncompleto(custoReal.incompleto());
        response.setValoresNaoRevisados((int) naoRevisados);
        response.setAlertas(alertas);
        response.setBloqueiosEnvio(bloqueios);
        response.setPrecoFinalManual(precoManualCentavos != null ? DinheiroUtils.paraReais(precoManualCentavos) : null);
        response.setModoPrecificacao(parametroCalculo.getModoPrecificacao() != null
                ? parametroCalculo.getModoPrecificacao().name() : null);
        response.setParcelasCartao(orcamento.getParcelasCartao());

        return response;
    }

    private CustoRealCalculado calcularCustoReal(Long empresaId, List<OrcamentoLinha> linhas) {

        long totalCentavos = 0L;
        boolean algumConhecido = false;
        boolean incompleto = false;

        Map<OrcamentoItem, Optional<TabelaPreco>> vidroPorItem = new HashMap<>();

        for (OrcamentoLinha linha : linhas) {

            TabelaPreco tabela = tabelaDeCusto(empresaId, linha, vidroPorItem);

            Long custoCentavos = tabela != null ? tabela.getCustoCentavos() : null;
            Long precoVendaCentavos = tabela != null ? tabela.getPrecoVendaCentavos() : null;

            if (custoCentavos == null || precoVendaCentavos == null || precoVendaCentavos <= 0) {
                incompleto = true;
                continue;
            }

            long custoRealLinha = BigDecimal.valueOf(linha.getValorExibidoCentavos())
                    .multiply(BigDecimal.valueOf(custoCentavos))
                    .divide(BigDecimal.valueOf(precoVendaCentavos), 0, RoundingMode.HALF_UP)
                    .longValueExact();

            totalCentavos += custoRealLinha;
            algumConhecido = true;
        }

        return new CustoRealCalculado(totalCentavos, algumConhecido, incompleto);
    }

    private record CustoRealCalculado(long totalCentavos, boolean algumConhecido, boolean incompleto) {
    }

    private TabelaPreco tabelaDeCusto(
            Long empresaId, OrcamentoLinha linha, Map<OrcamentoItem, Optional<TabelaPreco>> vidroPorItem) {

        if (linha.getTabelaPreco() != null) {
            return linha.getTabelaPreco();
        }

        OrcamentoItem item = linha.getOrcamentoItem();
        TipoComponenteCusto tipo = linha.getTipoComponente();

        if (item == null || (tipo != TipoComponenteCusto.VIDRO && tipo != TipoComponenteCusto.PERDAS)) {
            return null;
        }

        return vidroPorItem.computeIfAbsent(item, i -> buscarPrecoVidro(empresaId, i)).orElse(null);
    }

    private Optional<TabelaPreco> buscarPrecoVidro(Long empresaId, OrcamentoItem item) {

        if (item.getTipoVidro() == null || item.getEspessuraMm() == null || item.getCor() == null) {
            return Optional.empty();
        }

        return tabelaPrecoRepository.findFirstByEmpresaIdAndAtivoTrueAndTipoVidroAndEspessuraMmAndCorAndAcabamento(
                empresaId, item.getTipoVidro(), item.getEspessuraMm(), item.getCor(), item.getAcabamento()
        );
    }

    private void exigirPermissaoPrecoLivre(
            List<ComponenteManualRequest> componentes,
            List<OrcamentoLinha> linhasAtuais,
            boolean podeDefinirPrecoLivre) {

        if (podeDefinirPrecoLivre || componentes == null) {
            return;
        }

        for (ComponenteManualRequest componente : componentes) {

            if (componente.getTabelaPrecoId() != null) {
                continue;
            }

            long valorCentavos = DinheiroUtils.paraCentavos(componente.getValorUnitario());
            String descricao = componente.getDescricao() == null ? "" : componente.getDescricao().trim();

            boolean jaExistia = linhasAtuais.stream().anyMatch(linha ->
                    linha.getTipoComponente() == componente.getTipo()
                            && descricao.equals(descricaoBase(linha))
                            && linha.getValorUnitarioCentavos() != null
                            && linha.getValorUnitarioCentavos() == valorCentavos
            );

            if (!jaExistia) {
                throw new AccessDeniedException(
                        "Só gerente ou administrador pode lançar componente com valor digitado à mão. "
                                + "Escolha um item da Tabela de Preços."
                );
            }
        }
    }

    private static String descricaoBase(OrcamentoLinha linha) {
        return linha.getComponenteDescricao() != null ? linha.getComponenteDescricao() : linha.getDescricao();
    }

    private static String chaveLinha(TipoComponenteCusto tipo, String descricao, Long tabelaPrecoId) {
        return tipo + "|" + (tipo == TipoComponenteCusto.VIDRO || tipo == TipoComponenteCusto.PERDAS ? "" : descricao)
                + "|" + tabelaPrecoId;
    }

    private void aplicarCamposBasicos(OrcamentoItem item, OrcamentoItemRequest request) {

        item.setAmbiente(normalizar(request.getAmbiente()));
        item.setCorFerragem(normalizar(request.getCorFerragem()));
        item.setObservacoes(normalizar(request.getObservacoes()));
        item.setQuantidade(request.getQuantidade() == null ? 1 : request.getQuantidade());
        item.setTipoVidro(request.getTipoVidro());
        item.setEspessuraMm(request.getEspessuraMm());
        item.setCor(request.getCor());
        item.setAcabamento(request.getAcabamento());

        Integer larguraVaoMm = request.getLarguraVaoMm();
        Integer alturaVaoMm = request.getAlturaVaoMm();

        if ((larguraVaoMm == null || alturaVaoMm == null) && request.getMedidaTexto() != null) {

            item.setMedidaTextoOriginal(request.getMedidaTexto());

            var medidaParseada = MedidaParser.tentarInterpretar(request.getMedidaTexto());

            if (medidaParseada.isEmpty()) {

                throw new IllegalArgumentException("Não entendi a medida \"" + request.getMedidaTexto()
                        + "\". Informe largura x altura entre " + MedidaParser.LIMITE_MINIMO_MM / 10 + " cm e "
                        + MedidaParser.LIMITE_MAXIMO_MM / 1000 + " m, ex.: 1,20 x 1,90.");
            }

            larguraVaoMm = medidaParseada.get().larguraMm();
            alturaVaoMm = medidaParseada.get().alturaMm();
            item.setMedidaAproximada(true);
        }

        item.setLarguraVaoMm(larguraVaoMm);
        item.setAlturaVaoMm(alturaVaoMm);
        item.setLarguraVao2Mm(request.getLarguraVao2Mm());
        item.setAlturaVao2Mm(request.getAlturaVao2Mm());
    }

    private void garantirEditavel(Orcamento orcamento) {

        if (!STATUS_EDITAVEL.contains(orcamento.getStatus())) {

            throw new OrcamentoNaoEditavelException(
                    "Não é possível editar os itens de um orçamento com status " + orcamento.getStatus()
            );
        }
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private Orcamento buscarOrcamento(Long empresaId, Long orcamentoId) {

        return orcamentoRepository.findByIdAndEmpresaId(orcamentoId, empresaId)
                .orElseThrow(() -> new OrcamentoNaoEncontradoException("Orçamento não encontrado"));
    }

    private OrcamentoItem buscarItem(Long orcamentoId, Long itemId) {

        return orcamentoItemRepository.findByIdAndOrcamentoId(itemId, orcamentoId)
                .orElseThrow(() -> new OrcamentoItemNaoEncontradoException("Item não encontrado"));
    }

    private Tipologia buscarTipologia(Long empresaId, Long tipologiaId) {

        return tipologiaRepository.findByIdAndEmpresaId(tipologiaId, empresaId)
                .orElseThrow(() -> new TipologiaNaoEncontradaException("Tipologia não encontrada"));
    }
}
