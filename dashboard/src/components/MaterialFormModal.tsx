import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import type { Material, MaterialPayload } from '../types';

interface MaterialFormModalProps {
    material?: Material;
    onClose: () => void;
    onSaved: (material: Material) => void;
}

export function MaterialFormModal({ material, onClose, onSaved }: MaterialFormModalProps) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const [nome, setNome] = useState(material?.nome ?? '');
    const [descricao, setDescricao] = useState(material?.descricao ?? '');
    const [ativo, setAtivo] = useState(material?.ativo ?? true);

    const mutation = useMutation({
        mutationFn: (payload: MaterialPayload) =>
            material
                ? api.atualizarMaterial(material.id, payload)
                : api.criarMaterial(payload),
        onSuccess: (salvo) => {
            queryClient.invalidateQueries({ queryKey: ['materiais'] });
            onSaved(salvo);
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível salvar o material'
            ),
    });

    function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);

        mutation.mutate({
            nome: nome.trim(),
            descricao: descricao.trim() || undefined,
            ativo,
        });
    }

    return (
        <Modal title={material ? 'Editar material' : 'Novo material'} onClose={onClose}>
            <form onSubmit={aoSubmeter} className="space-y-4">
                <FormField label="Nome">
                    <input
                        required
                        autoFocus
                        value={nome}
                        onChange={(e) => setNome(e.target.value)}
                        className={inputClass}
                        placeholder="Ex: Vidro temperado 8mm"
                    />
                </FormField>

                <FormField label="Descrição">
                    <textarea
                        value={descricao}
                        onChange={(e) => setDescricao(e.target.value)}
                        className={inputClass}
                        rows={3}
                    />
                </FormField>

                {material && (
                    <label className="flex items-center gap-2 text-sm font-medium text-slate-700">
                        <input
                            type="checkbox"
                            checked={ativo}
                            onChange={(e) => setAtivo(e.target.checked)}
                            className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500/20"
                        />
                        Material ativo
                    </label>
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
