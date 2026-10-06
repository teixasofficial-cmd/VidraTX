import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Boxes, Plus } from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { usePodeAlterarPrecos } from '../lib/auth';
import { Spinner } from './Spinner';
import { EmptyState } from './EmptyState';
import { ItemOrcamentoCard } from './ItemOrcamentoCard';
import { ItemOrcamentoFormModal } from './ItemOrcamentoFormModal';
import { ResumoFinanceiroPainel } from './ResumoFinanceiroPainel';
import type { OrcamentoItem } from '../types';

interface CalculadoraOrcamentoProps {
    orcamentoId: number;
    editavel: boolean;
}

export function CalculadoraOrcamento({ orcamentoId, editavel }: CalculadoraOrcamentoProps) {

    const queryClient = useQueryClient();
    const podeAlterarPrecos = usePodeAlterarPrecos();
    const [modalItem, setModalItem] = useState<'novo' | OrcamentoItem | null>(null);
    const [erro, setErro] = useState<string | null>(null);

    const { data: itens, isLoading: carregandoItens } = useQuery({
        queryKey: ['orcamento-itens', orcamentoId],
        queryFn: () => api.orcamentoItens(orcamentoId),
    });

    const { data: totais, isLoading: carregandoTotais } = useQuery({
        queryKey: ['orcamento-totais', orcamentoId],
        queryFn: () => api.totaisOrcamento(orcamentoId),
    });

    const { data: parametros } = useQuery({
        queryKey: ['parametros-calculo'],
        queryFn: api.parametrosCalculo,
    });

    function invalidarTudo() {
        setErro(null);
        queryClient.invalidateQueries({ queryKey: ['orcamento-itens', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamento-totais', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamento', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamentos'] });
    }

    function aoFalhar(excecao: unknown) {
        setErro(excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível salvar a alteração');
        queryClient.invalidateQueries({ queryKey: ['orcamento-itens', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamento-totais', orcamentoId] });
    }

    const ajustarLinhaMutation = useMutation({
        mutationFn: ({ itemId, linhaId, valorFinal }: { itemId: number; linhaId: number; valorFinal: number | null }) =>
            api.ajustarLinhaOrcamento(orcamentoId, itemId, linhaId, valorFinal),
        onSuccess: invalidarTudo,
        onError: aoFalhar,
    });

    const removerItemMutation = useMutation({
        mutationFn: (itemId: number) => api.excluirOrcamentoItem(orcamentoId, itemId),
        onSuccess: invalidarTudo,
        onError: aoFalhar,
    });

    const aceitarTodasMutation = useMutation({
        mutationFn: () => api.aceitarTodasSugestoes(orcamentoId),
        onSuccess: invalidarTudo,
        onError: aoFalhar,
    });

    const ajustarPrecoFinalMutation = useMutation({
        mutationFn: (valor: number | null) => api.ajustarPrecoFinalOrcamento(orcamentoId, valor),
        onSuccess: invalidarTudo,
        onError: aoFalhar,
    });

    const definirParcelasMutation = useMutation({
        mutationFn: (parcelas: number | null) => api.definirParcelasOrcamento(orcamentoId, parcelas),
        onSuccess: invalidarTudo,
        onError: aoFalhar,
    });

    if (carregandoItens || carregandoTotais || !totais) {
        return <Spinner />;
    }

    return (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
            <div className="space-y-4">
                {erro && (
                    <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
                )}
                <div className="flex items-center justify-between">
                    <h2 className="text-sm font-semibold text-slate-900">Itens e cálculo</h2>
                    {editavel && (
                        <button
                            type="button"
                            onClick={() => setModalItem('novo')}
                            className="flex items-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-indigo-500"
                        >
                            <Plus className="h-3.5 w-3.5" />
                            Adicionar item
                        </button>
                    )}
                </div>

                {!itens || itens.length === 0 ? (
                    <EmptyState
                        icon={Boxes}
                        title="Nenhum item ainda"
                        description="Adicione um box, janela, espelho ou item livre pra começar a calcular."
                        action={editavel ? { label: 'Adicionar item', onClick: () => setModalItem('novo') } : undefined}
                    />
                ) : (
                    itens.map((item) => (
                        <ItemOrcamentoCard
                            key={item.id}
                            item={item}
                            editavel={editavel}
                            podeAlterarPrecos={podeAlterarPrecos}
                            onAjustarLinha={(linhaId, valorFinal) =>
                                ajustarLinhaMutation.mutate({ itemId: item.id, linhaId, valorFinal })
                            }
                            onEditar={() => setModalItem(item)}
                            onRemover={() => {
                                if (confirm('Remover este item do orçamento?')) {
                                    removerItemMutation.mutate(item.id);
                                }
                            }}
                        />
                    ))
                )}
            </div>

            <ResumoFinanceiroPainel
                totais={totais}
                parametros={parametros}
                editavel={editavel}
                podeAlterarPrecos={podeAlterarPrecos}
                onAceitarTodas={() => aceitarTodasMutation.mutate()}
                aceitandoTodas={aceitarTodasMutation.isPending}
                onAjustarPrecoFinal={(valor) => ajustarPrecoFinalMutation.mutate(valor)}
                ajustandoPrecoFinal={ajustarPrecoFinalMutation.isPending}
                onDefinirParcelas={(parcelas) => definirParcelasMutation.mutate(parcelas)}
                definindoParcelas={definirParcelasMutation.isPending}
            />

            {modalItem && (
                <ItemOrcamentoFormModal
                    orcamentoId={orcamentoId}
                    item={modalItem === 'novo' ? undefined : modalItem}
                    onClose={() => setModalItem(null)}
                    onSaved={() => setModalItem(null)}
                />
            )}
        </div>
    );
}
