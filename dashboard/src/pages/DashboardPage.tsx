import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import {
    AlertTriangle,
    CalendarDays,
    CheckCircle2,
    FileText,
    Hourglass,
    ListTodo,
    MessageCircle,
    Percent,
    Ruler,
    TrendingUp,
    Wrench,
    XCircle,
} from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { dataIsoLocal, formatarDataHora, formatarHora, formatarMoeda } from '../lib/formato';
import { StatCard } from '../components/StatCard';
import { Spinner } from '../components/Spinner';
import { ErrorState } from '../components/ErrorState';
import { ResponsavelBadge } from '../components/ProximaAcao';
import { StatusAgendamentoBadge } from '../components/StatusAgendamentoBadge';
import type { AgendaItem, ProximaAcaoItem } from '../types';

const DIAS_AGENDA = 7;

function plural(quantidade: number, singular: string, pluralTexto: string): string {
    return `${quantidade} ${quantidade === 1 ? singular : pluralTexto}`;
}

export function DashboardPage() {

    const hoje = new Date();
    const ate = new Date(hoje);
    ate.setDate(ate.getDate() + DIAS_AGENDA - 1);

    const resumo = useQuery({
        queryKey: ['dashboard-resumo'],
        queryFn: api.dashboardResumo,
        refetchInterval: 30000,
    });

    const acoes = useQuery({
        queryKey: ['dashboard-proximas-acoes'],
        queryFn: api.proximasAcoes,
        refetchInterval: 30000,
    });

    const agenda = useQuery({
        queryKey: ['dashboard-agenda', dataIsoLocal(hoje), dataIsoLocal(ate)],
        queryFn: () => api.agenda(dataIsoLocal(hoje), dataIsoLocal(ate)),
        refetchInterval: 60000,
    });

    if (resumo.isLoading) {
        return <Spinner />;
    }

    if (resumo.isError || !resumo.data) {
        return <ErrorState onRetry={() => resumo.refetch()} />;
    }

    const data = resumo.data;
    const emAberto = data.orcamentosRascunho + data.orcamentosEnviados;

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-slate-900">Visão geral</h1>
                <p className="mt-1 text-sm text-slate-500">O que precisa de você hoje e como a empresa está.</p>
            </div>

            {data.atendimentosPendentesWhatsapp > 0 && (
                <Link
                    to="/atendimentos"
                    className="flex items-center justify-between rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 transition-colors hover:bg-emerald-100"
                >
                    <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500 text-white">
                            <MessageCircle className="h-5 w-5" />
                        </div>
                        <div>
                            <p className="text-sm font-semibold text-emerald-900">
                                {data.atendimentosPendentesWhatsapp === 1
                                    ? '1 cliente esperando resposta no WhatsApp'
                                    : `${data.atendimentosPendentesWhatsapp} clientes esperando resposta no WhatsApp`}
                            </p>
                            <p className="text-sm text-emerald-700">
                                {data.atendimentosAtrasadosWhatsapp > 0 ? (
                                    <span className="font-semibold text-rose-700">
                                        {data.atendimentosAtrasadosWhatsapp === 1
                                            ? '1 passou do prazo de resposta. '
                                            : `${data.atendimentosAtrasadosWhatsapp} passaram do prazo de resposta. `}
                                    </span>
                                ) : null}
                                Clique para ver a fila de atendimento
                            </p>
                        </div>
                    </div>
                </Link>
            )}

            {data.mensagensNaoEntregues > 0 && (
                <div className="flex items-center gap-3 rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4">
                    <AlertTriangle className="h-5 w-5 shrink-0 text-rose-600" />
                    <p className="text-sm text-rose-800">
                        {data.mensagensNaoEntregues === 1
                            ? '1 mensagem não chegou ao cliente nos últimos 7 dias.'
                            : `${data.mensagensNaoEntregues} mensagens não chegaram aos clientes nos últimos 7 dias.`}
                        {' '}Confira a conexão do WhatsApp e as ações abaixo.
                    </p>
                </div>
            )}

            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                <StatCard
                    label="Sua vez"
                    value={String(data.acoesDaEmpresa)}
                    icon={ListTodo}
                    accent="warning"
                    hint={data.contrapropostasPendentes > 0
                        ? plural(data.contrapropostasPendentes, 'sugestão de data para responder', 'sugestões de data para responder')
                        : 'itens esperando uma ação da empresa'}
                />
                <StatCard
                    label="Aguardando cliente"
                    value={String(data.aguardandoCliente)}
                    icon={Hourglass}
                    hint={plural(data.medicoesAguardandoCliente, 'confirmação de medição', 'confirmações de medição')}
                />
                <StatCard
                    label="Medições hoje"
                    value={String(data.medicoesHoje)}
                    icon={Ruler}
                />
                <StatCard
                    label="Instalações hoje"
                    value={String(data.instalacoesHoje)}
                    icon={Wrench}
                    hint={plural(data.instalacoesAguardandoAgendamento, 'OS esperando agendamento', 'OS esperando agendamento')}
                />
            </div>

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
                <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm xl:col-span-2">
                    <h2 className="flex items-center gap-2 text-base font-semibold text-slate-900">
                        <ListTodo className="h-4 w-4 text-slate-400" />
                        Próximas ações
                    </h2>
                    <ProximasAcoes
                        itens={acoes.data}
                        carregando={acoes.isLoading}
                        erro={acoes.isError}
                        onRetry={() => acoes.refetch()}
                    />
                </section>

                <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                    <h2 className="flex items-center gap-2 text-base font-semibold text-slate-900">
                        <CalendarDays className="h-4 w-4 text-slate-400" />
                        Agenda dos próximos {DIAS_AGENDA} dias
                    </h2>
                    <Agenda
                        itens={agenda.data}
                        carregando={agenda.isLoading}
                        erro={agenda.isError}
                        onRetry={() => agenda.refetch()}
                    />
                </section>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
                <StatCard
                    label="Orçamentos em aberto"
                    value={String(emAberto)}
                    icon={FileText}
                />
                <StatCard
                    label="Aprovados"
                    value={String(data.orcamentosAprovados)}
                    icon={CheckCircle2}
                    accent="success"
                    hint={data.orcamentosAprovadosSemOs > 0
                        ? `${data.orcamentosAprovadosSemOs} sem ordem de serviço`
                        : undefined}
                />
                <StatCard
                    label="Recusados/perdidos"
                    value={String(data.orcamentosRecusados)}
                    icon={XCircle}
                    accent="danger"
                />
                <StatCard
                    label="Taxa de conversão"
                    value={`${data.taxaConversaoPercentual.toLocaleString('pt-BR')}%`}
                    icon={Percent}
                    hint={`${data.orcamentosAprovados} de ${data.totalOrcamentosEnviados} orçamentos enviados`}
                />
                <StatCard
                    label="Valor aprovado"
                    value={formatarMoeda(data.valorAprovado)}
                    icon={TrendingUp}
                    accent="warning"
                    hint={`de ${formatarMoeda(data.valorTotalEnviado)} em orçamentos enviados`}
                />
            </div>
        </div>
    );
}

