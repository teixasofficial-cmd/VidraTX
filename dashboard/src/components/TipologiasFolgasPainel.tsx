import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Check, Loader2 } from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { inputClass } from './FormField';
import { Spinner } from './Spinner';
import { ErrorState } from './ErrorState';
import type { Tipologia, TipologiaAjustePayload } from '../types';

export function TipologiasFolgasPainel({ podeEditar }: { podeEditar: boolean }) {

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['tipologias', 'todas'],
        queryFn: () => api.tipologias(false),
    });

    if (isError) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    return (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-sm font-semibold text-slate-900">Folgas e transpasse das tipologias</h2>
            <p className="mt-0.5 text-xs text-slate-500">
                Folga: quanto o kit tira da largura e da altura do vão. Transpasse: quanto as folhas de correr se sobrepõem.
                A peça de corte sai de (vão − folga + transpasse) ÷ número de folhas.
            </p>
            {isLoading || !data ? (
                <Spinner />
            ) : (
                <div className="mt-4 divide-y divide-slate-100">
                    {data.map((tipologia) => (
                        <LinhaTipologia key={tipologia.id} tipologia={tipologia} podeEditar={podeEditar} />
                    ))}
                </div>
            )}
        </div>
    );
}

function LinhaTipologia({ tipologia, podeEditar }: { tipologia: Tipologia; podeEditar: boolean }) {

    const queryClient = useQueryClient();
    const [form, setForm] = useState<TipologiaAjustePayload>(() => valores(tipologia));
    const [erro, setErro] = useState<string | null>(null);
    const [salvo, setSalvo] = useState(false);

    useEffect(() => setForm(valores(tipologia)), [tipologia]);

    const mutation = useMutation({
        mutationFn: () => api.ajustarFolgasTipologia(tipologia.id, form),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['tipologias'] });
            setSalvo(true);
            setTimeout(() => setSalvo(false), 3000);
        },
        onError: (excecao) =>
            setErro(excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível salvar'),
    });

    const alterado =
        form.descontoLarguraMm !== tipologia.descontoLarguraMm
        || form.descontoAlturaMm !== tipologia.descontoAlturaMm
        || form.transpasseMm !== tipologia.transpasseMm;

    function campo(nome: keyof Omit<TipologiaAjustePayload, 'ativo'>, rotulo: string) {
        return (
            <label className="block">
                <span className="mb-1 block text-xs text-slate-500">{rotulo}</span>
                <input
                    value={form[nome]}
                    onChange={(e) => setForm((atual) => ({ ...atual, [nome]: Number(e.target.value.replace(/\D/g, '')) || 0 }))}
                    className={inputClass}
                    inputMode="numeric"
                    disabled={!podeEditar}
                />
            </label>
        );
    }

    return (
        <div className="py-4">
            <p className="text-sm font-medium text-slate-900">
                {tipologia.nome}
                {!tipologia.ativo && <span className="ml-2 text-xs text-slate-400">(desativada)</span>}
            </p>
            <div className="mt-2 grid grid-cols-3 gap-3 sm:grid-cols-4 sm:items-end">
                {campo('descontoLarguraMm', 'Folga largura (mm)')}
                {campo('descontoAlturaMm', 'Folga altura (mm)')}
                {campo('transpasseMm', 'Transpasse (mm)')}
                {podeEditar && (
                    <button
                        type="button"
                        onClick={() => {
                            setErro(null);
                            mutation.mutate();
                        }}
                        disabled={!alterado || mutation.isPending}
                        className="col-span-3 flex min-h-11 items-center justify-center gap-2 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50 sm:col-span-1"
                    >
                        {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
                        {salvo ? 'Salvo' : 'Salvar'}
                    </button>
                )}
            </div>
            {erro && <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}
        </div>
    );
}

function valores(tipologia: Tipologia): TipologiaAjustePayload {
    return {
        descontoLarguraMm: tipologia.descontoLarguraMm,
        descontoAlturaMm: tipologia.descontoAlturaMm,
        transpasseMm: tipologia.transpasseMm,
        ativo: tipologia.ativo,
    };
}
