import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
    Building2,
    CheckCircle2,
    KeyRound,
    LogOut,
    Pencil,
    Plus,
    Power,
    ShieldCheck,
    Users,
    WifiOff,
    XCircle,
} from 'lucide-react';
import { superAdminApi } from '../lib/superAdminApi';
import { useSuperAdminAuth } from '../lib/superAdminAuth';
import { Spinner } from '../components/Spinner';
import { StatCard } from '../components/StatCard';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { EmpresaSuperAdminFormModal } from '../components/EmpresaSuperAdminFormModal';
import { RedefinirSenhaModal } from '../components/RedefinirSenhaModal';
import { TrocarMinhaSenhaSuperAdminModal } from '../components/TrocarMinhaSenhaSuperAdminModal';
import { ApiRequestError } from '../lib/apiClient';
import { formatarDataHora } from '../lib/formato';
import type { EmpresaSuperAdmin } from '../types';

function formatarData(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR').format(new Date(iso));
}

function StatusWhatsapp({ empresa }: { empresa: EmpresaSuperAdmin }) {

    const base = 'whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-medium';

    if (empresa.whatsappStatus === 'CONECTADO') {
        return <span className={`${base} bg-emerald-50 text-emerald-700`}>Conectado</span>;
    }

    if (empresa.whatsappDesconectadoDesde) {
        return (
            <span className={`${base} bg-rose-50 text-rose-700`}>
                Desconectado desde {formatarDataHora(empresa.whatsappDesconectadoDesde)}
            </span>
        );
    }

    if (empresa.whatsappStatus === 'CONECTANDO') {
        return <span className={`${base} bg-amber-50 text-amber-700`}>Conectando</span>;
    }

    return <span className={`${base} bg-slate-100 text-slate-500`}>Nunca conectado</span>;
}

