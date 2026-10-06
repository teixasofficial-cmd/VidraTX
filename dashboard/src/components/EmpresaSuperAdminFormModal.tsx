import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { superAdminApi } from '../lib/superAdminApi';
import { ApiRequestError } from '../lib/apiClient';
import type { EmpresaSuperAdmin } from '../types';

interface EmpresaSuperAdminFormModalProps {
    empresa?: EmpresaSuperAdmin;
    onClose: () => void;
    onSaved: () => void;
}

export function EmpresaSuperAdminFormModal({
    empresa,
    onClose,
    onSaved,
}: EmpresaSuperAdminFormModalProps) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const [nome, setNome] = useState(empresa?.nome ?? '');
    const [cnpj, setCnpj] = useState(empresa?.cnpj ?? '');
    const [email, setEmail] = useState(empresa?.email ?? '');
    const [telefone, setTelefone] = useState(empresa?.telefone ?? '');
    const [endereco, setEndereco] = useState(empresa?.endereco ?? '');
    const [senha, setSenha] = useState('');

    const mutation = useMutation({
        mutationFn: () =>
            empresa
                ? superAdminApi.atualizarEmpresa(empresa.id, {
                      nome: nome.trim(),
                      cnpj: cnpj.trim(),
                      email: email.trim(),
                      telefone: telefone.trim(),
                      endereco: endereco.trim(),
                  })
                : superAdminApi.criarEmpresa({
                      nome: nome.trim(),
                      cnpj: cnpj.trim(),
                      email: email.trim(),
                      telefone: telefone.trim(),
                      endereco: endereco.trim(),
                      senha,
                  }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['superadmin-empresas'] });
            queryClient.invalidateQueries({ queryKey: ['superadmin-resumo'] });
            onSaved();
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível salvar a empresa'
            ),
    });

    function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    return (
        <Modal title={empresa ? 'Editar empresa' : 'Nova empresa'} onClose={onClose}>
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
                    <FormField label="CNPJ">
                        <input
                            required
                            value={cnpj}
                            onChange={(e) => setCnpj(e.target.value)}
                            className={inputClass}
                            placeholder="00.000.000/0000-00"
                        />
                    </FormField>
                    <FormField label="Número">
                        <input
                            required
                            value={telefone}
                            onChange={(e) => setTelefone(e.target.value)}
                            className={inputClass}
                            placeholder="(11) 90000-0000"
                        />
                    </FormField>
                </div>

                <FormField label="E-mail (será o login da empresa)">
                    <input
                        required
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className={inputClass}
                    />
                    {empresa && (
                        <p className="mt-1 text-xs text-slate-400">
                            Alterar aqui muda o login que a empresa usa para entrar
                        </p>
                    )}
                </FormField>

                <FormField label="Endereço">
                    <input
                        required
                        value={endereco}
                        onChange={(e) => setEndereco(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                {!empresa && (
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
