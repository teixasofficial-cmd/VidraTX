import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { Columns3, DollarSign, FileText, List, Percent, Plus } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { StatCard } from '../components/StatCard';
import { StatusOrcamentoBadge } from '../components/StatusOrcamentoBadge';
import { OrcamentoFormModal } from '../components/OrcamentoFormModal';
import { KanbanBoard } from '../components/KanbanBoard';
import { MotivoPerdaModal } from '../components/MotivoPerdaModal';
import type { MotivoPerda, Orcamento, StatusOrcamento } from '../types';

function formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
}

function formatarData(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(
        new Date(iso)
    );
}

const STATUS_EM_ABERTO: StatusOrcamento[] = [
    'NOVO_CONTATO', 'PRE_ORCAMENTO', 'VISITA_AGENDADA', 'MEDIDO', 'ORCAMENTO_FINAL',
];

const filtrosLista: { rotulo: string; status: StatusOrcamento | undefined }[] = [
    { rotulo: 'Todos', status: undefined },
    { rotulo: 'Novo contato', status: 'NOVO_CONTATO' },
    { rotulo: 'Pré-orçamento', status: 'PRE_ORCAMENTO' },
    { rotulo: 'Visita agendada', status: 'VISITA_AGENDADA' },
    { rotulo: 'Medido', status: 'MEDIDO' },
    { rotulo: 'Orçamento final', status: 'ORCAMENTO_FINAL' },
    { rotulo: 'Enviados', status: 'ENVIADO' },
    { rotulo: 'Aprovados', status: 'APROVADO' },
    { rotulo: 'Perdidos', status: 'PERDIDO' },
];

