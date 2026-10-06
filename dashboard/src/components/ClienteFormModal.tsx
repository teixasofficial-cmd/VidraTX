import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import type { Cliente, ClientePayload } from '../types';

interface ClienteFormModalProps {
    cliente?: Cliente;
    onClose: () => void;
    onSaved: (cliente: Cliente) => void;
}

export function ClienteFormModal({ cliente, onClose, onSaved }: ClienteFormModalProps) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const [nome, setNome] = useState(cliente?.nome ?? '');
    const [telefone, setTelefone] = useState(cliente?.telefone ?? '');
    const [whatsapp, setWhatsapp] = useState(cliente?.whatsapp ?? '');
    const [email, setEmail] = useState(cliente?.email ?? '');
    const [cpfCnpj, setCpfCnpj] = useState(cliente?.cpfCnpj ?? '');
    const [endereco, setEndereco] = useState(cliente?.endereco ?? '');
    const [observacoes, setObservacoes] = useState(cliente?.observacoes ?? '');

    const mutation = useMutation({
        mutationFn: (payload: ClientePayload) =>
            cliente
                ? api.atualizarCliente(cliente.id, payload)
                : api.criarCliente(payload),
        onSuccess: (salvo) => {
            queryClient.invalidateQueries({ queryKey: ['clientes'] });
            onSaved(salvo);
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível salvar o cliente'
            ),
    });

    function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);

        const payload: ClientePayload = {
            nome: nome.trim(),
            telefone: telefone.trim() || undefined,
            whatsapp: whatsapp.trim() || undefined,
            email: email.trim() || undefined,
            cpfCnpj: cpfCnpj.trim() || undefined,
            endereco: endereco.trim() || undefined,
            observacoes: observacoes.trim() || undefined,
        };

        mutation.mutate(payload);
    }

    return (
        <Modal title={cliente ? 'Editar cliente' : 'Novo cliente'} onClose={onClose}>
            <form onSubmit={aoSubmeter} className="space-y-4">
                <FormField label="Nome">
                    <input
                        required
                        value={nome}
                        onChange={(e) => setNome(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                <div className="grid grid-cols-2 gap-4">
                    <FormField label="Telefone">
                        <input
                            value={telefone}
                            onChange={(e) => setTelefone(e.target.value)}
                            className={inputClass}
                            placeholder="(11) 90000-0000"
                        />
                    </FormField>
                    <FormField label="WhatsApp">
                        <input
                            value={whatsapp}
                            onChange={(e) => setWhatsapp(e.target.value)}
                            className={inputClass}
                            placeholder="(11) 90000-0000 ou +351 912 345 678"
                        />
                        <span className="mt-1 block text-xs text-slate-400">
                            Com DDD. Orçamentos e agendamentos vão para este número.
                        </span>
                    </FormField>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <FormField label="E-mail">
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                    <FormField label="CPF/CNPJ">
                        <input
                            value={cpfCnpj}
                            onChange={(e) => setCpfCnpj(e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                </div>

                <FormField label="Endereço">
                    <input
                        value={endereco}
                        onChange={(e) => setEndereco(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                <FormField label="Observações">
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
                        disabled={mutation.isPending}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {mutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        Salvar
                    </button>
                </div>
            </form>
        </Modal>
    );
}
