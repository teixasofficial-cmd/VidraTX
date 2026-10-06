import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Pencil, Plus, Sparkles, Tags, Trash2 } from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { usePodeAlterarPrecos } from '../lib/auth';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { TabelaPrecoFormModal } from '../components/TabelaPrecoFormModal';
import { ConfiguracaoExpressaWizard } from '../components/ConfiguracaoExpressaWizard';
import type { TabelaPreco } from '../types';

function formatarMoeda(valor: number | null): string {
    if (valor == null) return '—';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
}

function descreverEspecificacao(item: TabelaPreco): string {

    const partes = [item.tipoVidro, item.espessuraMm ? `${item.espessuraMm}mm` : null, item.cor, item.acabamento]
        .filter(Boolean);

    return partes.length > 0 ? partes.join(' · ') : '—';
}

const rotulosOrigem: Record<TabelaPreco['origem'], string> = {
    MANUAL: 'Manual',
    DERIVADO: 'Derivado',
    REFERENCIA_REGIONAL: 'Referência de mercado',
};

export function TabelaPrecosPage() {

    const queryClient = useQueryClient();
    const podeEditar = usePodeAlterarPrecos();
    const [erro, setErro] = useState<string | null>(null);
    const [modalAberto, setModalAberto] = useState<'novo' | 'expressa' | null>(null);
    const [itemEditando, setItemEditando] = useState<TabelaPreco | null>(null);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['tabela-precos'],
        queryFn: () => api.tabelaPrecos(),
    });

    const excluirMutation = useMutation({
        mutationFn: (id: number) => api.excluirTabelaPreco(id),
        onSuccess: () => {
            setErro(null);
            queryClient.invalidateQueries({ queryKey: ['tabela-precos'] });
        },
        onError: (e) => setErro(e instanceof ApiRequestError ? e.message : 'Não foi possível remover o item'),
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-900">Tabela de Preços</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Vidro, ferragem, kit, mão de obra e deslocamento usados pela calculadora.
                    </p>
                </div>
                {podeEditar && (
                <div className="flex items-center gap-2">
                    <button
                        type="button"
                        onClick={() => setModalAberto('expressa')}
                        className="flex items-center gap-2 rounded-lg border border-indigo-200 bg-indigo-50 px-4 py-2 text-sm font-semibold text-indigo-700 hover:bg-indigo-100"
                    >
                        <Sparkles className="h-4 w-4" />
                        Configuração Expressa
                    </button>
                    <button
                        type="button"
                        onClick={() => setModalAberto('novo')}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                    >
                        <Plus className="h-4 w-4" />
                        Novo item
                    </button>
                </div>
                )}
            </div>

            {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <ErrorState onRetry={() => refetch()} />
            ) : !data || data.length === 0 ? (
                <EmptyState
                    icon={Tags}
                    title="Sua tabela de preços está vazia"
                    description="Use a Configuração Expressa pra gerar tudo em poucos minutos, ou adicione os itens manualmente."
                    action={podeEditar
                        ? { label: 'Começar Configuração Expressa', onClick: () => setModalAberto('expressa') }
                        : undefined}
                />
            ) : (
                <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <table className="min-w-full divide-y divide-slate-100 text-sm">
                        <thead className="bg-slate-50">
                            <tr className="text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                                <th className="px-4 py-3">Descrição</th>
                                <th className="px-4 py-3">Especificação</th>
                                <th className="px-4 py-3">Unidade</th>
                                <th className="px-4 py-3">Custo</th>
                                <th className="px-4 py-3">Preço de venda</th>
                                <th className="px-4 py-3">Origem</th>
                                <th className="px-4 py-3" />
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {data.map((item) => (
                                <tr key={item.id} className={item.ativo ? '' : 'opacity-50'}>
                                    <td className="px-4 py-3 font-medium text-slate-900">{item.descricao}</td>
                                    <td className="px-4 py-3 text-slate-500">{descreverEspecificacao(item)}</td>
                                    <td className="px-4 py-3 text-slate-500">{item.unidade}</td>
                                    <td className="px-4 py-3 text-slate-500">{formatarMoeda(item.custo)}</td>
                                    <td className="px-4 py-3 font-semibold text-slate-900">
                                        <span
                                            className={item.precoAbaixoDoCusto ? 'text-rose-600' : undefined}
                                            title={
                                                item.precoAbaixoDoCusto
                                                    ? 'Preço de venda abaixo do custo cadastrado'
                                                    : undefined
                                            }
                                        >
                                            {formatarMoeda(item.precoVenda)}
                                            {item.precoAbaixoDoCusto ? ' ⚠' : ''}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3">
                                        <span
                                            title={item.formulaOrigem ?? undefined}
                                            className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                                                item.origem === 'MANUAL'
                                                    ? 'bg-slate-100 text-slate-600'
                                                    : 'bg-indigo-50 text-indigo-600'
                                            }`}
                                        >
                                            {rotulosOrigem[item.origem]}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3">
                                        {podeEditar && (
                                        <div className="flex justify-end gap-1">
                                            <button
                                                type="button"
                                                onClick={() => setItemEditando(item)}
                                                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                                aria-label="Editar"
                                            >
                                                <Pencil className="h-4 w-4" />
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => {
                                                    if (confirm(`Remover "${item.descricao}" da tabela?`)) {
                                                        excluirMutation.mutate(item.id);
                                                    }
                                                }}
                                                className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                                                aria-label="Remover"
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </button>
                                        </div>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {modalAberto === 'novo' && (
                <TabelaPrecoFormModal onClose={() => setModalAberto(null)} onSaved={() => setModalAberto(null)} />
            )}

            {itemEditando && (
                <TabelaPrecoFormModal
                    item={itemEditando}
                    onClose={() => setItemEditando(null)}
                    onSaved={() => setItemEditando(null)}
                />
            )}

            {modalAberto === 'expressa' && (
                <ConfiguracaoExpressaWizard
                    onClose={() => setModalAberto(null)}
                    onConcluido={() => setModalAberto(null)}
                />
            )}
        </div>
    );
}
