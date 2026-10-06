import { useState, type FormEvent } from 'react';
import { Ban, CheckCheck, Loader2, Lock, RotateCcw } from 'lucide-react';
import type { OrcamentoTotais, ParametroCalculo } from '../types';
import { formatarDecimal, formatarMoeda, lerValorDecimal } from '../lib/formato';
import { inputClass } from './FormField';

interface ResumoFinanceiroPainelProps {
    totais: OrcamentoTotais;
    parametros?: ParametroCalculo;
    editavel: boolean;
    podeAlterarPrecos: boolean;
    onAceitarTodas: () => void;
    aceitandoTodas: boolean;
    onAjustarPrecoFinal: (valor: number | null) => void;
    ajustandoPrecoFinal: boolean;
    onDefinirParcelas: (parcelas: number | null) => void;
    definindoParcelas: boolean;
}

export function ResumoFinanceiroPainel({
    totais,
    parametros,
    editavel,
    podeAlterarPrecos,
    onAceitarTodas,
    aceitandoTodas,
    onAjustarPrecoFinal,
    ajustandoPrecoFinal,
    onDefinirParcelas,
    definindoParcelas,
}: ResumoFinanceiroPainelProps) {

    const [editandoTotal, setEditandoTotal] = useState(false);
    const [totalDigitado, setTotalDigitado] = useState('');

    const podeEditarPreco = editavel && podeAlterarPrecos;

    const margemReal = totais.margemReal;

    const corMargem =
        parametros && margemReal !== null && margemReal < parametros.percentualMargemMinima
            ? 'text-rose-600'
            : parametros && margemReal !== null && margemReal < parametros.percentualMargemDesejada
                ? 'text-amber-600'
                : 'text-emerald-600';

    const opcoesParcelas = Object.entries(parametros?.taxaCartaoParcelas ?? {})
        .filter(([, taxa]) => taxa != null)
        .map(([parcelas]) => Number(parcelas))
        .filter((parcelas) => Number.isInteger(parcelas) && parcelas >= 1)
        .sort((a, b) => a - b);

    function confirmarTotal() {

        setEditandoTotal(false);

        const valor = lerValorDecimal(totalDigitado);

        if (valor === null || valor < 0) {
            return;
        }

        if (valor.toFixed(2) !== totais.valorFinal.toFixed(2)) {
            onAjustarPrecoFinal(valor);
        }
    }

    function aoSubmeterTotal(evento: FormEvent) {
        evento.preventDefault();
        confirmarTotal();
    }

    return (
        <div className="sticky top-6 space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Resumo financeiro</p>

            <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                    <span className="text-slate-500">Custo total</span>
                    <span className="font-medium text-slate-700">{formatarMoeda(totais.custoTotal)}</span>
                </div>
                {(totais.deslocamento ?? 0) > 0 && (
                    <div className="flex items-center justify-between text-xs">
                        <span className="text-slate-400">inclui deslocamento</span>
                        <span className="text-slate-500">{formatarMoeda(totais.deslocamento)}</span>
                    </div>
                )}
                <div className="flex justify-between">
                    <span className="text-slate-500">Preço sugerido</span>
                    <span className="font-medium text-slate-700">{formatarMoeda(totais.precoSugerido)}</span>
                </div>
                {totais.ajusteComercial !== 0 && (
                    <div className="flex justify-between">
                        <span className="text-slate-500">
                            {totais.ajusteComercial < 0 ? 'Desconto' : 'Acréscimo'}
                        </span>
                        <span className={totais.ajusteComercial < 0 ? 'text-rose-600' : 'text-emerald-600'}>
                            {totais.ajusteComercial > 0 ? '+' : ''}
                            {formatarMoeda(totais.ajusteComercial)}
                        </span>
                    </div>
                )}
            </div>

            <div className="rounded-xl bg-slate-50 p-3">
                <div className="flex items-center justify-between">
                    <p className="text-xs font-medium text-slate-500">Preço final</p>
                    {totais.precoFinalManual != null && (
                        <span
                            className="flex items-center gap-1 text-[11px] font-medium text-indigo-600"
                            title="Preço fixado por um gerente: não muda quando itens ou parâmetros mudam"
                        >
                            <Lock className="h-3 w-3" />
                            fixado
                        </span>
                    )}
                </div>
                {podeEditarPreco && editandoTotal ? (
                    <form onSubmit={aoSubmeterTotal} className="mt-1 flex gap-2">
                        <input
                            autoFocus
                            type="text"
                            inputMode="decimal"
                            value={totalDigitado}
                            onChange={(e) => setTotalDigitado(e.target.value)}
                            onBlur={confirmarTotal}
                            onKeyDown={(e) => {
                                if (e.key === 'Escape') setEditandoTotal(false);
                            }}
                            className={inputClass}
                        />
                        <button
                            type="submit"
                            onMouseDown={(e) => e.preventDefault()}
                            disabled={ajustandoPrecoFinal}
                            className="rounded-lg bg-indigo-600 px-3 py-2 text-xs font-semibold text-white"
                        >
                            OK
                        </button>
                    </form>
                ) : (
                    <button
                        type="button"
                        disabled={!podeEditarPreco}
                        onClick={() => {
                            setTotalDigitado(formatarDecimal(totais.valorFinal));
                            setEditandoTotal(true);
                        }}
                        className="mt-1 block text-2xl font-bold text-slate-900 disabled:cursor-default"
                        title={podeEditarPreco ? 'Clique para fixar o preço final (desconto ou acréscimo)' : undefined}
                    >
                        {ajustandoPrecoFinal ? <Loader2 className="h-6 w-6 animate-spin" /> : formatarMoeda(totais.valorFinal)}
                    </button>
                )}
                {podeEditarPreco && totais.precoFinalManual != null && !editandoTotal && (
                    <button
                        type="button"
                        onClick={() => onAjustarPrecoFinal(null)}
                        disabled={ajustandoPrecoFinal}
                        className="mt-1 flex items-center gap-1 text-[11px] text-slate-500 hover:text-indigo-600"
                    >
                        <RotateCcw className="h-3 w-3" />
                        Voltar ao preço calculado ({formatarMoeda(totais.precoSugerido)})
                    </button>
                )}
            </div>

            <div className="flex items-center justify-between gap-2 text-sm">
                <span className="text-slate-500">Pagamento no cartão</span>
                {podeEditarPreco && opcoesParcelas.length > 0 ? (
                    <select
                        value={totais.parcelasCartao ?? ''}
                        disabled={definindoParcelas}
                        onChange={(e) => onDefinirParcelas(e.target.value === '' ? null : Number(e.target.value))}
                        className="rounded-lg border border-slate-300 px-2 py-1 text-sm text-slate-700"
                    >
                        <option value="">Sem parcelamento</option>
                        {opcoesParcelas.map((parcelas) => (
                            <option key={parcelas} value={parcelas}>{parcelas}x</option>
                        ))}
                    </select>
                ) : (
                    <span className="font-medium text-slate-700">
                        {totais.parcelasCartao ? `${totais.parcelasCartao}x` : 'Sem parcelamento'}
                    </span>
                )}
            </div>

            {parametros && margemReal !== null && (
                <div className="flex items-center justify-between text-sm">
                    <span className="text-slate-500">
                        Margem real{totais.modoPrecificacao === 'VENDA' ? ' (sobre a venda)' : ''}
                    </span>
                    <span className={`font-semibold ${corMargem}`}>
                        {margemReal.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%
                    </span>
                </div>
            )}

            {totais.margemSobreCustoReal !== null && totais.margemSobreCustoReal !== margemReal && (
                <div className="flex items-center justify-between text-sm">
                    <span className="text-slate-500">Margem sobre seu custo</span>
                    <span className="font-semibold text-slate-700">
                        {totais.margemSobreCustoReal.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%
                    </span>
                </div>
            )}
            {totais.custoRealIncompleto && (
                <p className="text-[11px] leading-relaxed text-slate-400">
                    Preencha "Seu custo" nos itens da Tabela de Preços usados aqui para ver a
                    margem sobre o que você realmente pagou. Componente com valor digitado à mão
                    não tem custo cadastrado: com ele no orçamento, essa margem não aparece.
                </p>
            )}

            {totais.alertas.map((alerta, indice) => (
                <p key={`${alerta.codigo}-${indice}`} className="rounded-lg bg-rose-50 px-2.5 py-2 text-xs text-rose-700">
                    {alerta.mensagem}
                </p>
            ))}

            {totais.valoresNaoRevisados > 0 && (
                <div className="flex items-center justify-between rounded-lg bg-amber-50 px-3 py-2">
                    <span className="text-xs font-medium text-amber-700">
                        {totais.valoresNaoRevisados} {totais.valoresNaoRevisados === 1 ? 'valor' : 'valores'} ainda não revisado{totais.valoresNaoRevisados === 1 ? '' : 's'}
                    </span>
                    {editavel && (
                        <button
                            type="button"
                            onClick={onAceitarTodas}
                            disabled={aceitandoTodas}
                            className="flex items-center gap-1 text-xs font-semibold text-amber-800 hover:underline"
                        >
                            {aceitandoTodas ? <Loader2 className="h-3 w-3 animate-spin" /> : <CheckCheck className="h-3 w-3" />}
                            Aceitar todas
                        </button>
                    )}
                </div>
            )}

            {editavel && totais.bloqueiosEnvio.length > 0 && (
                <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2">
                    <p className="flex items-center gap-1.5 text-xs font-semibold text-rose-700">
                        <Ban className="h-3.5 w-3.5" />
                        Ainda não pode ser enviado ao cliente
                    </p>
                    <ul className="mt-1 list-disc space-y-0.5 pl-5 text-xs text-rose-700">
                        {totais.bloqueiosEnvio.map((bloqueio) => (
                            <li key={bloqueio}>{bloqueio}</li>
                        ))}
                    </ul>
                </div>
            )}

            <p className="border-t border-slate-100 pt-3 text-[11px] leading-relaxed text-slate-400">
                Os valores em cinza são sugestões calculadas a partir da sua tabela.
                {podeAlterarPrecos
                    ? ' Você decide o preço final.'
                    : ' Só gerentes e administradores alteram preços e descontos.'}
            </p>
        </div>
    );
}