function destinoDaAcao(item: ProximaAcaoItem): string | null {

    switch (item.tipo) {
        case 'MEDICAO':
        case 'ORCAMENTO':
            return item.orcamentoId ? `/orcamentos/${item.orcamentoId}` : null;
        case 'INSTALACAO':
        case 'ORDEM_SERVICO':
            return item.ordemServicoId ? `/ordens-servico/${item.ordemServicoId}` : null;
        case 'CONVERSA':
            return item.atendimentoId ? `/atendimentos/${item.atendimentoId}` : null;
        default:
            return null;
    }
}

const rotuloTipo: Record<ProximaAcaoItem['tipo'], string> = {
    MEDICAO: 'Medição',
    INSTALACAO: 'Instalação',
    ORCAMENTO: 'Orçamento',
    ORDEM_SERVICO: 'Ordem de serviço',
    CONVERSA: 'WhatsApp',
    SOLICITACAO: 'Solicitação',
};

function ProximasAcoes({
    itens,
    carregando,
    erro,
    onRetry,
}: {
    itens: ProximaAcaoItem[] | undefined;
    carregando: boolean;
    erro: boolean;
    onRetry: () => void;
}) {

    const [mostrarAguardando, setMostrarAguardando] = useState(false);

    if (carregando) {
        return <Spinner />;
    }

    if (erro || !itens) {
        return <ErrorState onRetry={onRetry} />;
    }

    const daEmpresa = itens.filter((i) => i.responsavel === 'EMPRESA' || i.responsavel === 'SISTEMA');
    const doCliente = itens.filter((i) => i.responsavel === 'CLIENTE');

    return (
        <div className="mt-4 space-y-4">
            {daEmpresa.length === 0 ? (
                <p className="rounded-xl bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                    Nada esperando a empresa agora.
                </p>
            ) : (
                <ul className="divide-y divide-slate-100">
                    {daEmpresa.map((item) => (
                        <LinhaAcao key={`${item.tipo}-${item.referenciaId}`} item={item} />
                    ))}
                </ul>
            )}

            {doCliente.length > 0 && (
                <div>
                    <button
                        onClick={() => setMostrarAguardando((v) => !v)}
                        className="text-sm font-medium text-indigo-600 hover:text-indigo-700"
                    >
                        {mostrarAguardando ? 'Ocultar' : 'Ver'} {doCliente.length}{' '}
                        {doCliente.length === 1 ? 'item aguardando' : 'itens aguardando'} o cliente
                    </button>
                    {mostrarAguardando && (
                        <ul className="mt-2 divide-y divide-slate-100">
                            {doCliente.map((item) => (
                                <LinhaAcao key={`${item.tipo}-${item.referenciaId}`} item={item} />
                            ))}
                        </ul>
                    )}
                </div>
            )}
        </div>
    );
}

