import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import type { Perfil, UsuarioEmpresa } from '../types';

interface UsuarioEmpresaFormModalProps {
    usuario?: UsuarioEmpresa;
    onClose: () => void;
    onSaved: (usuario: UsuarioEmpresa) => void;
}

const perfis: { valor: Perfil; rotulo: string }[] = [
    { valor: 'ADMIN', rotulo: 'Administrador' },
    { valor: 'GERENTE', rotulo: 'Gerente' },
    { valor: 'FUNCIONARIO', rotulo: 'Funcionário' },
];

export function UsuarioEmpresaFormModal({ usuario, onClose, onSaved }: UsuarioEmpresaFormModalProps) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const [nome, setNome] = useState(usuario?.nome ?? '');
    const [email, setEmail] = useState(usuario?.email ?? '');
    const [senha, setSenha] = useState('');
    const [perfil, setPerfil] = useState<Perfil>(usuario?.perfil ?? 'FUNCIONARIO');
    const [ativo, setAtivo] = useState(usuario?.ativo ?? true);

    const mutation = useMutation({
        mutationFn: () =>
            usuario
                ? api.atualizarUsuarioEmpresa(usuario.id, {
                      nome: nome.trim(),
                      email: email.trim(),
                      perfil,
                      ativo,
                  })
                : api.criarUsuarioEmpresa({
                      nome: nome.trim(),
                      email: email.trim(),
                      senha,
                      perfil,
                      ativo,
                  }),
        onSuccess: (salvo) => {
            queryClient.invalidateQueries({ queryKey: ['usuarios-empresa'] });
            onSaved(salvo);
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível salvar o usuário'
            ),
    });

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    return (
        <Modal title={usuario ? 'Editar usuário' : 'Novo usuário'} onClose={onClose}>
            <form onSubmit={aoSubmeter} className="space-y-4">
                <FormField label="Nome">
                    <input
                        required
                        autoFocus
                        value={nome}
                        onChange={(e) => setNome(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                <FormField label="E-mail">
                    <input
                        required
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                {!usuario && (
                    <FormField label="Senha inicial">
                        <input
                            required
                            type="password"
                            minLength={8}
                            value={senha}
                            onChange={(e) => setSenha(e.target.value)}
                            className={inputClass}
                            placeholder="Mínimo 8 caracteres"
                        />
                    </FormField>
                )}

                <FormField label="Perfil">
                    <select
                        value={perfil}
                        onChange={(e) => setPerfil(e.target.value as Perfil)}
                        className={inputClass}
                    >
                        {perfis.map((p) => (
                            <option key={p.valor} value={p.valor}>
                                {p.rotulo}
                            </option>
                        ))}
                    </select>
                </FormField>

                {usuario && (
                    <label className="flex items-center gap-2 text-sm font-medium text-slate-700">
                        <input
                            type="checkbox"
                            checked={ativo}
                            onChange={(e) => setAtivo(e.target.checked)}
                            className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500/20"
                        />
                        Usuário ativo (consegue fazer login)
                    </label>
                )}

                {!usuario && (
                    <p className="text-xs text-slate-400">
                        O usuário pode trocar essa senha depois, pelo ícone de chave no topo da tela.
                    </p>
                )}

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
