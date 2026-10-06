import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Pencil, Plus, Trash2, Wrench } from 'lucide-react';
import { api } from '../lib/api';
import { useAuth } from '../lib/auth';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { ServicoFormModal } from '../components/ServicoFormModal';
import { ApiRequestError } from '../lib/apiClient';
import type { Servico } from '../types';

export function ServicosPage() {

    const queryClient = useQueryClient();
    const { usuario } = useAuth();
    const podeEditar = usuario?.perfil === 'ADMIN' || usuario?.perfil === 'GERENTE';

    const [modalAberto, setModalAberto] = useState(false);
    const [editando, setEditando] = useState<Servico | undefined>(undefined);
    const [erro, setErro] = useState<string | null>(null);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['servicos'],
        queryFn: () => api.servicos(),
    });

    const excluirMutation = useMutation({
        mutationFn: (id: number) => api.excluirServico(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['servicos'] }),
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível excluir o serviço'
            ),
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-900">Serviços</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Catálogo de serviços oferecidos pela empresa.
                    </p>
                </div>
                {podeEditar && (
                    <button
                        type="button"
                        onClick={() => setModalAberto(true)}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                    >
                        <Plus className="h-4 w-4" />
                        Novo serviço
                    </button>
                )}
            </div>

            {erro && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <ErrorState onRetry={() => refetch()} />
            ) : !data || data.length === 0 ? (
                <EmptyState
                    icon={Wrench}
                    title="Nenhum serviço cadastrado"
                    description="Cadastre os serviços que a vidraçaria oferece."
                />
            ) : (
                <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <ul className="divide-y divide-slate-100">
                        {data.map((servico) => (
                            <li
                                key={servico.id}
                                className="flex items-center justify-between gap-3 px-5 py-4"
                            >
                                <div className="min-w-0">
                                    <div className="flex items-center gap-2">
                                        <p className="truncate text-sm font-medium text-slate-900">
                                            {servico.nome}
                                        </p>
                                        {!servico.ativo && (
                                            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500">
                                                Inativo
                                            </span>
                                        )}
                                    </div>
                                    {servico.descricao && (
                                        <p className="mt-0.5 truncate text-sm text-slate-500">
                                            {servico.descricao}
                                        </p>
                                    )}
                                </div>
                                {podeEditar && (
                                    <div className="flex shrink-0 items-center gap-1">
                                        <button
                                            type="button"
                                            onClick={() => setEditando(servico)}
                                            className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                            aria-label="Editar"
                                        >
                                            <Pencil className="h-4 w-4" />
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                if (
                                                    confirm(
                                                        `Excluir o serviço "${servico.nome}"? Esta ação não pode ser desfeita.`
                                                    )
                                                ) {
                                                    setErro(null);
                                                    excluirMutation.mutate(servico.id);
                                                }
                                            }}
                                            className="rounded-lg p-2 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                                            aria-label="Excluir"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {modalAberto && (
                <ServicoFormModal
                    onClose={() => setModalAberto(false)}
                    onSaved={() => setModalAberto(false)}
                />
            )}

            {editando && (
                <ServicoFormModal
                    servico={editando}
                    onClose={() => setEditando(undefined)}
                    onSaved={() => setEditando(undefined)}
                />
            )}
        </div>
    );
}