export function OrcamentosPipelinePage() {

    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [visao, setVisao] = useState<'kanban' | 'lista'>('kanban');
    const [statusFiltro, setStatusFiltro] = useState<StatusOrcamento | undefined>(undefined);
    const [modalAberto, setModalAberto] = useState(false);
    const [movimentoPendente, setMovimentoPendente] = useState<{ orcamento: Orcamento; novoStatus: StatusOrcamento } | null>(null);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['orcamentos', { status: undefined }],
        queryFn: () => api.orcamentos({}),
    });

    const moverMutation = useMutation({
        mutationFn: ({ id, status, motivoPerda, motivoPerdaOutro }: {
            id: number; status: StatusOrcamento; motivoPerda?: MotivoPerda; motivoPerdaOutro?: string;
        }) => api.moverPipelineOrcamento(id, { status, motivoPerda, motivoPerdaOutro }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['orcamentos'] });
            setMovimentoPendente(null);
        },
    });

    function aoMoverCard(orcamento: Orcamento, novoStatus: StatusOrcamento) {

        if (novoStatus === 'PERDIDO') {
            setMovimentoPendente({ orcamento, novoStatus });
            return;
        }

        moverMutation.mutate({ id: orcamento.id, status: novoStatus });
    }

    const indicadores = useMemo(() => {

        const lista = data ?? [];
        const emAberto = lista.filter((o) => STATUS_EM_ABERTO.includes(o.status) || o.status === 'ENVIADO');
        const valorEmNegociacao = emAberto.reduce((soma, o) => soma + o.valorTotal, 0);
        const finalizados = lista.filter((o) => o.status === 'APROVADO' || o.status === 'PERDIDO');
        const aprovados = lista.filter((o) => o.status === 'APROVADO').length;
        const taxaConversao = finalizados.length > 0 ? (aprovados / finalizados.length) * 100 : 0;

        return {
            totalEmAberto: emAberto.length,
            valorEmNegociacao,
            taxaConversao,
        };
    }, [data]);

    const listaFiltrada = useMemo(() => {

        if (!data) return [];
        if (!statusFiltro) return data;

        return data.filter((o) => o.status === statusFiltro);
    }, [data, statusFiltro]);

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-900">Orçamentos</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Do primeiro contato até aprovado ou perdido.
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <div className="flex items-center gap-1 rounded-lg border border-slate-200 bg-white p-1">
                        <button
                            type="button"
                            onClick={() => setVisao('kanban')}
                            className={`flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium ${
                                visao === 'kanban' ? 'bg-indigo-600 text-white' : 'text-slate-500 hover:bg-slate-50'
                            }`}
                        >
                            <Columns3 className="h-4 w-4" />
                            Kanban
                        </button>
                        <button
                            type="button"
                            onClick={() => setVisao('lista')}
                            className={`flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium ${
                                visao === 'lista' ? 'bg-indigo-600 text-white' : 'text-slate-500 hover:bg-slate-50'
                            }`}
                        >
                            <List className="h-4 w-4" />
                            Lista
                        </button>
                    </div>
                    <button
                        type="button"
                        onClick={() => setModalAberto(true)}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                    >
                        <Plus className="h-4 w-4" />
                        Novo orçamento
                    </button>
                </div>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <StatCard
                    label="Orçamentos em aberto"
                    value={String(indicadores.totalEmAberto)}
                    icon={FileText}
                />
                <StatCard
                    label="Valor em negociação"
                    value={formatarMoeda(indicadores.valorEmNegociacao)}
                    icon={DollarSign}
                    accent="warning"
                />
                <StatCard
                    label="Taxa de conversão"
                    value={`${indicadores.taxaConversao.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`}
                    icon={Percent}
                    accent="success"
                />
            </div>

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <ErrorState onRetry={() => refetch()} />
            ) : !data || data.length === 0 ? (
                <EmptyState
                    icon={FileText}
                    title="Nenhum orçamento ainda"
                    description="Assim que um cliente chamar no WhatsApp ou você criar um orçamento manualmente, ele aparece aqui."
                />
            ) : visao === 'kanban' ? (
                <KanbanBoard orcamentos={data} onMover={aoMoverCard} />
            ) : (
                <div className="space-y-3">
                    <div className="flex flex-wrap gap-2">
                        {filtrosLista.map((filtro) => (
                            <button
                                key={filtro.rotulo}
                                type="button"
                                onClick={() => setStatusFiltro(filtro.status)}
                                className={`rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${
                                    statusFiltro === filtro.status
                                        ? 'bg-indigo-600 text-white'
                                        : 'bg-white text-slate-600 ring-1 ring-inset ring-slate-200 hover:bg-slate-50'
                                }`}
                            >
                                {filtro.rotulo}
                            </button>
                        ))}
                    </div>

                    <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                        <ul className="divide-y divide-slate-100">
                            {listaFiltrada.map((orcamento) => (
                                <li key={orcamento.id}>
                                    <Link
                                        to={`/orcamentos/${orcamento.id}`}
                                        className="flex items-center justify-between px-5 py-4 transition-colors hover:bg-slate-50"
                                    >
                                        <div>
                                            <div className="flex items-center gap-2">
                                                <span className="text-sm font-medium text-slate-900">
                                                    #{orcamento.id} · {orcamento.clienteNome}
                                                </span>
                                                <StatusOrcamentoBadge status={orcamento.status} />
                                            </div>
                                            <p className="mt-0.5 text-xs text-slate-400">
                                                Criado em {formatarData(orcamento.criadoEm)}
                                            </p>
                                        </div>
                                        <span className="text-sm font-semibold text-slate-700">
                                            {formatarMoeda(orcamento.valorTotal)}
                                        </span>
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            )}

            {modalAberto && (
                <OrcamentoFormModal
                    onClose={() => setModalAberto(false)}
                    onCreated={(orcamento) => navigate(`/orcamentos/${orcamento.id}`)}
                />
            )}

            {movimentoPendente && (
                <MotivoPerdaModal
                    enviando={moverMutation.isPending}
                    onClose={() => setMovimentoPendente(null)}
                    onConfirmar={(motivoPerda, motivoPerdaOutro) =>
                        moverMutation.mutate({
                            id: movimentoPendente.orcamento.id,
                            status: 'PERDIDO',
                            motivoPerda,
                            motivoPerdaOutro,
                        })
                    }
                />
            )}
        </div>
    );
}
