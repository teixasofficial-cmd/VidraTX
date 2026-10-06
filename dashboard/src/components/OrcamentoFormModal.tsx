import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import type { Orcamento } from '../types';

interface OrcamentoFormModalProps {
    orcamento?: Orcamento;
    clienteIdInicial?: number;
    onClose: () => void;
    onCreated: (orcamento: Orcamento) => void;
}

export function OrcamentoFormModal({
    orcamento,
    clienteIdInicial,
    onClose,
    onCreated,
}: OrcamentoFormModalProps) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);
    const [clienteId, setClienteId] = useState<string>(
        orcamento ? String(orcamento.clienteId) : clienteIdInicial ? String(clienteIdInicial) : ''
    );
    const [observacoes, setObservacoes] = useState(orcamento?.observacoes ?? '');
    const [validoAte, setValidoAte] = useState(orcamento?.validoAte ?? '');

    const { data: clientes, isLoading: carregandoClientes } = useQuery({
        queryKey: ['clientes', ''],
        queryFn: () => api.clientes(),
    });

    const mutation = useMutation({
        mutationFn: () => {
            const payload = {
                clienteId: Number(clienteId),
                observacoes: observacoes.trim() || undefined,
                validoAte: validoAte || undefined,
            };
            return orcamento
                ? api.atualizarOrcamento(orcamento.id, payload)
                : api.criarOrcamento(payload);
        },
        onSuccess: (salvo) => {
            queryClient.invalidateQueries({ queryKey: ['orcamentos'] });
            queryClient.invalidateQueries({ queryKey: ['orcamento', salvo.id] });
            onCreated(salvo);
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível salvar o orçamento'
            ),
    });

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    return (
        <Modal title={orcamento ? 'Editar orçamento' : 'Novo orçamento'} onClose={onClose}>
            <form onSubmit={aoSubmeter} className="space-y-4">
                <FormField label="Cliente">
                    <select
                        required
                        value={clienteId}
                        onChange={(e) => setClienteId(e.target.value)}
                        disabled={carregandoClientes || Boolean(clienteIdInicial) || Boolean(orcamento)}
                        className={inputClass}
                    >
                        <option value="" disabled>
                            Selecione um cliente...
                        </option>
                        {clientes?.map((cliente) => (
                            <option key={cliente.id} value={cliente.id}>
                                {cliente.nome}
                            </option>
                        ))}
                    </select>
                </FormField>

                <FormField label="Válido até (opcional)">
                    <input
                        type="date"
                        value={validoAte}
                        onChange={(e) => setValidoAte(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                <FormField label="Observações (opcional)">
                    <textarea
                        value={observacoes}
                        onChange={(e) => setObservacoes(e.target.value)}
                        className={inputClass}
                        rows={3}
                    />
                </FormField>

                {erro && (
                    <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
                )}

                <div className="flex justify-end gap-2 pt-2">
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                    >
                        Cancelar
                    </button>
                    <button
                        type="submit"
                        disabled={mutation.isPending || !clienteId}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {mutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        {orcamento ? 'Salvar' : 'Criar orçamento'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}
