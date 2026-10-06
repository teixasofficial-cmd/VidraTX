package br.com.vidratx.service;

import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Tipologia;
import br.com.vidratx.enums.CategoriaTipologia;
import br.com.vidratx.repository.TipologiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipologiaSeedService {

    private record DefinicaoTipologia(String codigo, String nome, CategoriaTipologia categoria, String regrasJson) {
    }

    private static final List<DefinicaoTipologia> CATALOGO_PADRAO = List.of(
            new DefinicaoTipologia(
                    "box_frontal_2f", "Box frontal (2 folhas)", CategoriaTipologia.BOX,
                    """
                    {"numeroFolhas":2,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":12,"descontoAlturaMm":0,"transpasseMm":50,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "box_canto_4f", "Box de canto (4 folhas)", CategoriaTipologia.BOX,
                    """
                    {"numeroFolhas":4,"formulaPecas":"DOIS_VAOS_DIVIDIDOS","descontoLarguraMm":12,"descontoAlturaMm":0,"transpasseMm":50,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "box_abrir_pivotante", "Box de abrir/pivotante", CategoriaTipologia.BOX,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":10,"descontoAlturaMm":10,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "espelho", "Espelho", CategoriaTipologia.ESPELHO,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "porta_pivotante", "Porta de vidro temperado (pivotante)", CategoriaTipologia.PORTA,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":10,"descontoAlturaMm":15,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "porta_correr", "Porta de vidro temperado (de correr)", CategoriaTipologia.PORTA,
                    """
                    {"numeroFolhas":2,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":12,"descontoAlturaMm":0,"transpasseMm":40,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "janela_2f", "Janela de vidro temperado (2 folhas)", CategoriaTipologia.JANELA,
                    """
                    {"numeroFolhas":2,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":10,"descontoAlturaMm":0,"transpasseMm":30,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "janela_4f", "Janela de vidro temperado (4 folhas)", CategoriaTipologia.JANELA,
                    """
                    {"numeroFolhas":4,"formulaPecas":"DIVIDIR_LARGURA_IGUAL","descontoLarguraMm":10,"descontoAlturaMm":0,"transpasseMm":30,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "vidro_avulso", "Troca de vidro / vidro avulso", CategoriaTipologia.VIDRO_AVULSO,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "guarda_corpo", "Guarda-corpo", CategoriaTipologia.GUARDA_CORPO,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":["REQUER_LAMINADO"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "envidracamento_sacada", "Envidraçamento de sacada", CategoriaTipologia.ENVIDRACAMENTO_SACADA,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":["REQUER_VIDRO_SEGURANCA"]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "tampo_prateleira", "Tampo de mesa e prateleira", CategoriaTipologia.TAMPO_PRATELEIRA,
                    """
                    {"numeroFolhas":1,"formulaPecas":"PECA_UNICA","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}
                    """.strip()
            ),
            new DefinicaoTipologia(
                    "item_livre", "Item livre", CategoriaTipologia.ITEM_LIVRE,
                    """
                    {"numeroFolhas":0,"formulaPecas":"SEM_PECAS","descontoLarguraMm":0,"descontoAlturaMm":0,"transpasseMm":0,"alertasNormativos":[]}
                    """.strip()
            )
    );

    private final TipologiaRepository tipologiaRepository;

    public TipologiaSeedService(TipologiaRepository tipologiaRepository) {
        this.tipologiaRepository = tipologiaRepository;
    }

    @Transactional
    public void seedPadrao(Empresa empresa) {

        if (tipologiaRepository.existsByEmpresaId(empresa.getId())) {
            return;
        }

        for (DefinicaoTipologia definicao : CATALOGO_PADRAO) {

            Tipologia tipologia = new Tipologia();

            tipologia.setEmpresa(empresa);
            tipologia.setCodigo(definicao.codigo());
            tipologia.setNome(definicao.nome());
            tipologia.setCategoria(definicao.categoria());
            tipologia.setRegrasJson(definicao.regrasJson());

            tipologiaRepository.save(tipologia);
        }
    }
}
