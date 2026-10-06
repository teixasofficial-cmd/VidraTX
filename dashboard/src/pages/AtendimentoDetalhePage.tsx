import { useEffect, useRef, useState, type FormEvent, type ReactNode } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    ArrowRightLeft,
    Bot,
    CheckCircle2,
    FileText,
    Headset,
    History,
    Link2,
    RotateCcw,
    Send,
    User,
    UserCheck,
} from 'lucide-react';
import { api } from '../lib/api';
import { MensagemImagem } from '../components/MensagemImagem';
import { Spinner } from '../components/Spinner';
import { ErrorState } from '../components/ErrorState';
import { ProximaAcaoAviso } from '../components/ProximaAcao';
import { EnvioStatus, envioComProblema } from '../components/EnvioStatus';
import { useAuth } from '../lib/auth';
import { ApiRequestError } from '../lib/apiClient';
import { formatarDataHora, formatarHora, formatarTelefone } from '../lib/formato';
import { rotuloEventoHistorico } from '../lib/historico';
import type { RemetenteMensagem, StatusAtendimento } from '../types';

const remetenteEstilo: Record<
    RemetenteMensagem,
    { alinhamento: string; bolha: string; icone: typeof User }
> = {
    CLIENTE: {
        alinhamento: 'justify-start',
        bolha: 'bg-white border border-slate-200 text-slate-800',
        icone: User,
    },
    BOT: {
        alinhamento: 'justify-end',
        bolha: 'bg-slate-200 text-slate-700',
        icone: Bot,
    },
    ATENDENTE: {
        alinhamento: 'justify-end',
        bolha: 'bg-indigo-600 text-white',
        icone: Headset,
    },
};

const statusEstilo: Record<StatusAtendimento, string> = {
    EM_FLUXO_BOT: 'bg-slate-100 text-slate-600',
    AGUARDANDO_ATENDENTE: 'bg-amber-100 text-amber-700',
    EM_ATENDIMENTO_HUMANO: 'bg-indigo-100 text-indigo-700',
    ENCERRADO: 'bg-slate-100 text-slate-500',
};

function rotuloStatus(status: StatusAtendimento, atendenteNome: string | null): string {

    switch (status) {
        case 'EM_FLUXO_BOT':
            return 'Com o bot';
        case 'AGUARDANDO_ATENDENTE':
            return 'Aguardando atendente';
        case 'EM_ATENDIMENTO_HUMANO':
            return atendenteNome ? `Com ${atendenteNome}` : 'Em atendimento';
        case 'ENCERRADO':
            return 'Encerrado';
    }
}

