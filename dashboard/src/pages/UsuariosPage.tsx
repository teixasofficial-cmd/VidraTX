import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { KeyRound, Pencil, Plus, Trash2, Users } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { UsuarioEmpresaFormModal } from '../components/UsuarioEmpresaFormModal';
import { RedefinirSenhaModal } from '../components/RedefinirSenhaModal';
import { ApiRequestError } from '../lib/apiClient';
import type { UsuarioEmpresa } from '../types';

const rotuloPerfil: Record<UsuarioEmpresa['perfil'], string> = {
    ADMIN: 'Administrador',
    GERENTE: 'Gerente',
    FUNCIONARIO: 'Funcionário',
};

export function UsuariosPage() {

    const queryClient = useQueryClient();
    const [modalAberto, setModalAberto] = useState(false);
    const [editando, setEditando] = useState<UsuarioEmpresa | undefined>(undefined);
    const [erro, setErro] = useState<string | null>(null);
    const [trocandoSenha, setTrocandoSenha] = useState<UsuarioEmpresa | null>(null);
    const [senhaRedefinida, setSenhaRedefinida] = useState<string | null>(null);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['usuarios-empresa'],
        queryFn: () => api.usuariosEmpresa(),
    });

    const excluirMutation = useMutation({
        mutationFn: (id: number) => api.excluirUsuarioEmpresa(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios-empresa'] }),
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível excluir o usuário'
            ),
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-900">Usuários</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Contas que acessam o painel desta empresa.
                    </p>
                </div>
                <button
                    type="button"
                    onClick={() => setModalAberto(true)}
                    className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                >
                    <Plus className="h-4 w-4" />
                    Novo usuário
                </button>
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
                    icon={Users}
                    title="Nenhum usuário cadastrado"
                    description="Cadastre as pessoas que vão acessar o painel da empresa."
                />
            ) : (
                <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <ul className="divide-y divide-slate-100">
                        {data.map((usuario) => (
                            <li
                                key={usuario.id}
                                className="flex items-center justify-between gap-3 px-5 py-4"
                            >
                                <div className="flex min-w-0 items-center gap-3">
                                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-indigo-100 text-sm font-semibold text-indigo-700">
                                        {usuario.nome.charAt(0).toUpperCase()}
                                    </div>
                                    <div className="min-w-0">
                                        <div className="flex items-center gap-2">
                                            <p className="truncate text-sm font-medium text-slate-900">
                                                {usuario.nome}
                                            </p>
                                            {!usuario.ativo && (
                                                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500">
                                                    Inativo
                                                </span>
                                            )}
                                        </div>
                                        <p className="truncate text-sm text-slate-500">
                                            {usuario.email} · {rotuloPerfil[usuario.perfil]}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex shrink-0 items-center gap-1">
                                    <button
                                        type="button"
                                        onClick={() => { setSenhaRedefinida(null); setTrocandoSenha(usuario); }}
                                        className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                        aria-label="Redefinir senha"
                                        title="Redefinir senha (quem esqueceu a senha)"
                                    >
                                        <KeyRound className="h-4 w-4" />
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setEditando(usuario)}
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
                                                    `Excluir o usuário "${usuario.nome}"? Esta ação não pode ser desfeita.`
                                                )
                                            ) {
                                                setErro(null);
                                                excluirMutation.mutate(usuario.id);
                                            }
                                        }}
                                        className="rounded-lg p-2 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                                        aria-label="Excluir"
                                    >
                                        <Trash2 className="h-4 w-4" />
                                    </button>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {trocandoSenha && (
                <RedefinirSenhaModal
                    empresaNome={trocandoSenha.nome}
                    redefinir={(senha) => api.redefinirSenhaUsuario(trocandoSenha.id, senha)}
                    onClose={() => setTrocandoSenha(null)}
                    onSaved={() => {
                        setSenhaRedefinida(trocandoSenha.nome);
                        setTrocandoSenha(null);
                    }}
                />
            )}

            {senhaRedefinida && (
                <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                    Senha de {senhaRedefinida} redefinida. Passe a senha nova para a pessoa; ela pode trocar depois
                    pelo ícone de chave no topo da tela.
                </p>
            )}

            {modalAberto && (
                <UsuarioEmpresaFormModal
                    onClose={() => setModalAberto(false)}
                    onSaved={() => setModalAberto(false)}
                />
            )}

            {editando && (
                <UsuarioEmpresaFormModal
                    usuario={editando}
                    onClose={() => setEditando(undefined)}
                    onSaved={() => setEditando(undefined)}
                />
            )}
        </div>
    );
}
