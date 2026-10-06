import { useState, type KeyboardEvent } from 'react';
import { Check, RotateCcw, Sparkles } from 'lucide-react';
import type { OrcamentoLinha } from '../types';
import { formatarDecimal, formatarMoeda, lerValorDecimal } from '../lib/formato';

const rotulosTipo: Record<OrcamentoLinha['tipo'], string> = {
    VIDRO: 'Vidro',
    LAPIDACAO: 'Lapidação',
    BISOTE: 'Bisotê',
    FURO: 'Furo',
    RECORTE: 'Recorte',
    FERRAGEM: 'Ferragem',
    KIT: 'Kit',
    MAO_DE_OBRA: 'Mão de obra',
    DESLOCAMENTO: 'Deslocamento',
    PERDAS: 'Perdas',
    AJUSTE_COMERCIAL: 'Ajuste comercial',
    ITEM_LIVRE: 'Item livre',
};

interface LinhaCustoFantasmaProps {
    linha: OrcamentoLinha;
    podeEditar: boolean;
    onAjustar: (valorFinal: number | null) => void;
}

export function LinhaCustoFantasma({ linha, podeEditar, onAjustar }: LinhaCustoFantasmaProps) {

    const ehSugestao = linha.valorFinal == null;
    const [texto, setTexto] = useState(formatarDecimal(linha.valorExibido));
    const [editando, setEditando] = useState(false);

    const valorExibidoAtual = editando ? texto : formatarDecimal(linha.valorExibido);

    function commit() {

        setEditando(false);

        const valorNumerico = lerValorDecimal(texto);

        if (valorNumerico === null || valorNumerico < 0) {
            setTexto(formatarDecimal(linha.valorExibido));
            return;
        }

        if (valorNumerico === linha.valorExibido && !ehSugestao) {
            return;
        }

        onAjustar(valorNumerico);
    }

    function aceitarSugestao() {
        onAjustar(linha.valorSugerido);
    }

    function aoTeclar(evento: KeyboardEvent<HTMLInputElement>) {

        if (evento.key === 'Tab' || evento.key === 'Enter') {

            if (ehSugestao) {
                evento.preventDefault();
                aceitarSugestao();
            } else {
                commit();
            }
        }
    }

    return (
        <div className="flex items-center gap-3 py-1.5">
            <div className="min-w-0 flex-1">
                <p className="truncate text-sm text-slate-700">{linha.descricao}</p>
                <p className="text-xs text-slate-400">
                    {rotulosTipo[linha.tipo]}
                    {Number(linha.quantidade) !== 1 ? ` · ${Number(linha.quantidade).toLocaleString('pt-BR')}× ` : ''}
                    {linha.valorUnitario != null && Number(linha.quantidade) !== 1
                        ? `${formatarMoeda(linha.valorUnitario)} cada`
                        : ''}
                </p>
            </div>

            {!podeEditar ? (
                <div className="flex shrink-0 items-center gap-1.5">
                    {ehSugestao && (
                        <span title="Sugestão calculada — ainda não revisada por um gerente">
                            <Sparkles className="h-3.5 w-3.5 text-slate-400" />
                        </span>
                    )}
                    <span className={`text-sm ${ehSugestao ? 'italic text-slate-400' : 'text-slate-900'}`}>
                        {formatarMoeda(linha.valorExibido)}
                    </span>
                </div>
            ) : (
            <div className="flex shrink-0 flex-col items-end">
                <div className="flex items-center gap-1.5">
                    {ehSugestao && (
                        <span title={`Sugestão calculada · ${linha.origemSugestao.toLowerCase()}`}>
                            <Sparkles className="h-3.5 w-3.5 text-slate-400" />
                        </span>
                    )}
                    <div className="relative">
                        <span className="pointer-events-none absolute left-2 top-1/2 -translate-y-1/2 text-xs text-slate-400">
                            R$
                        </span>
                        <input
                            type="text"
                            inputMode="decimal"
                            value={valorExibidoAtual}
                            onFocus={() => {
                                setEditando(true);
                                setTexto(String(linha.valorExibido.toFixed(2)).replace('.', ','));
                            }}
                            onChange={(e) => setTexto(e.target.value)}
                            onBlur={commit}
                            onKeyDown={aoTeclar}
                            className={`w-24 rounded-lg border py-1 pl-7 pr-2 text-right text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 ${
                                ehSugestao
                                    ? 'border-dashed border-slate-300 italic text-slate-400 focus:border-indigo-400'
                                    : 'border-slate-300 text-slate-900 focus:border-indigo-500'
                            }`}
                        />
                    </div>
                    {ehSugestao && (
                        <button
                            type="button"
                            title="Aceitar sugestão"
                            onClick={aceitarSugestao}
                            className="rounded-md p-1 text-emerald-500 hover:bg-emerald-50"
                        >
                            <Check className="h-4 w-4" />
                        </button>
                    )}
                </div>

                {!ehSugestao && linha.valorFinal !== linha.valorSugerido && (
                    <button
                        type="button"
                        onClick={() => onAjustar(null)}
                        className="mt-1 flex items-center gap-1 text-[11px] text-slate-400 hover:text-indigo-600"
                    >
                        <RotateCcw className="h-3 w-3" />
                        Sugerido: {formatarMoeda(linha.valorSugerido)} · voltar à sugestão
                    </button>
                )}
            </div>
            )}
        </div>
    );
}