export function AtendimentoDetalhePage() {

    const { id } = useParams();
    const atendimentoId = Number(id);
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { usuario } = useAuth();

    const [mensagem, setMensagem] = useState('');
    const [erro, setErro] = useState<string | null>(null);
    const [painel, setPainel] = useState<'transferir' | 'vincular' | 'encerrar' | null>(null);
    const [mostrarHistorico, setMostrarHistorico] = useState(false);
    const [destinoId, setDestinoId] = useState('');
    const [buscaCliente, setBuscaCliente] = useState('');
    const [clienteId, setClienteId] = useState('');
    const [motivoEncerramento, setMotivoEncerramento] = useState('');
    const [pendenciasAoEncerrar, setPendenciasAoEncerrar] = useState<string[] | null>(null);
    const fimDaConversaRef = useRef<HTMLDivElement>(null);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['atendimento', atendimentoId],
        queryFn: () => api.atendimentoDetalhe(atendimentoId),
        refetchInterval: 8000,
        enabled: Number.isFinite(atendimentoId),
    });

    const atendentes = useQuery({
        queryKey: ['atendentes'],
        queryFn: api.atendentes,
        enabled: painel === 'transferir',
    });

    const clientes = useQuery({
        queryKey: ['clientes', buscaCliente],
        queryFn: () => api.clientes(buscaCliente.trim() || undefined),
        enabled: painel === 'vincular',
    });

    useEffect(() => {
        fimDaConversaRef.current?.scrollIntoView({ block: 'end' });
    }, [data?.mensagens.length]);

    function invalidarListas() {
        queryClient.invalidateQueries({ queryKey: ['atendimento', atendimentoId] });
        queryClient.invalidateQueries({ queryKey: ['atendimentos'] });
        queryClient.invalidateQueries({ queryKey: ['atendimentos-pendentes'] });
        queryClient.invalidateQueries({ queryKey: ['atendimentos-pendentes-contagem'] });
        queryClient.invalidateQueries({ queryKey: ['dashboard-resumo'] });
        queryClient.invalidateQueries({ queryKey: ['dashboard-proximas-acoes'] });
    }

    function aposAcao() {
        setErro(null);
        setPainel(null);
        setPendenciasAoEncerrar(null);
        invalidarListas();
    }

    function tratarErro(excecao: unknown, mensagemPadrao: string) {
        setErro(excecao instanceof ApiRequestError ? excecao.message : mensagemPadrao);
    }

    const assumirMutation = useMutation({
        mutationFn: () => api.assumirAtendimento(atendimentoId),
        onSuccess: aposAcao,
        onError: (excecao) => tratarErro(excecao, 'Não foi possível assumir o atendimento'),
    });

    const responderMutation = useMutation({
        mutationFn: (texto: string) => api.responderAtendimento(atendimentoId, texto),
        onSuccess: () => {
            setMensagem('');
            aposAcao();
        },
        onError: (excecao) => tratarErro(excecao, 'Não foi possível enviar a mensagem'),
    });

    const encerrarMutation = useMutation({
        mutationFn: (forcar: boolean) => api.encerrarAtendimento(atendimentoId, {
            forcar,
            motivo: motivoEncerramento.trim() || undefined,
        }),
        onSuccess: aposAcao,
        onError: (excecao) => {
            if (excecao instanceof ApiRequestError && excecao.erros?.codigo === 'PENDENCIAS_ABERTAS') {
                setPendenciasAoEncerrar(
                    Object.entries(excecao.erros)
                        .filter(([chave]) => chave.startsWith('pendencia'))
                        .map(([, valor]) => valor)
                );
                return;
            }
            tratarErro(excecao, 'Não foi possível encerrar o atendimento');
        },
    });

    const transferirMutation = useMutation({
        mutationFn: (usuarioId: number) => api.transferirAtendimento(atendimentoId, usuarioId),
        onSuccess: aposAcao,
        onError: (excecao) => tratarErro(excecao, 'Não foi possível transferir o atendimento'),
    });

    const reabrirMutation = useMutation({
        mutationFn: () => api.reabrirAtendimento(atendimentoId),
        onSuccess: aposAcao,
        onError: (excecao) => tratarErro(excecao, 'Não foi possível reabrir o atendimento'),
    });

    const devolverAoBotMutation = useMutation({
        mutationFn: () => api.devolverAtendimentoAoBot(atendimentoId),
        onSuccess: aposAcao,
        onError: (excecao) => tratarErro(excecao, 'Não foi possível devolver a conversa ao bot'),
    });

    const vincularMutation = useMutation({
        mutationFn: (id: number) => api.vincularClienteAtendimento(atendimentoId, id),
        onSuccess: aposAcao,
        onError: (excecao) => tratarErro(excecao, 'Não foi possível vincular o cliente'),
    });

    const criarOrcamentoMutation = useMutation({
        mutationFn: (solicitacaoId: number) => api.criarOrcamentoDeSolicitacao(solicitacaoId),
        onSuccess: (orcamento) => navigate(`/orcamentos/${orcamento.id}`),
        onError: (excecao) => tratarErro(excecao, 'Não foi possível criar o orçamento'),
    });

    if (isLoading) {
        return <Spinner />;
    }

    if (isError || !data) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    const { atendimento, mensagens, historico } = data;
    const souEuQueAtendo = atendimento.atendenteId === usuario?.usuarioId;
    const encerrado = atendimento.status === 'ENCERRADO';
    const comPessoa = atendimento.status === 'EM_ATENDIMENTO_HUMANO' || atendimento.status === 'AGUARDANDO_ATENDENTE';
    const podeResponder = !encerrado;

    function aoEnviar(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);

        const texto = mensagem.trim();

        if (!texto) {
            return;
        }

        responderMutation.mutate(texto);
    }

    function abrirPainel(qual: 'transferir' | 'vincular' | 'encerrar') {
        setErro(null);
        setPendenciasAoEncerrar(null);
        setDestinoId('');
        setClienteId('');
        setBuscaCliente('');
        setMotivoEncerramento('');
        setPainel((atual) => (atual === qual ? null : qual));
    }

    return (
        <div className="mx-auto flex h-[calc(100vh-6rem)] max-w-2xl flex-col lg:h-[calc(100vh-8rem)]">
            <div className="mb-3 flex items-center gap-3">
                <button
                    type="button"
                    onClick={() => navigate('/atendimentos')}
                    className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
                    aria-label="Voltar"
                >
                    <ArrowLeft className="h-5 w-5" />
                </button>
                <div className="min-w-0 flex-1">
                    <h1 className="truncate text-lg font-semibold text-slate-900">
                        {atendimento.clienteId ? (
                            <Link to={`/clientes/${atendimento.clienteId}`} className="hover:underline">
                                {atendimento.clienteNome ?? formatarTelefone(atendimento.telefone)}
                            </Link>
                        ) : (
                            atendimento.clienteNome ?? atendimento.telefone
                        )}
                    </h1>
                    <p className="text-sm text-slate-500">
                        {formatarTelefone(atendimento.telefone)}
                        {!atendimento.clienteId && ' · sem cadastro'}
                    </p>
                </div>
                <span
                    className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${statusEstilo[atendimento.status]}`}
                >
                    {rotuloStatus(atendimento.status, atendimento.atendenteNome)}
                </span>
            </div>

            <div className="mb-3 space-y-2">
                <ProximaAcaoAviso
                    responsavel={atendimento.responsavelProximaAcao}
                    texto={atendimento.proximaAcao}
                />

                {atendimento.pendencias && atendimento.pendencias.length > 0 && (
                    <div className="rounded-xl border border-sky-200 bg-sky-50 px-3 py-2 text-sm text-sky-900">
                        <p className="font-medium">Esperando resposta do cliente:</p>
                        <ul className="mt-1 list-disc pl-5">
                            {atendimento.pendencias.map((pendencia) => <li key={pendencia}>{pendencia}</li>)}
                        </ul>
                    </div>
                )}

                {encerrado && atendimento.motivoEncerramento && (
                    <p className="rounded-lg bg-slate-100 px-3 py-2 text-sm text-slate-600">
                        Encerrada{atendimento.encerradoEm ? ` em ${formatarDataHora(atendimento.encerradoEm)}` : ''}:{' '}
                        {atendimento.motivoEncerramento}
                    </p>
                )}

                {atendimento.solicitacaoOrcamentoId && (
                    <button
                        type="button"
                        onClick={() => criarOrcamentoMutation.mutate(atendimento.solicitacaoOrcamentoId!)}
                        disabled={criarOrcamentoMutation.isPending}
                        className="flex w-full items-center justify-center gap-2 rounded-xl border border-indigo-200 bg-indigo-50 px-4 py-2.5 text-sm font-semibold text-indigo-700 transition-colors hover:bg-indigo-100 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        <FileText className="h-4 w-4" />
                        Criar orçamento a partir desta solicitação
                    </button>
                )}
            </div>

            <div className="flex-1 space-y-3 overflow-y-auto rounded-2xl border border-slate-200 bg-slate-50 p-4">
                {mensagens.map((msg) => {

                    const estilo = remetenteEstilo[msg.remetente];
                    const Icone = estilo.icone;
                    const falhou = envioComProblema(msg.envioStatus);

                    return (
                        <div key={msg.id} className={`flex ${estilo.alinhamento}`}>
                            <div
                                className={`flex max-w-[75%] items-start gap-2 rounded-2xl px-4 py-2.5 text-sm shadow-sm ${estilo.bolha} ${
                                    falhou ? 'ring-2 ring-rose-400' : ''
                                }`}
                            >
                                <Icone className="mt-0.5 h-3.5 w-3.5 shrink-0 opacity-60" />
                                <div>
                                    {msg.tipo === 'IMAGEM' ? (
                                        <>
                                            <MensagemImagem mensagemId={msg.id} legenda={msg.conteudo} />
                                            {msg.conteudo && (
                                                <p className="mt-1 whitespace-pre-wrap">{msg.conteudo}</p>
                                            )}
                                        </>
                                    ) : (
                                        <p className="whitespace-pre-wrap">{msg.conteudo}</p>
                                    )}
                                    <div className="mt-1 flex items-center justify-end gap-2 text-[11px] opacity-70">
                                        <span>{formatarHora(msg.enviadoEm)}</span>
                                        {msg.remetente !== 'CLIENTE' && (
                                            <EnvioStatus
                                                status={msg.envioStatus}
                                                erro={msg.envioErro}
                                                compacto={!falhou}
                                                sobreFundoEscuro={msg.remetente === 'ATENDENTE'}
                                            />
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    );
                })}
                <div ref={fimDaConversaRef} />
            </div>

            {erro && (
                <p className="mt-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}

            {painel === 'transferir' && (
                <div className="mt-3 flex flex-wrap items-center gap-2 rounded-xl border border-slate-200 bg-white p-3">
                    <select
                        value={destinoId}
                        onChange={(e) => setDestinoId(e.target.value)}
                        className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    >
                        <option value="">Transferir para...</option>
                        {atendentes.data
                            ?.filter((a) => a.id !== atendimento.atendenteId)
                            .map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
                    </select>
                    <button
                        type="button"
                        disabled={!destinoId || transferirMutation.isPending}
                        onClick={() => transferirMutation.mutate(Number(destinoId))}
                        className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
                    >
                        Transferir
                    </button>
                </div>
            )}

            {painel === 'vincular' && (
                <div className="mt-3 space-y-2 rounded-xl border border-slate-200 bg-white p-3">
                    <input
                        value={buscaCliente}
                        onChange={(e) => setBuscaCliente(e.target.value)}
                        placeholder="Buscar cliente pelo nome"
                        className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    />
                    <div className="flex flex-wrap items-center gap-2">
                        <select
                            value={clienteId}
                            onChange={(e) => setClienteId(e.target.value)}
                            className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm"
                        >
                            <option value="">Selecione o cliente...</option>
                            {clientes.data?.map((c) => (
                                <option key={c.id} value={c.id}>
                                    {c.nome}{c.whatsapp ? ` · ${formatarTelefone(c.whatsapp)}` : ''}
                                </option>
                            ))}
                        </select>
                        <button
                            type="button"
                            disabled={!clienteId || vincularMutation.isPending}
                            onClick={() => vincularMutation.mutate(Number(clienteId))}
                            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
                        >
                            Vincular
                        </button>
                    </div>
                </div>
            )}

            {painel === 'encerrar' && (
                <div className="mt-3 space-y-2 rounded-xl border border-slate-200 bg-white p-3">
                    <input
                        value={motivoEncerramento}
                        onChange={(e) => setMotivoEncerramento(e.target.value)}
                        placeholder="Motivo (opcional)"
                        maxLength={300}
                        className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    />
                    {pendenciasAoEncerrar && (
                        <div className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
                            <p className="font-medium">O cliente ainda tem assuntos em aberto:</p>
                            <ul className="mt-1 list-disc pl-5">
                                {pendenciasAoEncerrar.map((p) => <li key={p}>{p}</li>)}
                            </ul>
                            <p className="mt-1">Encerrar mesmo assim? As respostas dele ainda serão entendidas pelo sistema.</p>
                        </div>
                    )}
                    <div className="flex justify-end gap-2">
                        <button
                            type="button"
                            onClick={() => setPainel(null)}
                            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            Voltar
                        </button>
                        <button
                            type="button"
                            disabled={encerrarMutation.isPending}
                            onClick={() => encerrarMutation.mutate(pendenciasAoEncerrar !== null)}
                            className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700 disabled:opacity-60"
                        >
                            {pendenciasAoEncerrar ? 'Encerrar mesmo assim' : 'Encerrar conversa'}
                        </button>
                    </div>
                </div>
            )}

            <div className="mt-3 space-y-2">
                {podeResponder && (
                    <>
                        {atendimento.status === 'EM_ATENDIMENTO_HUMANO' && !souEuQueAtendo && (
                            <p className="rounded-lg bg-slate-100 px-3 py-2 text-center text-xs text-slate-500">
                                Sendo atendido por {atendimento.atendenteNome}. Se você responder, a conversa passa para você.
                            </p>
                        )}
                        <form onSubmit={aoEnviar} className="flex items-center gap-2">
                            <input
                                value={mensagem}
                                onChange={(evento) => setMensagem(evento.target.value)}
                                placeholder="Escreva uma mensagem..."
                                className="flex-1 rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                            />
                            <button
                                type="submit"
                                disabled={responderMutation.isPending || !mensagem.trim()}
                                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-indigo-600 text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                                aria-label="Enviar"
                            >
                                <Send className="h-4 w-4" />
                            </button>
                        </form>
                    </>
                )}

                <div className="flex flex-wrap gap-2">
                    {(atendimento.status === 'AGUARDANDO_ATENDENTE' || atendimento.status === 'EM_FLUXO_BOT') && (
                        <BotaoConversa
                            destaque
                            onClick={() => assumirMutation.mutate()}
                            disabled={assumirMutation.isPending}
                        >
                            <UserCheck className="h-4 w-4" />
                            Assumir atendimento
                        </BotaoConversa>
                    )}

                    {!encerrado && (
                        <BotaoConversa onClick={() => abrirPainel('transferir')}>
                            <ArrowRightLeft className="h-4 w-4" />
                            Transferir
                        </BotaoConversa>
                    )}

                    {comPessoa && (
                        <BotaoConversa
                            onClick={() => {
                                if (confirm(
                                    'Devolver a conversa ao bot? Se o cliente tiver uma pergunta em aberto, '
                                    + 'o bot a repete; senão, mostra o menu na próxima mensagem dele.'
                                )) {
                                    setErro(null);
                                    devolverAoBotMutation.mutate();
                                }
                            }}
                            disabled={devolverAoBotMutation.isPending}
                        >
                            <Bot className="h-4 w-4" />
                            Devolver ao bot
                        </BotaoConversa>
                    )}

                    {!atendimento.clienteId && (
                        <BotaoConversa onClick={() => abrirPainel('vincular')}>
                            <Link2 className="h-4 w-4" />
                            Vincular a um cliente
                        </BotaoConversa>
                    )}

                    {!encerrado && (
                        <BotaoConversa onClick={() => abrirPainel('encerrar')}>
                            <CheckCircle2 className="h-4 w-4" />
                            Encerrar
                        </BotaoConversa>
                    )}

                    {encerrado && (
                        <BotaoConversa
                            destaque
                            onClick={() => {
                                setErro(null);
                                reabrirMutation.mutate();
                            }}
                            disabled={reabrirMutation.isPending}
                        >
                            <RotateCcw className="h-4 w-4" />
                            Reabrir conversa
                        </BotaoConversa>
                    )}

                    <BotaoConversa onClick={() => setMostrarHistorico((v) => !v)}>
                        <History className="h-4 w-4" />
                        {mostrarHistorico ? 'Ocultar histórico' : 'Histórico'}
                    </BotaoConversa>
                </div>

                {atendimento.status === 'EM_FLUXO_BOT' && (
                    <p className="text-center text-xs text-slate-500">
                        O bot está conversando com o cliente. Ao responder, você assume a conversa.
                    </p>
                )}

                {mostrarHistorico && (
                    <ul className="max-h-48 space-y-1 overflow-y-auto rounded-xl border border-slate-200 bg-white p-3 text-xs">
                        {historico.length === 0 && <li className="text-slate-500">Nenhum evento registrado.</li>}
                        {historico.map((evento) => (
                            <li key={evento.id} className="text-slate-600">
                                <span className="text-slate-400">{formatarDataHora(evento.criadoEm)}</span>{' '}
                                <span className="font-medium">{rotuloEventoHistorico[evento.tipo]}</span>
                                {evento.descricao ? ` — ${evento.descricao}` : ''}
                                {evento.usuarioNome ? ` (${evento.usuarioNome})` : ''}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}

function BotaoConversa({
    children,
    destaque = false,
    ...props
}: {
    children: ReactNode;
    destaque?: boolean;
    onClick: () => void;
    disabled?: boolean;
}) {
    return (
        <button
            type="button"
            {...props}
            className={`flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                destaque
                    ? 'bg-indigo-600 text-white hover:bg-indigo-500'
                    : 'border border-slate-300 text-slate-600 hover:bg-slate-50'
            }`}
        >
            {children}
        </button>
    );
}
