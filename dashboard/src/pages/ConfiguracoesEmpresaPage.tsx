import { useEffect, useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2, Save } from 'lucide-react';
import { api } from '../lib/api';
import { useAuth } from '../lib/auth';
import { Spinner } from '../components/Spinner';
import { ErrorState } from '../components/ErrorState';
import { FormField, inputClass } from '../components/FormField';
import { AgendaEmpresaPainel } from '../components/AgendaEmpresaPainel';
import { ApiRequestError } from '../lib/apiClient';
import type { MinhaEmpresaPayload } from '../types';

const campoInicial: MinhaEmpresaPayload = {
    razaoSocial: '',
    nomeFantasia: '',
    cnpj: '',
    email: '',
    telefone: '',
    whatsapp: '',
    logoUrl: '',
    corPrimaria: '',
    corSecundaria: '',
    endereco: '',
    sobre: '',
};

export function ConfiguracoesEmpresaPage() {

    const queryClient = useQueryClient();
    const { usuario } = useAuth();
    const podeEditar = usuario?.perfil === 'ADMIN';

    const [erro, setErro] = useState<string | null>(null);
    const [sucesso, setSucesso] = useState(false);
    const [form, setForm] = useState<MinhaEmpresaPayload>(campoInicial);

    const { data: empresa, isLoading, isError, refetch } = useQuery({
        queryKey: ['minha-empresa'],
        queryFn: () => api.minhaEmpresa(),
    });

    useEffect(() => {
        if (empresa) {
            setForm({
                razaoSocial: empresa.razaoSocial,
                nomeFantasia: empresa.nomeFantasia,
                cnpj: empresa.cnpj,
                email: empresa.email ?? '',
                telefone: empresa.telefone ?? '',
                whatsapp: empresa.whatsapp ?? '',
                logoUrl: empresa.logoUrl ?? '',
                corPrimaria: empresa.corPrimaria ?? '',
                corSecundaria: empresa.corSecundaria ?? '',
                endereco: empresa.endereco ?? '',
                sobre: empresa.sobre ?? '',
            });
        }
    }, [empresa]);

    const mutation = useMutation({
        mutationFn: () =>
            api.atualizarMinhaEmpresa({
                razaoSocial: form.razaoSocial.trim(),
                nomeFantasia: form.nomeFantasia.trim(),
                cnpj: form.cnpj.trim(),
                email: form.email?.trim() || undefined,
                telefone: form.telefone?.trim() || undefined,
                whatsapp: form.whatsapp?.trim() || undefined,
                logoUrl: form.logoUrl?.trim() || undefined,
                corPrimaria: form.corPrimaria?.trim() || undefined,
                corSecundaria: form.corSecundaria?.trim() || undefined,
                endereco: form.endereco?.trim() || undefined,
                sobre: form.sobre?.trim() || undefined,
            }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['minha-empresa'] });
            setSucesso(true);
            setTimeout(() => setSucesso(false), 3000);
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível salvar os dados da empresa'
            ),
    });

    function campo<K extends keyof MinhaEmpresaPayload>(chave: K, valor: string) {
        setForm((atual) => ({ ...atual, [chave]: valor }));
    }

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    if (isLoading) {
        return <Spinner />;
    }

    if (isError || !empresa) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    return (
        <div className="max-w-2xl space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-slate-900">Empresa</h1>
                <p className="mt-1 text-sm text-slate-500">
                    Dados que aparecem para os clientes e identificam a vidraçaria no sistema.
                </p>
            </div>

            <div className="flex flex-wrap gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div>
                    <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                        Endereço de acesso
                    </p>
                    <p className="mt-0.5 text-sm text-slate-800">/{empresa.slug}</p>
                </div>
                <div>
                    <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                        Situação
                    </p>
                    <p className="mt-0.5 text-sm text-slate-800">
                        {empresa.ativa ? 'Ativa' : 'Inativa'}
                    </p>
                </div>
            </div>

            <form onSubmit={aoSubmeter} className="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField label="Razão social">
                        <input
                            required
                            disabled={!podeEditar}
                            value={form.razaoSocial}
                            onChange={(e) => campo('razaoSocial', e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                    <FormField label="Nome fantasia">
                        <input
                            required
                            disabled={!podeEditar}
                            value={form.nomeFantasia}
                            onChange={(e) => campo('nomeFantasia', e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                </div>

                <FormField label="CNPJ">
                    <input
                        required
                        disabled={!podeEditar}
                        value={form.cnpj}
                        onChange={(e) => campo('cnpj', e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField label="E-mail">
                        <input
                            type="email"
                            disabled={!podeEditar}
                            value={form.email}
                            onChange={(e) => campo('email', e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                    <FormField label="Telefone">
                        <input
                            disabled={!podeEditar}
                            value={form.telefone}
                            onChange={(e) => campo('telefone', e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                </div>

                <FormField label="WhatsApp">
                    <input
                        disabled={!podeEditar}
                        value={form.whatsapp}
                        onChange={(e) => campo('whatsapp', e.target.value)}
                        className={inputClass}
                        placeholder="(11) 90000-0000"
                    />
                </FormField>

                <FormField label="Endereço">
                    <input
                        disabled={!podeEditar}
                        value={form.endereco}
                        onChange={(e) => campo('endereco', e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                <FormField label="URL do logo">
                    <input
                        disabled={!podeEditar}
                        value={form.logoUrl}
                        onChange={(e) => campo('logoUrl', e.target.value)}
                        className={inputClass}
                        placeholder="https://..."
                    />
                </FormField>

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField label="Cor primária">
                        <input
                            disabled={!podeEditar}
                            value={form.corPrimaria}
                            onChange={(e) => campo('corPrimaria', e.target.value)}
                            className={inputClass}
                            placeholder="#1A73E8"
                        />
                    </FormField>
                    <FormField label="Cor secundária">
                        <input
                            disabled={!podeEditar}
                            value={form.corSecundaria}
                            onChange={(e) => campo('corSecundaria', e.target.value)}
                            className={inputClass}
                            placeholder="#1A73E8"
                        />
                    </FormField>
                </div>

                <FormField label="Sobre a empresa">
                    <textarea
                        disabled={!podeEditar}
                        value={form.sobre}
                        onChange={(e) => campo('sobre', e.target.value)}
                        className={inputClass}
                        rows={3}
                    />
                </FormField>

                {erro && (
                    <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
                )}
                {sucesso && (
                    <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-600">
                        Dados salvos com sucesso.
                    </p>
                )}

                {podeEditar && (
                    <div className="flex justify-end pt-2">
                        <button
                            type="submit"
                            disabled={mutation.isPending}
                            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {mutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                            <Save className="h-4 w-4" />
                            Salvar alterações
                        </button>
                    </div>
                )}
            </form>

            <AgendaEmpresaPainel podeEditar={usuario?.perfil === 'ADMIN' || usuario?.perfil === 'GERENTE'} />
        </div>
    );
}
