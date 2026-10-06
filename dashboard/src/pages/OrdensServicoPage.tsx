import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ClipboardList } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { StatusProducaoBadge } from '../components/StatusProducaoBadge';
import type { StatusProducao } from '../types';

function formatarData(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(
        new Date(iso)
    );
}

const filtros: { rotulo: string; status: StatusProducao | undefined }[] = [
    { rotulo: 'Todas', status: undefined },
    { rotulo: 'Aguardando produção', status: 'AGUARDANDO_PRODUCAO' },
    { rotulo: 'Em produção', status: 'EM_PRODUCAO' },
    { rotulo: 'Pronto', status: 'PRONTO' },
    { rotulo: 'Conferido', status: 'CONFERIDO' },
];

export function OrdensServicoPage() {

    const [statusFiltro, setStatusFiltro] = useState<StatusProducao | undefined>(undefined);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['ordens-servico', { status: statusFiltro }],
        queryFn: () => api.ordensServico(statusFiltro),
    });

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-slate-900">Ordens de serviço</h1>
                <p className="mt-1 text-sm text-slate-500">
                    Produção, instalação e pós-venda de cada orçamento aprovado. Uma ordem de
                    serviço é criada a partir da tela do orçamento aprovado.
                </p>
            </div>

            <div className="flex flex-wrap gap-2">
                {filtros.map((filtro) => (
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

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <ErrorState onRetry={() => refetch()} />
            ) : !data || data.length === 0 ? (
                <EmptyState
                    icon={ClipboardList}
                    title="Nenhuma ordem de serviço encontrada"
                    description="Aprove um orçamento e crie uma ordem de serviço a partir dele, ou ajuste o filtro."
                />
            ) : (
                <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <ul className="divide-y divide-slate-100">
                        {data.map((ordem) => (
                            <li key={ordem.id}>
                                <Link
                                    to={`/ordens-servico/${ordem.id}`}
                                    className="flex items-center justify-between px-5 py-4 transition-colors hover:bg-slate-50"
                                >
                                    <div>
                                        <div className="flex items-center gap-2">
                                            <span className="text-sm font-medium text-slate-900">
                                                OS #{ordem.id} · {ordem.clienteNome}
                                            </span>
                                            {ordem.necessitaProducao && ordem.statusProducao ? (
                                                <StatusProducaoBadge status={ordem.statusProducao} />
                                            ) : (
                                                <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-500">
                                                    Sem produção
                                                </span>
                                            )}
                                        </div>
                                        <p className="mt-0.5 text-xs text-slate-400">
                                            Criada em {formatarData(ordem.criadoEm)}
                                        </p>
                                    </div>
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
