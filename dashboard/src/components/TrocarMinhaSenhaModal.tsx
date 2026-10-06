import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';

interface TrocarMinhaSenhaModalProps {
    onClose: () => void;
    onSaved: () => void;
}

export function TrocarMinhaSenhaModal({ onClose, onSaved }: TrocarMinhaSenhaModalProps) {

    const [senhaAtual, setSenhaAtual] = useState('');
    const [novaSenha, setNovaSenha] = useState('');
    const [erro, setErro] = useState<string | null>(null);

    const mutation = useMutation({
        mutationFn: () => api.trocarMinhaSenha(senhaAtual, novaSenha),
        onSuccess: onSaved,
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível trocar a senha'
            ),
    });

    function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    return (
        <Modal title="Trocar minha senha" onClose={onClose} largura="sm">
            <form onSubmit={aoSubmeter} className="space-y-4">
                <FormField label="Senha atual">
                    <input
                        required
                        type="password"
                        value={senhaAtual}
                        onChange={(e) => setSenhaAtual(e.target.value)}
                        className={inputClass}
                        autoFocus
                    />
                </FormField>

                <FormField label="Nova senha">
                    <input
                        required
                        type="password"
                        minLength={8}
                        value={novaSenha}
                        onChange={(e) => setNovaSenha(e.target.value)}
                        className={inputClass}
                        placeholder="Mínimo 8 caracteres"
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
                        Trocar senha
                    </button>
                </div>
            </form>
        </Modal>
    );
}
