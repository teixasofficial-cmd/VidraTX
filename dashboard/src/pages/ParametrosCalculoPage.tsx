import { useEffect, useState, type ReactNode } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2, Save } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { ErrorState } from '../components/ErrorState';
import { FormField, inputClass } from '../components/FormField';
import { ApiRequestError } from '../lib/apiClient';
import { usePodeAlterarPrecos } from '../lib/auth';
import { TipologiasFolgasPainel } from '../components/TipologiasFolgasPainel';
import type { ArredondamentoComercial, ModoPrecificacao, ParametroCalculoPayload, RegimeTributario } from '../types';

const PARCELAS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

function Secao({ titulo, descricao, children }: { titulo: string; descricao?: string; children: ReactNode }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-sm font-semibold text-slate-900">{titulo}</h2>
            {descricao && <p className="mt-0.5 text-xs text-slate-500">{descricao}</p>}
            <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">{children}</div>
        </div>
    );
}

export function ParametrosCalculoPage() {

    const queryClient = useQueryClient();
    const podeEditar = usePodeAlterarPrecos();
    const [erro, setErro] = useState<string | null>(null);
    const [sucesso, setSucesso] = useState(false);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['parametros-calculo'],
        queryFn: api.parametrosCalculo,
    });

    const [form, setForm] = useState<ParametroCalculoPayload | null>(null);

    useEffect(() => {
        if (data) {
            setForm(data);
        }
    }, [data]);

    const mutation = useMutation({
        mutationFn: (payload: ParametroCalculoPayload) => api.atualizarParametrosCalculo(payload),
        onSuccess: (salvo) => {
            queryClient.setQueryData(['parametros-calculo'], salvo);
            setSucesso(true);
            setTimeout(() => setSucesso(false), 3000);
        },
        onError: (excecao) =>
            setErro(excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível salvar'),
    });

    if (isError) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    if (isLoading || !form) {
        return <Spinner />;
    }

    function num(valor: string): number {
        return Number(valor.replace(',', '.')) || 0;
    }

    function atualizar<K extends keyof ParametroCalculoPayload>(campo: K, valor: ParametroCalculoPayload[K]) {
        setForm((atual) => (atual ? { ...atual, [campo]: valor } : atual));
    }

    function aoSalvar() {
        setErro(null);
        if (form) mutation.mutate(form);
    }

    return (
        <div className="max-w-3xl space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-slate-900">Parâmetros de Cálculo</h1>
                <p className="mt-1 text-sm text-slate-500">
                    Como o motor calcula preço, margem e alertas — tudo com um valor padrão, ajuste o que quiser.
                </p>
                {!podeEditar && (
                    <p className="mt-2 rounded-lg bg-slate-100 px-3 py-2 text-sm text-slate-600">
                        Só gerentes e administradores alteram estes parâmetros.
                    </p>
                )}
            </div>

            <fieldset disabled={!podeEditar} className="space-y-6">

            <Secao titulo="Precificação" descricao="Como o preço de venda é sugerido a partir do custo.">
                <FormField label="Você informa">
                    <select
                        value={form.modoPrecificacao}
                        onChange={(e) => atualizar('modoPrecificacao', e.target.value as ModoPrecificacao)}
                        className={inputClass}
                    >
                        <option value="CUSTO">Meu custo (o sistema sugere o preço de venda)</option>
                        <option value="VENDA">Meu preço de venda direto</option>
                    </select>
                </FormField>
                <FormField label="Regime tributário">
                    <select
                        value={form.regimeTributario ?? ''}
                        onChange={(e) => atualizar('regimeTributario', (e.target.value || null) as RegimeTributario)}
                        className={inputClass}
                    >
                        <option value="">Não informado</option>
                        <option value="MEI">MEI</option>
                        <option value="SIMPLES_NACIONAL">Simples Nacional</option>
                        <option value="OUTRO">Outro</option>
                    </select>
                </FormField>
                <FormField label="Impostos (%)">
                    <input value={form.percentualImpostos} onChange={(e) => atualizar('percentualImpostos', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Comissão (%)">
                    <input value={form.percentualComissao} onChange={(e) => atualizar('percentualComissao', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Margem desejada (%)">
                    <input value={form.percentualMargemDesejada} onChange={(e) => atualizar('percentualMargemDesejada', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Margem mínima (%) — alerta abaixo disso">
                    <input value={form.percentualMargemMinima} onChange={(e) => atualizar('percentualMargemMinima', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Arredondamento do preço final">
                    <select
                        value={form.arredondamentoComercial}
                        onChange={(e) => atualizar('arredondamentoComercial', e.target.value as ArredondamentoComercial)}
                        className={inputClass}
                    >
                        <option value="NENHUM">Nenhum</option>
                        <option value="PROXIMA_DEZENA">Próxima dezena (R$ 10)</option>
                        <option value="TERMINAR_90">Terminar em ,90</option>
                        <option value="PROXIMA_CENTENA">Próxima centena (R$ 100)</option>
                    </select>
                </FormField>
            </Secao>

            <Secao titulo="Medidas e perdas" descricao="Como as medidas do vão viram peças de corte.">
                <FormField label="Arredondar medidas para múltiplos de (mm)">
                    <input value={form.multiploArredondamentoMm} onChange={(e) => atualizar('multiploArredondamentoMm', Number(e.target.value) || 0)} className={inputClass} inputMode="numeric" />
                </FormField>
                <FormField label="Área mínima cobrada por peça (m²)">
                    <input value={form.areaMinimaM2} onChange={(e) => atualizar('areaMinimaM2', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Perdas/quebra (%)">
                    <input value={form.percentualPerdas} onChange={(e) => atualizar('percentualPerdas', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Tamanho máximo de chapa — largura (mm)">
                    <input value={form.tamanhoMaximoChapaLarguraMm} onChange={(e) => atualizar('tamanhoMaximoChapaLarguraMm', Number(e.target.value) || 0)} className={inputClass} inputMode="numeric" />
                </FormField>
                <FormField label="Tamanho máximo de chapa — altura (mm)">
                    <input value={form.tamanhoMaximoChapaAlturaMm} onChange={(e) => atualizar('tamanhoMaximoChapaAlturaMm', Number(e.target.value) || 0)} className={inputClass} inputMode="numeric" />
                </FormField>
            </Secao>

            <Secao
                titulo="Deslocamento, visita e proposta"
                descricao="O deslocamento entra uma vez em cada orçamento. A visita técnica aparece na proposta de data da medição."
            >
                <FormField label="Deslocamento por orçamento (R$) — 0 para não cobrar">
                    <input
                        value={form.valorDeslocamento ?? 0}
                        onChange={(e) => {
                            atualizar('regraDeslocamento', 'FIXO');
                            atualizar('valorDeslocamento', num(e.target.value));
                        }}
                        className={inputClass}
                        inputMode="decimal"
                    />
                </FormField>
                <FormField label="Valor da visita técnica (R$)">
                    <input value={form.valorVisitaTecnica ?? 0} onChange={(e) => atualizar('valorVisitaTecnica', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Validade padrão da proposta (dias)">
                    <input value={form.validadePadraoDias} onChange={(e) => atualizar('validadePadraoDias', Number(e.target.value) || 0)} className={inputClass} inputMode="numeric" />
                </FormField>
                <FormField label="Variação mínima do pré-orçamento (%)">
                    <input value={form.variacaoPreOrcamentoMinPct} onChange={(e) => atualizar('variacaoPreOrcamentoMinPct', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
                <FormField label="Variação máxima do pré-orçamento (%)">
                    <input value={form.variacaoPreOrcamentoMaxPct} onChange={(e) => atualizar('variacaoPreOrcamentoMaxPct', num(e.target.value))} className={inputClass} inputMode="decimal" />
                </FormField>
            </Secao>

            <Secao
                titulo="Taxa do cartão por parcela"
                descricao="Taxa da maquininha em cada opção de parcelamento. Deixe vazio as parcelas que você não oferece; o orçamento só mostra as preenchidas."
            >
                {PARCELAS.map((parcelas) => (
                    <FormField key={parcelas} label={`${parcelas}x (%)`}>
                        <input
                            value={form.taxaCartaoParcelas?.[String(parcelas)] ?? ''}
                            onChange={(e) => {
                                const taxas = { ...(form.taxaCartaoParcelas ?? {}) };
                                if (e.target.value.trim() === '') {
                                    delete taxas[String(parcelas)];
                                } else {
                                    taxas[String(parcelas)] = num(e.target.value);
                                }
                                atualizar('taxaCartaoParcelas', taxas);
                            }}
                            className={inputClass}
                            inputMode="decimal"
                        />
                    </FormField>
                ))}
            </Secao>

            </fieldset>

            {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}
            {sucesso && <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">Parâmetros salvos.</p>}

            {podeEditar && (
            <div className="flex justify-end">
                <button
                    type="button"
                    onClick={aoSalvar}
                    disabled={mutation.isPending}
                    className="flex items-center gap-2 rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                >
                    {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                    Salvar parâmetros
                </button>
            </div>
            )}

            <TipologiasFolgasPainel podeEditar={podeEditar} />
        </div>
    );
}
