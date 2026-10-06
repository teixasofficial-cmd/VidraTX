import { AlertTriangle, Pencil, Ruler, Trash2 } from 'lucide-react';
import { LinhaCustoFantasma } from './LinhaCustoFantasma';
import type { OrcamentoItem } from '../types';

function formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
}

interface ItemOrcamentoCardProps {
    item: OrcamentoItem;
    editavel: boolean;
    podeAlterarPrecos: boolean;
    onAjustarLinha: (linhaId: number, valorFinal: number | null) => void;
    onEditar: () => void;
    onRemover: () => void;
}

export function ItemOrcamentoCard({
    item,
    editavel,
    podeAlterarPrecos,
    onAjustarLinha,
    onEditar,
    onRemover,
}: ItemOrcamentoCardProps) {

    const especificacao = [
        item.tipoVidro, item.espessuraMm ? `${item.espessuraMm}mm` : null, item.cor, item.acabamento,
    ].filter(Boolean).join(' · ');

    return (
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div className="flex items-start justify-between gap-2">
                <div>
                    <p className="text-sm font-semibold text-slate-900">{item.tipologiaNome}</p>
                    <p className="text-xs text-slate-500">
                        {item.ambiente && <>{item.ambiente} · </>}
                        {especificacao || 'especificação de vidro pendente'}
                        {item.corFerragem && <> · ferragem {item.corFerragem}</>}
                    </p>
                    {item.larguraVaoMm && item.alturaVaoMm && (
                        <p className="mt-0.5 flex items-center gap-1 text-xs text-slate-400">
                            <Ruler className="h-3 w-3" />
                            Vão {(item.larguraVaoMm / 10).toFixed(0)} × {(item.alturaVaoMm / 10).toFixed(0)} cm
                            {item.medidaAproximada && ' (aproximado)'}
                        </p>
                    )}
                </div>

                {editavel && (
                    <div className="flex shrink-0 gap-1">
                        <button onClick={onEditar} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600" aria-label="Editar item">
                            <Pencil className="h-4 w-4" />
                        </button>
                        <button onClick={onRemover} className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600" aria-label="Remover item">
                            <Trash2 className="h-4 w-4" />
                        </button>
                    </div>
                )}
            </div>

            {item.pecas.length > 0 && (
                <div className="mt-3 flex flex-wrap gap-2">
                    {item.pecas.map((peca) => (
                        <span
                            key={peca.id}
                            className={`rounded-md px-2 py-1 text-[11px] font-medium ${
                                peca.excedeTamanhoMaximo ? 'bg-amber-50 text-amber-700' : 'bg-slate-50 text-slate-500'
                            }`}
                        >
                            {peca.descricao} · corte {peca.larguraCorteMm}×{peca.alturaCorteMm} mm
                        </span>
                    ))}
                </div>
            )}

            {item.alertas.length > 0 && (
                <div className="mt-3 space-y-1">
                    {item.alertas.map((alerta) => (
                        <p key={alerta.codigo} className="flex items-start gap-1.5 rounded-lg bg-amber-50 px-2 py-1.5 text-xs text-amber-700">
                            <AlertTriangle className="mt-0.5 h-3.5 w-3.5 shrink-0" />
                            {alerta.mensagem}
                        </p>
                    ))}
                </div>
            )}

            <div className="mt-3 divide-y divide-slate-100 border-t border-slate-100 pt-1">
                {item.linhas.map((linha) => (
                    <LinhaCustoFantasma
                        key={linha.id}
                        linha={linha}
                        podeEditar={editavel && podeAlterarPrecos}
                        onAjustar={(valorFinal) => onAjustarLinha(linha.id, valorFinal)}
                    />
                ))}
            </div>

            <div className="mt-2 flex justify-end border-t border-slate-100 pt-2 text-sm">
                <span className="text-slate-500">Subtotal do item:&nbsp;</span>
                <span className="font-semibold text-slate-900">{formatarMoeda(item.custoItem)}</span>
            </div>
        </div>
    );
}
