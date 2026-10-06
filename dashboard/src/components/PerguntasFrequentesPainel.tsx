import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2, Pencil, Plus, Trash2 } from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { FormField, inputClass } from './FormField';
import { Spinner } from './Spinner';
import type { PerguntaFrequente, PerguntaFrequentePayload } from '../types';

const VAZIA: PerguntaFrequentePayload = { pergunta: '', palavrasChave: '', resposta: '', ativa: true };

export function PerguntasFrequentesPainel() {

    const queryClient = useQueryClient();
    const [editando, setEditando] = useState<number | 'nova' | null>(null);
    const [form, setForm] = useState<PerguntaFrequentePayload>(VAZIA);
    const [erro, setErro] = useState<string | null>(null);

    const { data, isLoading, isError } = useQuery({
        queryKey: ['perguntas-frequentes'],
        queryFn: api.perguntasFrequentes,
    });

    const salvar = useMutation({
        mutationFn: (payload: PerguntaFrequentePayload) =>
            editando === 'nova' || editando === null
                ? api.criarPerguntaFrequente(payload)
                : api.atualizarPerguntaFrequente(editando, payload),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['perguntas-frequentes'] });
            setEditando(null);
        },
        onError: (e) => setErro(e instanceof ApiRequestError ? e.message : 'Não foi possível salvar a dúvida'),
    });

    const excluir = useMutation({
        mutationFn: (id: number) => api.excluirPerguntaFrequente(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['perguntas-frequentes'] }),
    });

    function abrir(pergunta?: PerguntaFrequente) {
        setErro(null);
        setEditando(pergunta ? pergunta.id : 'nova');
        setForm(pergunta
            ? { pergunta: pergunta.pergunta, palavrasChave: pergunta.palavrasChave, resposta: pergunta.resposta, ativa: pergunta.ativa }
            : VAZIA);
    }

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        salvar.mutate(form);
    }

    return (
        <div className="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-start justify-between gap-4">
                <div>
                    <p className="text-sm font-semibold text-slate-900">Dúvidas que o robô responde</p>
                    <p className="mt-0.5 text-xs text-slate-500">
                        Quando a mensagem do cliente tem uma das palavras-chave, ele recebe a resposta na hora.
                        Dúvida sem resposta cadastrada vai para uma pessoa.
                    </p>
                </div>
                {editando === null && (
                    <button
                        type="button"
                        onClick={() => abrir()}
                        className="flex shrink-0 items-center gap-1.5 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                    >
                        <Plus className="h-4 w-4" />
                        Nova dúvida
                    </button>
                )}
            </div>

            {editando !== null && (
                <form onSubmit={aoSubmeter} className="space-y-3 rounded-xl bg-slate-50 p-4">
                    <FormField label="Pergunta (como o cliente costuma perguntar)">
                        <input
                            required
                            maxLength={200}
                            value={form.pergunta}
                            placeholder="Ex.: Vocês atendem no meu bairro?"
                            onChange={(e) => setForm({ ...form, pergunta: e.target.value })}
                            className={inputClass}
                        />
                    </FormField>
                    <FormField label="Palavras-chave (separadas por vírgula)">
                        <input
                            required
                            maxLength={500}
                            value={form.palavrasChave}
                            placeholder="Ex.: bairro, região, atendem"
                            onChange={(e) => setForm({ ...form, palavrasChave: e.target.value })}
                            className={inputClass}
                        />
                    </FormField>
                    <FormField label="Resposta">
                        <textarea
                            required
                            rows={3}
                            maxLength={2000}
                            value={form.resposta}
                            onChange={(e) => setForm({ ...form, resposta: e.target.value })}
                            className={inputClass}
                        />
                    </FormField>
                    <label className="flex items-center gap-2 text-sm text-slate-700">
                        <input
                            type="checkbox"
                            checked={form.ativa}
                            onChange={(e) => setForm({ ...form, ativa: e.target.checked })}
                            className="h-4 w-4 rounded border-slate-300"
                        />
                        Ativa
                    </label>

                    {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}

                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={() => setEditando(null)}
                            className="rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                        >
                            Cancelar
                        </button>
                        <button
                            type="submit"
                            disabled={salvar.isPending}
                            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
                        >
                            {salvar.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                            Salvar
                        </button>
                    </div>
                </form>
            )}

            {isLoading && <Spinner />}
            {isError && <p className="text-sm text-rose-600">Não foi possível carregar as dúvidas.</p>}

            {data && data.length === 0 && editando === null && (
                <p className="text-sm text-slate-500">
                    Nenhuma dúvida cadastrada. Comece pelas que mais chegam: bairros atendidos, prazo de entrega,
                    formas de pagamento, se fazem visita.
                </p>
            )}

            {data && data.length > 0 && (
                <ul className="divide-y divide-slate-100">
                    {data.map((p) => (
                        <li key={p.id} className="flex items-start justify-between gap-4 py-3">
                            <div className="min-w-0">
                                <p className="text-sm font-medium text-slate-900">
                                    {p.pergunta}
                                    {!p.ativa && <span className="ml-2 text-xs font-normal text-slate-400">(desativada)</span>}
                                </p>
                                <p className="mt-0.5 text-sm text-slate-600">{p.resposta}</p>
                                <p className="mt-0.5 text-xs text-slate-400">Palavras-chave: {p.palavrasChave}</p>
                            </div>
                            <div className="flex shrink-0 gap-1">
                                <button
                                    type="button"
                                    title="Editar"
                                    onClick={() => abrir(p)}
                                    className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
                                >
                                    <Pencil className="h-4 w-4" />
                                </button>
                                <button
                                    type="button"
                                    title="Excluir"
                                    onClick={() => {
                                        if (confirm(`Excluir a dúvida "${p.pergunta}"?`)) {
                                            excluir.mutate(p.id);
                                        }
                                    }}
                                    className="rounded-lg p-2 text-slate-500 hover:bg-rose-50 hover:text-rose-600"
                                >
                                    <Trash2 className="h-4 w-4" />
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