function formatarCnpj(cnpj: string): string {

    if (cnpj.length !== 14) {
        return cnpj;
    }

    return cnpj.replace(
        /(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/,
        '$1.$2.$3/$4-$5'
    );
}

export function SuperAdminDashboardPage() {

    const { admin, sair } = useSuperAdminAuth();
    const queryClient = useQueryClient();

    const [modalAberto, setModalAberto] = useState<'nova' | EmpresaSuperAdmin | null>(null);
    const [empresaParaSenha, setEmpresaParaSenha] = useState<EmpresaSuperAdmin | null>(null);
    const [trocandoMinhaSenha, setTrocandoMinhaSenha] = useState(false);
    const [erro, setErro] = useState<string | null>(null);

    const { data: resumo } = useQuery({
        queryKey: ['superadmin-resumo'],
        queryFn: superAdminApi.resumo,
    });

    const { data: empresas, isLoading, isError, refetch } = useQuery({
        queryKey: ['superadmin-empresas'],
        queryFn: superAdminApi.empresas,
    });

    const alternarStatusMutation = useMutation({
        mutationFn: (id: number) => superAdminApi.alternarStatusEmpresa(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['superadmin-empresas'] });
            queryClient.invalidateQueries({ queryKey: ['superadmin-resumo'] });
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível alterar o status da empresa'
            ),
    });

    return (
        <div className="min-h-screen bg-slate-100">
            <header className="border-b border-slate-200 bg-white">
                <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
                    <div className="flex items-center gap-3">
                        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-900 text-white">
                            <ShieldCheck className="h-5 w-5" />
                        </div>
                        <div>
                            <h1 className="text-base font-semibold text-slate-900">
                                Painel do super admin
                            </h1>
                            <p className="text-xs text-slate-500">{admin?.email}</p>
                        </div>
                    </div>
                    <div className="flex items-center gap-2">
                        <button
                            type="button"
                            onClick={() => setTrocandoMinhaSenha(true)}
                            className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            <KeyRound className="h-4 w-4" />
                            Trocar minha senha
                        </button>
                        <button
                            type="button"
                            onClick={sair}
                            className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            <LogOut className="h-4 w-4" />
                            Sair
                        </button>
                    </div>
                </div>
            </header>

            <main className="mx-auto max-w-6xl space-y-6 px-4 py-6">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <StatCard
                        label="Empresas cadastradas"
                        value={String(resumo?.totalEmpresas ?? '—')}
                        icon={Building2}
                    />
                    <StatCard
                        label="Ativas"
                        value={String(resumo?.empresasAtivas ?? '—')}
                        icon={CheckCircle2}
                        accent="success"
                    />
                    <StatCard
                        label="Desativadas"
                        value={String(resumo?.empresasInativas ?? '—')}
                        icon={XCircle}
                        accent="danger"
                    />
                    <StatCard
                        label="WhatsApp desconectado"
                        value={String(resumo?.whatsappDesconectados ?? '—')}
                        icon={WifiOff}
                        accent={resumo?.whatsappDesconectados ? 'warning' : 'default'}
                    />
                </div>

                {erro && (
                    <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
                )}

                <div className="flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-slate-900">Empresas</h2>
                    <button
                        type="button"
                        onClick={() => setModalAberto('nova')}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                    >
                        <Plus className="h-4 w-4" />
                        Nova empresa
                    </button>
                </div>

                {isLoading ? (
                    <Spinner />
                ) : isError ? (
                    <ErrorState onRetry={() => refetch()} />
                ) : !empresas || empresas.length === 0 ? (
                    <EmptyState
                        icon={Building2}
                        title="Nenhuma empresa cadastrada"
                        description="Cadastre a primeira empresa para liberar o acesso dela ao painel."
                    />
                ) : (
                    <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
                        <table className="w-full text-left text-sm">
                            <thead className="border-b border-slate-200 bg-slate-50 text-xs font-semibold uppercase tracking-wide text-slate-500">
                                <tr>
                                    <th className="px-4 py-3">Empresa</th>
                                    <th className="px-4 py-3">CNPJ</th>
                                    <th className="px-4 py-3">Login</th>
                                    <th className="px-4 py-3">Usuários</th>
                                    <th className="px-4 py-3">Cadastrada em</th>
                                    <th className="px-4 py-3">Status</th>
                                    <th className="px-4 py-3">WhatsApp</th>
                                    <th className="px-4 py-3" />
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {empresas.map((empresa) => (
                                    <tr key={empresa.id}>
                                        <td className="px-4 py-3">
                                            <p className="font-medium text-slate-900">{empresa.nome}</p>
                                            <p className="text-xs text-slate-400">/{empresa.slug}</p>
                                        </td>
                                        <td className="px-4 py-3 text-slate-600">
                                            {formatarCnpj(empresa.cnpj)}
                                        </td>
                                        <td className="px-4 py-3 text-slate-600">{empresa.email}</td>
                                        <td className="px-4 py-3 text-slate-600">
                                            <span className="inline-flex items-center gap-1">
                                                <Users className="h-3.5 w-3.5 text-slate-400" />
                                                {empresa.totalUsuarios}
                                            </span>
                                        </td>
                                        <td className="px-4 py-3 text-slate-600">
                                            {formatarData(empresa.criadoEm)}
                                        </td>
                                        <td className="px-4 py-3">
                                            <span
                                                className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                                                    empresa.ativa
                                                        ? 'bg-emerald-50 text-emerald-700'
                                                        : 'bg-slate-100 text-slate-500'
                                                }`}
                                            >
                                                {empresa.ativa ? 'Ativa' : 'Desativada'}
                                            </span>
                                        </td>
                                        <td className="px-4 py-3">
                                            <StatusWhatsapp empresa={empresa} />
                                        </td>
                                        <td className="px-4 py-3">
                                            <div className="flex items-center justify-end gap-1">
                                                <button
                                                    type="button"
                                                    onClick={() => setModalAberto(empresa)}
                                                    title="Editar"
                                                    className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                                >
                                                    <Pencil className="h-4 w-4" />
                                                </button>
                                                <button
                                                    type="button"
                                                    onClick={() => setEmpresaParaSenha(empresa)}
                                                    title="Redefinir senha"
                                                    className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                                >
                                                    <KeyRound className="h-4 w-4" />
                                                </button>
                                                <button
                                                    type="button"
                                                    onClick={() => {
                                                        setErro(null);
                                                        alternarStatusMutation.mutate(empresa.id);
                                                    }}
                                                    title={empresa.ativa ? 'Desativar' : 'Ativar'}
                                                    className={`rounded-lg p-2 hover:bg-slate-100 ${
                                                        empresa.ativa
                                                            ? 'text-slate-400 hover:text-rose-600'
                                                            : 'text-slate-400 hover:text-emerald-600'
                                                    }`}
                                                >
                                                    <Power className="h-4 w-4" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>

            {modalAberto && (
                <EmpresaSuperAdminFormModal
                    empresa={modalAberto === 'nova' ? undefined : modalAberto}
                    onClose={() => setModalAberto(null)}
                    onSaved={() => setModalAberto(null)}
                />
            )}

            {empresaParaSenha && (
                <RedefinirSenhaModal
                    empresaId={empresaParaSenha.id}
                    empresaNome={empresaParaSenha.nome}
                    onClose={() => setEmpresaParaSenha(null)}
                    onSaved={() => setEmpresaParaSenha(null)}
                />
            )}

            {trocandoMinhaSenha && (
                <TrocarMinhaSenhaSuperAdminModal
                    onClose={() => setTrocandoMinhaSenha(false)}
                    onSaved={() => setTrocandoMinhaSenha(false)}
                />
            )}
        </div>
    );
}