function LinhaAcao({ item }: { item: ProximaAcaoItem }) {

    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const criarOrcamento = useMutation({
        mutationFn: () => api.criarOrcamentoDeSolicitacao(item.referenciaId),
        onSuccess: (orcamento) => {
            queryClient.invalidateQueries({ queryKey: ['dashboard-proximas-acoes'] });
            navigate(`/orcamentos/${orcamento.id}`);
        },
        onError: (e) => setErro(e instanceof ApiRequestError ? e.message : 'Não foi possível criar o orçamento'),
    });

    const destino = destinoDaAcao(item);

    const conteudo = (
        <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
                <span className="text-xs font-medium uppercase tracking-wide text-slate-400">
                    {rotuloTipo[item.tipo]}
                </span>
                {item.urgente && (
                    <span className="rounded-full bg-rose-100 px-2 py-0.5 text-xs font-medium text-rose-700">
                        Urgente
                    </span>
                )}
                <ResponsavelBadge responsavel={item.responsavel} />
            </div>
            <p className="mt-1 text-sm font-medium text-slate-900">
                {item.clienteNome ?? 'Cliente sem cadastro'}
            </p>
            <p className="text-sm text-slate-600">{item.descricao}</p>
            {erro && <p className="mt-1 text-xs text-rose-600">{erro}</p>}
        </div>
    );

    return (
        <li className="flex items-start gap-3 py-3">
            {destino ? (
                <Link to={destino} className="-mx-2 flex flex-1 rounded-lg px-2 hover:bg-slate-50">
                    {conteudo}
                </Link>
            ) : (
                conteudo
            )}

            <div className="flex shrink-0 flex-col items-end gap-2">
                {item.data && (
                    <span className="text-xs text-slate-400">{formatarDataHora(item.data)}</span>
                )}
                {item.tipo === 'SOLICITACAO' && (
                    <button
                        onClick={() => criarOrcamento.mutate()}
                        disabled={criarOrcamento.isPending}
                        className="rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
                    >
                        Criar orçamento
                    </button>
                )}
            </div>
        </li>
    );
}

function Agenda({
    itens,
    carregando,
    erro,
    onRetry,
}: {
    itens: AgendaItem[] | undefined;
    carregando: boolean;
    erro: boolean;
    onRetry: () => void;
}) {

    if (carregando) {
        return <Spinner />;
    }

    if (erro || !itens) {
        return <ErrorState onRetry={onRetry} />;
    }

    if (itens.length === 0) {
        return <p className="mt-4 text-sm text-slate-500">Nenhuma visita ou instalação marcada.</p>;
    }

    const porDia = new Map<string, AgendaItem[]>();

    for (const item of itens) {
        const dia = item.inicio.slice(0, 10);
        porDia.set(dia, [...(porDia.get(dia) ?? []), item]);
    }

    return (
        <div className="mt-4 space-y-4">
            {[...porDia.entries()].map(([dia, doDia]) => (
                <div key={dia}>
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                        {new Intl.DateTimeFormat('pt-BR', {
                            weekday: 'long',
                            day: '2-digit',
                            month: '2-digit',
                        }).format(new Date(`${dia}T12:00:00`))}
                    </p>
                    <ul className="mt-1 space-y-1">
                        {doDia.map((item) => (
                            <li key={`${item.tipo}-${item.id}`}>
                                <Link
                                    to={item.tipo === 'MEDICAO'
                                        ? `/orcamentos/${item.orcamentoId}`
                                        : `/ordens-servico/${item.ordemServicoId}`}
                                    className="block rounded-lg px-2 py-1.5 text-sm hover:bg-slate-50"
                                >
                                    <div className="flex items-center justify-between gap-2">
                                        <span className="font-medium text-slate-900">
                                            {formatarHora(item.inicio)}–{formatarHora(item.fim)} ·{' '}
                                            {item.tipo === 'MEDICAO' ? 'Medição' : 'Instalação'}
                                        </span>
                                        <StatusAgendamentoBadge status={item.status} />
                                    </div>
                                    <p className="truncate text-slate-600">
                                        {item.clienteNome}
                                        {item.equipe ? ` · ${item.equipe}` : ''}
                                    </p>
                                    {item.cancelamentoSolicitado && (
                                        <p className="text-xs font-medium text-rose-600">
                                            Cliente pediu para cancelar — confirme com ele
                                        </p>
                                    )}
                                    {item.endereco && (
                                        <p className="truncate text-xs text-slate-400">{item.endereco}</p>
                                    )}
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            ))}
        </div>
    );
}
