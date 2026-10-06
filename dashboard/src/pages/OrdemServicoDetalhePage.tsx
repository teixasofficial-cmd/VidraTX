import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, CheckCircle2, ClipboardCheck, Loader2, PackageCheck, Save } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { ErrorState } from '../components/ErrorState';
import { StatusProducaoBadge } from '../components/StatusProducaoBadge';
import { InstalacaoPainel } from '../components/InstalacaoPainel';
import { PosVendaPainel } from '../components/PosVendaPainel';
import { inputClass } from '../components/FormField';
import { ApiRequestError } from '../lib/apiClient';
import { useAuth } from '../lib/auth';
import { formatarDataHora } from '../lib/formato';

export function OrdemServicoDetalhePage() {

    const { id } = useParams();
    const ordemServicoId = Number(id);
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { usuario } = useAuth();

    const podeGerenciar = usuario?.perfil === 'ADMIN' || usuario?.perfil === 'GERENTE';

    const [editandoObservacoes, setEditandoObservacoes] = useState(false);
    const [observacoesInput, setObservacoesInput] = useState('');
    const [erro, setErro] = useState<string | null>(null);

    const { data: ordem, isLoading, isError, refetch } = useQuery({
        queryKey: ['ordem-servico', ordemServicoId],
        queryFn: () => api.ordemServico(ordemServicoId),
        enabled: Number.isFinite(ordemServicoId),
    });

    function invalidar() {
        queryClient.invalidateQueries({ queryKey: ['ordem-servico', ordemServicoId] });
        queryClient.invalidateQueries({ queryKey: ['ordens-servico'] });
    }

    function tratarErro(excecao: unknown, mensagemPadrao: string) {
        setErro(excecao instanceof ApiRequestError ? excecao.message : mensagemPadrao);
    }

    const salvarObservacoesMutation = useMutation({
        mutationFn: () => api.atualizarOrdemServico(ordemServicoId, { observacoes: observacoesInput.trim() || undefined }),
        onSuccess: () => {
            invalidar();
            setEditandoObservacoes(false);
        },
        onError: (e) => tratarErro(e, 'Não foi possível salvar as observações'),
    });

    const iniciarProducaoMutation = useMutation({
        mutationFn: () => api.iniciarProducaoOrdemServico(ordemServicoId),
        onSuccess: invalidar,
        onError: (e) => tratarErro(e, 'Não foi possível iniciar a produção'),
    });

    const concluirProducaoMutation = useMutation({
        mutationFn: () => api.concluirProducaoOrdemServico(ordemServicoId),
        onSuccess: invalidar,
        onError: (e) => tratarErro(e, 'Não foi possível concluir a produção'),
    });

    const necessitaProducaoMutation = useMutation({
        mutationFn: (valor: boolean) => api.alterarNecessitaProducaoOrdemServico(ordemServicoId, valor),
        onSuccess: () => {
            invalidar();
            queryClient.invalidateQueries({ queryKey: ['dashboard-proximas-acoes'] });
        },
        onError: (e) => tratarErro(e, 'Não foi possível alterar a produção'),
    });

    const conferirProducaoMutation = useMutation({
        mutationFn: () => api.conferirProducaoOrdemServico(ordemServicoId),
        onSuccess: invalidar,
        onError: (e) => tratarErro(e, 'Não foi possível conferir a produção'),
    });

    if (isLoading) {
        return <Spinner />;
    }

    if (isError || !ordem) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    function aoSalvarObservacoes(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        salvarObservacoesMutation.mutate();
    }

    const podeAgendarInstalacao =
        !ordem.necessitaProducao || ordem.statusProducao === 'CONFERIDO';

    return (
        <div className="max-w-3xl space-y-6">
            <div className="flex items-center gap-3">
                <button
                    type="button"
                    onClick={() => navigate('/ordens-servico')}
                    className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
                    aria-label="Voltar"
                >
                    <ArrowLeft className="h-5 w-5" />
                </button>
                <div className="flex-1">
                    <h1 className="text-2xl font-semibold text-slate-900">
                        Ordem de serviço #{ordem.id}
                    </h1>
                    <p className="mt-0.5 text-sm text-slate-500">
                        {ordem.clienteNome} ·{' '}
                        <Link to={`/orcamentos/${ordem.orcamentoId}`} className="text-indigo-600 hover:underline">
                            orçamento #{ordem.orcamentoId}
                        </Link>
                    </p>
                </div>
            </div>

            {erro && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}

            <div className="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex items-center justify-between">
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
                        Produção
                    </p>
                    {ordem.necessitaProducao && ordem.statusProducao ? (
                        <StatusProducaoBadge status={ordem.statusProducao} />
                    ) : (
                        <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-500">
                            Não passa por produção
                        </span>
                    )}
                </div>

                {podeGerenciar && (!ordem.necessitaProducao || ordem.statusProducao === 'AGUARDANDO_PRODUCAO') && (
                    <label className="flex items-center gap-2 text-sm text-slate-600">
                        <input
                            type="checkbox"
                            checked={ordem.necessitaProducao}
                            disabled={necessitaProducaoMutation.isPending}
                            onChange={(e) => {
                                setErro(null);
                                necessitaProducaoMutation.mutate(e.target.checked);
                            }}
                        />
                        Esta ordem passa por produção antes da instalação
                    </label>
                )}

                {ordem.necessitaProducao && (
                    <>
                        <div className="grid grid-cols-1 gap-3 text-sm text-slate-500 sm:grid-cols-3">
                            {ordem.producaoIniciadaEm && (
                                <p>Iniciada em {formatarDataHora(ordem.producaoIniciadaEm)}</p>
                            )}
                            {ordem.producaoConcluidaEm && (
                                <p>Concluída em {formatarDataHora(ordem.producaoConcluidaEm)}</p>
                            )}
                            {ordem.producaoConferidaEm && (
                                <p>Conferida em {formatarDataHora(ordem.producaoConferidaEm)}</p>
                            )}
                        </div>

                        {podeGerenciar && (
                        <div className="flex flex-wrap gap-2">
                            {ordem.statusProducao === 'AGUARDANDO_PRODUCAO' && (
                                <button
                                    type="button"
                                    onClick={() => iniciarProducaoMutation.mutate()}
                                    disabled={iniciarProducaoMutation.isPending}
                                    className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                                >
                                    <PackageCheck className="h-4 w-4" />
                                    Iniciar produção
                                </button>
                            )}
                            {ordem.statusProducao === 'EM_PRODUCAO' && (
                                <button
                                    type="button"
                                    onClick={() => concluirProducaoMutation.mutate()}
                                    disabled={concluirProducaoMutation.isPending}
                                    className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                                >
                                    <CheckCircle2 className="h-4 w-4" />
                                    Concluir produção
                                </button>
                            )}
                            {ordem.statusProducao === 'PRONTO' && (
                                <button
                                    type="button"
                                    onClick={() => conferirProducaoMutation.mutate()}
                                    disabled={conferirProducaoMutation.isPending}
                                    className="flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-60"
                                >
                                    <ClipboardCheck className="h-4 w-4" />
                                    Conferir produção
                                </button>
                            )}
                        </div>
                        )}
                    </>
                )}
            </div>

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-3 flex items-center justify-between">
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
                        Observações
                    </p>
                    {!editandoObservacoes && podeGerenciar && (
                        <button
                            type="button"
                            onClick={() => {
                                setObservacoesInput(ordem.observacoes ?? '');
                                setEditandoObservacoes(true);
                            }}
                            className="text-sm font-medium text-indigo-600 hover:text-indigo-500"
                        >
                            Editar
                        </button>
                    )}
                </div>

                {editandoObservacoes ? (
                    <form onSubmit={aoSalvarObservacoes} className="space-y-3">
                        <textarea
                            autoFocus
                            value={observacoesInput}
                            onChange={(e) => setObservacoesInput(e.target.value)}
                            className={inputClass}
                            rows={3}
                        />
                        <div className="flex justify-end gap-2">
                            <button
                                type="button"
                                onClick={() => setEditandoObservacoes(false)}
                                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                            >
                                Cancelar
                            </button>
                            <button
                                type="submit"
                                disabled={salvarObservacoesMutation.isPending}
                                className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                            >
                                {salvarObservacoesMutation.isPending && (
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                )}
                                <Save className="h-4 w-4" />
                                Salvar
                            </button>
                        </div>
                    </form>
                ) : (
                    <p className="whitespace-pre-wrap text-sm text-slate-700">
                        {ordem.observacoes || '—'}
                    </p>
                )}
            </div>

            <InstalacaoPainel ordemServicoId={ordemServicoId} podeAgendar={podeAgendarInstalacao} />

            <PosVendaPainel ordemServicoId={ordemServicoId} />
        </div>
    );
}
