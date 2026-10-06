import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CheckCircle2, Loader2, MessageSquare, Plus, ShieldCheck } from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { Spinner } from './Spinner';
import { EmptyState } from './EmptyState';
import { StatusPosVendaBadge } from './StatusPosVendaBadge';
import { FormField, inputClass } from './FormField';
import type { PosVenda } from '../types';

function formatarDataHora(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(
        new Date(iso)
    );
}

export function PosVendaPainel({ ordemServicoId }: { ordemServicoId: number }) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);
    const [abrindoChamado, setAbrindoChamado] = useState(false);
    const [problema, setProblema] = useState('');

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['pos-venda', ordemServicoId],
        queryFn: () => api.posVendas(ordemServicoId),
    });

    function invalidar() {
        queryClient.invalidateQueries({ queryKey: ['pos-venda', ordemServicoId] });
    }

    const abrirMutation = useMutation({
        mutationFn: () => api.abrirPosVenda(ordemServicoId, { problema: problema.trim() }),
        onSuccess: () => {
            invalidar();
            setAbrindoChamado(false);
            setProblema('');
        },
        onError: (e) =>
            setErro(e instanceof ApiRequestError ? e.message : 'Não foi possível abrir o chamado'),
    });

    function aoAbrirChamado(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        abrirMutation.mutate();
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-slate-900">Pós-venda</h2>
                {!abrindoChamado && (
                    <button
                        type="button"
                        onClick={() => setAbrindoChamado(true)}
                        className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
                    >
                        <Plus className="h-4 w-4" />
                        Abrir chamado
                    </button>
                )}
            </div>

            {erro && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}

            {abrindoChamado && (
                <form
                    onSubmit={aoAbrirChamado}
                    className="space-y-3 rounded-2xl border border-slate-200 bg-white p-5"
                >
                    <FormField label="Descrição do problema relatado pelo cliente">
                        <textarea
                            required
                            autoFocus
                            value={problema}
                            onChange={(e) => setProblema(e.target.value)}
                            className={inputClass}
                            rows={3}
                        />
                    </FormField>
                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={() => setAbrindoChamado(false)}
                            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            Cancelar
                        </button>
                        <button
                            type="submit"
                            disabled={abrirMutation.isPending}
                            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {abrirMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                            Abrir chamado
                        </button>
                    </div>
                </form>
            )}

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
                    Não foi possível carregar o pós-venda.
                </p>
            ) : !data || data.length === 0 ? (
                !abrindoChamado && (
                    <EmptyState
                        icon={ShieldCheck}
                        title="Nenhum chamado de pós-venda"
                        description="Se o cliente relatar um problema depois da instalação, registre aqui."
                    />
                )
            ) : (
                <ul className="space-y-3">
                    {data.map((chamado) => (
                        <ItemPosVenda
                            key={chamado.id}
                            chamado={chamado}
                            ordemServicoId={ordemServicoId}
                            onAtualizado={() => {
                                invalidar();
                                refetch();
                            }}
                        />
                    ))}
                </ul>
            )}
        </div>
    );
}

function ItemPosVenda({
    chamado,
    ordemServicoId,
    onAtualizado,
}: {
    chamado: PosVenda;
    ordemServicoId: number;
    onAtualizado: () => void;
}) {

    const [erro, setErro] = useState<string | null>(null);
    const [campo, setCampo] = useState('');
    const [editando, setEditando] = useState<'atendimento' | 'solucao' | null>(null);

    function tratarErro(e: unknown, padrao: string) {
        setErro(e instanceof ApiRequestError ? e.message : padrao);
    }

    const iniciarMutation = useMutation({
        mutationFn: () =>
            api.iniciarAtendimentoPosVenda(ordemServicoId, chamado.id, {
                atendimento: campo.trim(),
            }),
        onSuccess: () => {
            onAtualizado();
            setEditando(null);
            setCampo('');
        },
        onError: (e) => tratarErro(e, 'Não foi possível iniciar o atendimento'),
    });

    const resolverMutation = useMutation({
        mutationFn: () =>
            api.resolverPosVenda(ordemServicoId, chamado.id, { solucao: campo.trim() }),
        onSuccess: () => {
            onAtualizado();
            setEditando(null);
            setCampo('');
        },
        onError: (e) => tratarErro(e, 'Não foi possível registrar a solução'),
    });

    const encerrarMutation = useMutation({
        mutationFn: () => api.encerrarPosVenda(ordemServicoId, chamado.id),
        onSuccess: onAtualizado,
        onError: (e) => tratarErro(e, 'Não foi possível encerrar o chamado'),
    });

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        if (editando === 'atendimento') iniciarMutation.mutate();
        if (editando === 'solucao') resolverMutation.mutate();
    }

    return (
        <li className="space-y-3 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
                <StatusPosVendaBadge status={chamado.status} />
                <span className="text-xs text-slate-400">
                    Aberto em {formatarDataHora(chamado.criadoEm)}
                </span>
            </div>

            <p className="whitespace-pre-wrap text-sm text-slate-700">{chamado.problema}</p>

            {chamado.atendimento && (
                <p className="whitespace-pre-wrap text-sm text-slate-600">
                    <span className="font-medium text-slate-700">Atendimento: </span>
                    {chamado.atendimento}
                </p>
            )}
            {chamado.solucao && (
                <p className="whitespace-pre-wrap text-sm text-slate-600">
                    <span className="font-medium text-slate-700">Solução: </span>
                    {chamado.solucao}
                </p>
            )}

            {erro && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}

            {editando && (
                <form onSubmit={aoSubmeter} className="space-y-2 border-t border-slate-100 pt-3">
                    <FormField
                        label={editando === 'atendimento' ? 'Como o atendimento foi feito' : 'Qual foi a solução'}
                    >
                        <textarea
                            required
                            autoFocus
                            value={campo}
                            onChange={(e) => setCampo(e.target.value)}
                            className={inputClass}
                            rows={2}
                        />
                    </FormField>
                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={() => {
                                setEditando(null);
                                setCampo('');
                            }}
                            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            Cancelar
                        </button>
                        <button
                            type="submit"
                            disabled={iniciarMutation.isPending || resolverMutation.isPending}
                            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {(iniciarMutation.isPending || resolverMutation.isPending) && (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            )}
                            Salvar
                        </button>
                    </div>
                </form>
            )}

            {!editando && (
                <div className="flex flex-wrap gap-2 border-t border-slate-100 pt-3">
                    {chamado.status === 'ABERTA' && (
                        <button
                            type="button"
                            onClick={() => setEditando('atendimento')}
                            className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            <MessageSquare className="h-4 w-4" />
                            Iniciar atendimento
                        </button>
                    )}
                    {chamado.status === 'EM_ATENDIMENTO' && (
                        <button
                            type="button"
                            onClick={() => setEditando('solucao')}
                            className="flex items-center gap-2 rounded-lg border border-emerald-200 px-3 py-1.5 text-sm font-medium text-emerald-700 hover:bg-emerald-50"
                        >
                            <CheckCircle2 className="h-4 w-4" />
                            Registrar solução
                        </button>
                    )}
                    {chamado.status === 'RESOLVIDA' && (
                        <button
                            type="button"
                            onClick={() => {
                                setErro(null);
                                encerrarMutation.mutate();
                            }}
                            disabled={encerrarMutation.isPending}
                            className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {encerrarMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                            Encerrar chamado
                        </button>
                    )}
                </div>
            )}
        </li>
    );
}
