import { useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Clock, FileText, Pencil, Plus, ShieldOff, Trash2 } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { ClienteFormModal } from '../components/ClienteFormModal';
import { OrcamentoFormModal } from '../components/OrcamentoFormModal';
import { StatusOrcamentoBadge } from '../components/StatusOrcamentoBadge';
import { ApiRequestError } from '../lib/apiClient';
import { rotuloEventoHistorico } from '../lib/historico';
import { formatarTelefone } from '../lib/formato';
import { useAuth } from '../lib/auth';

function formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
}

function formatarData(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR').format(new Date(iso));
}

function formatarDataHora(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short',
        timeStyle: 'short',
    }).format(new Date(iso));
}

export function ClienteDetalhePage() {

    const { id } = useParams();
    const clienteId = Number(id);
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [editando, setEditando] = useState(false);
    const [criandoOrcamento, setCriandoOrcamento] = useState(false);
    const [erro, setErro] = useState<string | null>(null);

    const { data: cliente, isLoading, isError, refetch } = useQuery({
        queryKey: ['cliente', clienteId],
        queryFn: () => api.cliente(clienteId),
        enabled: Number.isFinite(clienteId),
    });

    const {
        data: orcamentos,
        isLoading: carregandoOrcamentos,
        isError: erroOrcamentos,
    } = useQuery({
        queryKey: ['orcamentos', { clienteId }],
        queryFn: () => api.orcamentos({ clienteId }),
        enabled: Number.isFinite(clienteId),
    });

    const {
        data: historico,
        isLoading: carregandoHistorico,
        isError: erroHistorico,
    } = useQuery({
        queryKey: ['historico-cliente', clienteId],
        queryFn: () => api.historicoCliente(clienteId),
        enabled: Number.isFinite(clienteId),
    });

    const excluirMutation = useMutation({
        mutationFn: () => api.excluirCliente(clienteId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['clientes'] });
            navigate('/clientes');
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível excluir o cliente'
            ),
    });

    const { usuario } = useAuth();

    const anonimizarMutation = useMutation({
        mutationFn: () => api.anonimizarCliente(clienteId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['clientes'] });
            queryClient.invalidateQueries({ queryKey: ['cliente', clienteId] });
            queryClient.invalidateQueries({ queryKey: ['historico-cliente', clienteId] });
        },
        onError: (excecao) =>
            setErro(excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível apagar os dados'),
    });

    if (isLoading) {
        return <Spinner />;
    }

    if (isError || !cliente) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    return (
        <div className="max-w-3xl space-y-6">
            <div className="flex items-center gap-3">
                <button
                    type="button"
                    onClick={() => navigate('/clientes')}
                    className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
                    aria-label="Voltar"
                >
                    <ArrowLeft className="h-5 w-5" />
                </button>
                <div className="flex-1">
                    <h1 className="text-2xl font-semibold text-slate-900">{cliente.nome}</h1>
                    <p className="mt-0.5 text-sm text-slate-500">
                        Cliente desde {formatarData(cliente.criadoEm)}
                    </p>
                </div>
                <button
                    type="button"
                    onClick={() => setEditando(true)}
                    className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                >
                    <Pencil className="h-4 w-4" />
                    Editar
                </button>
                <button
                    type="button"
                    onClick={() => {
                        if (confirm('Excluir este cliente? Esta ação não pode ser desfeita.')) {
                            excluirMutation.mutate();
                        }
                    }}
                    className="flex items-center gap-2 rounded-lg border border-rose-200 px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50"
                >
                    <Trash2 className="h-4 w-4" />
                    Excluir
                </button>
                {usuario?.perfil === 'ADMIN' && (
                    <button
                        type="button"
                        title="O cliente pediu para apagar os dados dele (LGPD)"
                        onClick={() => {
                            if (confirm(
                                'Apagar os dados pessoais deste cliente (nome, telefone, e-mail, endereço, documento, '
                                + 'conversas e fotos)? Os orçamentos e valores continuam, sem o nome dele. '
                                + 'Não tem volta.'
                            )) {
                                anonimizarMutation.mutate();
                            }
                        }}
                        className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                    >
                        <ShieldOff className="h-4 w-4" />
                        Apagar dados (LGPD)
                    </button>
                )}
            </div>

            {erro && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}

            <div className="grid grid-cols-1 gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:grid-cols-2">
                <Campo label="Telefone" valor={formatarTelefone(cliente.telefone) || null} />
                <Campo label="WhatsApp" valor={formatarTelefone(cliente.whatsapp) || null} />
                <Campo label="E-mail" valor={cliente.email} />
                <Campo label="CPF/CNPJ" valor={cliente.cpfCnpj} />
                <Campo label="Endereço" valor={cliente.endereco} className="sm:col-span-2" />
                <Campo label="Observações" valor={cliente.observacoes} className="sm:col-span-2" />
            </div>

            <div className="space-y-3">
                <div className="flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-slate-900">Orçamentos</h2>
                    <button
                        type="button"
                        onClick={() => setCriandoOrcamento(true)}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-indigo-500"
                    >
                        <Plus className="h-4 w-4" />
                        Novo orçamento
                    </button>
                </div>

                {carregandoOrcamentos ? (
                    <Spinner />
                ) : erroOrcamentos ? (
                    <ErrorState title="Não foi possível carregar os orçamentos" />
                ) : !orcamentos || orcamentos.length === 0 ? (
                    <EmptyState
                        icon={FileText}
                        title="Nenhum orçamento ainda"
                        description="Crie o primeiro orçamento para este cliente."
                    />
                ) : (
                    <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                        <ul className="divide-y divide-slate-100">
                            {orcamentos.map((orcamento) => (
                                <li key={orcamento.id}>
                                    <Link
                                        to={`/orcamentos/${orcamento.id}`}
                                        className="flex items-center justify-between px-5 py-3.5 transition-colors hover:bg-slate-50"
                                    >
                                        <div className="flex items-center gap-3">
                                            <span className="text-sm font-medium text-slate-900">
                                                Orçamento #{orcamento.id}
                                            </span>
                                            <StatusOrcamentoBadge status={orcamento.status} />
                                        </div>
                                        <span className="text-sm font-medium text-slate-700">
                                            {formatarMoeda(orcamento.valorTotal)}
                                        </span>
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            <div className="space-y-3">
                <h2 className="text-lg font-semibold text-slate-900">Histórico</h2>

                {carregandoHistorico ? (
                    <Spinner />
                ) : erroHistorico ? (
                    <ErrorState title="Não foi possível carregar o histórico" />
                ) : !historico || historico.length === 0 ? (
                    <EmptyState
                        icon={Clock}
                        title="Nenhum evento registrado"
                        description="Mudanças de status de orçamentos e instalações deste cliente aparecerão aqui."
                    />
                ) : (
                    <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                        <ul className="divide-y divide-slate-100">
                            {historico.map((evento) => (
                                <li key={evento.id} className="flex items-start gap-3 px-5 py-3.5">
                                    <Clock className="mt-0.5 h-4 w-4 shrink-0 text-slate-400" />
                                    <div className="min-w-0 flex-1">
                                        <p className="text-sm font-medium text-slate-900">
                                            {rotuloEventoHistorico[evento.tipo]}
                                        </p>
                                        <p className="mt-0.5 text-sm text-slate-500">
                                            {evento.descricao}
                                        </p>
                                        <p className="mt-1 text-xs text-slate-400">
                                            {formatarDataHora(evento.criadoEm)}
                                            {evento.usuarioNome ? ` · ${evento.usuarioNome}` : ''}
                                        </p>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            {editando && (
                <ClienteFormModal
                    cliente={cliente}
                    onClose={() => setEditando(false)}
                    onSaved={() => {
                        setEditando(false);
                        queryClient.invalidateQueries({ queryKey: ['cliente', clienteId] });
                    }}
                />
            )}

            {criandoOrcamento && (
                <OrcamentoFormModal
                    clienteIdInicial={clienteId}
                    onClose={() => setCriandoOrcamento(false)}
                    onCreated={(orcamento) => navigate(`/orcamentos/${orcamento.id}`)}
                />
            )}
        </div>
    );
}

function Campo({
    label,
    valor,
    className = '',
}: {
    label: string;
    valor: string | null;
    className?: string;
}) {
    return (
        <div className={className}>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</p>
            <p className="mt-0.5 text-sm text-slate-800">{valor || '—'}</p>
        </div>
    );
}
