import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { superAdminApi } from '../lib/superAdminApi';
import { ApiRequestError } from '../lib/apiClient';

interface RedefinirSenhaModalProps {
    empresaId?: number;
    empresaNome: string;
    redefinir?: (senha: string) => Promise<unknown>;
    onClose: () => void;
    onSaved: () => void;
}

export function RedefinirSenhaModal({
    empresaId,
    empresaNome,
    redefinir,
    onClose,
    onSaved,
}: RedefinirSenhaModalProps) {

    const [senha, setSenha] = useState('');
    const [erro, setErro] = useState<string | null>(null);

    const mutation = useMutation({
        mutationFn: () => (redefinir ? redefinir(senha) : superAdminApi.redefinirSenhaEmpresa(empresaId ?? 0, senha)),
        onSuccess: onSaved,
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível redefinir a senha'
            ),
    });

    function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    return (
        <Modal title="Redefinir senha" onClose={onClose} largura="sm">
            <form onSubmit={aoSubmeter} className="space-y-4">
                <p className="text-sm text-slate-600">
                    Nova senha de login para <strong>{empresaNome}</strong>.
                </p>

                <FormField label="Nova senha">
                    <input
                        required
                        type="password"
                        minLength={8}
                        value={senha}
                        onChange={(e) => setSenha(e.target.value)}
                        className={inputClass}
                        placeholder="Mínimo 8 caracteres"
                        autoFocus
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
                        Redefinir
                    </button>
                </div>
            </form>
        </Modal>
    );
}
