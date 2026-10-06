import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
    AlertTriangle,
    ArrowLeft,
    CheckCircle2,
    ClipboardList,
    Clock,
    FileText,
    MessageSquareWarning,
    Pencil,
    RotateCcw,
    Send,
    Trash2,
    XCircle,
} from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { ErrorState } from '../components/ErrorState';
import { StatusOrcamentoBadge } from '../components/StatusOrcamentoBadge';
import { OrcamentoFormModal } from '../components/OrcamentoFormModal';
import { MensagemImagem } from '../components/MensagemImagem';
import { CalculadoraOrcamento } from '../components/CalculadoraOrcamento';
import { MedicaoPainel } from '../components/MedicaoPainel';
import { MotivoPerdaModal } from '../components/MotivoPerdaModal';
import { Modal } from '../components/Modal';
import { ProximaAcaoAviso } from '../components/ProximaAcao';
import { EnvioStatus } from '../components/EnvioStatus';
import { ApiRequestError } from '../lib/apiClient';
import { formatarDataHora, formatarMoeda, formatarTelefone } from '../lib/formato';
import type { EnviarOrcamentoPayload, MotivoPerda, StatusOrcamento } from '../types';

interface MedicaoPendenteNoEnvio {
    mensagem: string;
    valorEsperado: number;
}

function formatarData(iso: string): string {
    const data = iso.length === 10 ? new Date(`${iso}T12:00:00`) : new Date(iso);
    return new Intl.DateTimeFormat('pt-BR').format(data);
}

const STATUS_EDITAVEL: StatusOrcamento[] = [
    'NOVO_CONTATO', 'PRE_ORCAMENTO', 'VISITA_AGENDADA', 'MEDIDO', 'ORCAMENTO_FINAL',
];

const STATUS_ESTIMATIVA: StatusOrcamento[] = ['NOVO_CONTATO', 'PRE_ORCAMENTO', 'VISITA_AGENDADA'];

export function OrcamentoDetalhePage() {

    const { id } = useParams();
    const orcamentoId = Number(id);
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [editandoHeader, setEditandoHeader] = useState(false);
    const [modalPerda, setModalPerda] = useState(false);
    const [erro, setErro] = useState<string | null>(null);
    const [aviso, setAviso] = useState<string | null>(null);
    const [medicaoPendente, setMedicaoPendente] = useState<MedicaoPendenteNoEnvio | null>(null);

    const { data: orcamento, isLoading, isError, refetch } = useQuery({
        queryKey: ['orcamento', orcamentoId],
        queryFn: () => api.orcamento(orcamentoId),
        enabled: Number.isFinite(orcamentoId),
    });

    const { data: fotosWhatsapp } = useQuery({
        queryKey: ['orcamento-fotos-whatsapp', orcamentoId],
        queryFn: () => api.orcamentoFotosWhatsapp(orcamentoId),
        enabled: Number.isFinite(orcamentoId),
    });

    const { data: totais } = useQuery({
        queryKey: ['orcamento-totais', orcamentoId],
        queryFn: () => api.totaisOrcamento(orcamentoId),
        enabled: Number.isFinite(orcamentoId),
    });

    function invalidar() {
        queryClient.invalidateQueries({ queryKey: ['orcamento', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamento-totais', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamento-itens', orcamentoId] });
        queryClient.invalidateQueries({ queryKey: ['orcamentos'] });
        queryClient.invalidateQueries({ queryKey: ['dashboard-resumo'] });
        queryClient.invalidateQueries({ queryKey: ['dashboard-proximas-acoes'] });
    }

    function aposAcao(resposta: { aviso: string | null }) {
        setAviso(resposta.aviso);
        invalidar();
    }

    function tratarErro(excecao: unknown, mensagemPadrao: string) {
        setErro(excecao instanceof ApiRequestError ? excecao.message : mensagemPadrao);
    }

    const excluirMutation = useMutation({
        mutationFn: () => api.excluirOrcamento(orcamentoId),
        onSuccess: () => navigate('/orcamentos'),
        onError: (e) => tratarErro(e, 'Não foi possível excluir o orçamento'),
    });

    const enviarMutation = useMutation({
        mutationFn: async (payload: EnviarOrcamentoPayload) => {
            if (orcamento && orcamento.status !== 'ORCAMENTO_FINAL') {
                await api.moverPipelineOrcamento(orcamentoId, { status: 'ORCAMENTO_FINAL' });
            }
            return api.enviarOrcamento(orcamentoId, payload);
        },
        onSuccess: (resposta) => {
            setMedicaoPendente(null);
            aposAcao(resposta);
            queryClient.invalidateQueries({ queryKey: ['medicao', orcamentoId] });
        },
        onError: (e, payload) => {
            queryClient.invalidateQueries({ queryKey: ['orcamento', orcamentoId] });
            if (e instanceof ApiRequestError && e.erros?.codigo === 'MEDICAO_PENDENTE') {
                setMedicaoPendente({ mensagem: e.message, valorEsperado: payload.valorEsperado ?? valorNaTelaAtual() });
                return;
            }
            setMedicaoPendente(null);
            if (e instanceof ApiRequestError && e.erros?.codigo === 'PRECO_ALTERADO') {
                invalidar();
            }
            tratarErro(e, 'Não foi possível enviar o orçamento');
        },
    });

    const reabrirMutation = useMutation({
        mutationFn: (motivo?: string) => api.reabrirOrcamento(orcamentoId, { motivo }),
        onSuccess: aposAcao,
        onError: (e) => tratarErro(e, 'Não foi possível reabrir o orçamento'),
    });

    const enviarEstimativaMutation = useMutation({
        mutationFn: () => api.enviarEstimativaOrcamento(orcamentoId),
        onSuccess: aposAcao,
        onError: (e) => tratarErro(e, 'Não foi possível enviar a estimativa'),
    });

    const revisarMutation = useMutation({
        mutationFn: () => api.revisarOrcamento(orcamentoId),
        onSuccess: aposAcao,
        onError: (e) => tratarErro(e, 'Não foi possível reabrir o orçamento para revisão'),
    });

    const aprovarMutation = useMutation({
        mutationFn: () => api.aprovarOrcamento(orcamentoId),
        onSuccess: aposAcao,
        onError: (e) => tratarErro(e, 'Não foi possível aprovar o orçamento'),
    });

    const perderMutation = useMutation({
        mutationFn: (payload: { motivoPerda: MotivoPerda; motivoPerdaOutro?: string }) =>
            api.perderOrcamento(orcamentoId, payload),
        onSuccess: () => {
            invalidar();
            setModalPerda(false);
        },
        onError: (e) => tratarErro(e, 'Não foi possível marcar como perdido'),
    });

    const expirarMutation = useMutation({
        mutationFn: () => api.expirarOrcamento(orcamentoId),
        onSuccess: invalidar,
        onError: (e) => tratarErro(e, 'Não foi possível marcar como expirado'),
    });

    const criarOrdemServicoMutation = useMutation({
        mutationFn: () => api.criarOrdemServico({ orcamentoId }),
        onSuccess: (ordem) => navigate(`/ordens-servico/${ordem.id}`),
        onError: (e) => tratarErro(e, 'Não foi possível criar a ordem de serviço'),
    });

    function valorNaTelaAtual(): number {
        return totais?.valorFinal ?? orcamento?.valorTotal ?? 0;
    }

    if (isLoading) {
        return <Spinner />;
    }

    if (isError || !orcamento) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    const editavel = STATUS_EDITAVEL.includes(orcamento.status);
    const podeEnviar = editavel;
    const valorNaTela = totais?.valorFinal ?? orcamento.valorTotal;
    const bloqueiosEnvio = totais?.bloqueiosEnvio ?? [];
    const semWhatsapp = !orcamento.clienteWhatsapp;

    function aoEnviar() {

        setErro(null);
        setAviso(null);

        const confirmado = confirm(
            `Isso vai enviar uma mensagem de WhatsApp para ${orcamento!.clienteNome} ` +
            `informando que o orçamento ficou em ${formatarMoeda(valorNaTela)}. Confirma o envio?`
        );

        if (confirmado) {
            enviarMutation.mutate({ valorEsperado: valorNaTela });
        }
    }

    function aoEnviarEstimativa() {

        setErro(null);
        setAviso(null);

        if (confirm(
            `Enviar a ${orcamento!.clienteNome} uma estimativa de preço (faixa, sem compromisso) ` +
            'com base nas medidas aproximadas? O valor exato sai depois da medição.'
        )) {
            enviarEstimativaMutation.mutate();
        }
    }

    return (
        <div className="max-w-5xl space-y-6">
            <div className="flex flex-wrap items-start gap-3">
                <button
                    type="button"
                    onClick={() => navigate('/orcamentos')}
                    className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
                    aria-label="Voltar"
                >
                    <ArrowLeft className="h-5 w-5" />
                </button>
                <div className="min-w-[12rem] flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                        <h1 className="text-2xl font-semibold text-slate-900">
                            Orçamento #{orcamento.id}
                        </h1>
                        <StatusOrcamentoBadge status={orcamento.status} />
                    </div>
                    <p className="mt-0.5 text-sm text-slate-500">{orcamento.clienteNome}</p>
                </div>
                {editavel && (
                    <>
                        <button
                            type="button"
                            onClick={() => setEditandoHeader(true)}
                            className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                        >
                            <Pencil className="h-4 w-4" />
                            Editar
                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                if (confirm('Excluir este orçamento? Esta ação não pode ser desfeita.')) {
                                    excluirMutation.mutate();
                                }
                            }}
                            className="flex items-center gap-2 rounded-lg border border-rose-200 px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50"
                        >
                            <Trash2 className="h-4 w-4" />
                            Excluir
                        </button>
                    </>
                )}
            </div>

            {erro && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>
            )}
            {aviso && (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">{aviso}</p>
            )}

            <ProximaAcaoAviso responsavel={orcamento.responsavelProximaAcao} texto={orcamento.proximaAcao} />

            {orcamento.alteracaoSolicitadaEm && (
                <div className="flex items-start gap-3 rounded-2xl border border-violet-200 bg-violet-50 p-4 text-sm text-violet-800">
                    <MessageSquareWarning className="mt-0.5 h-5 w-5 shrink-0" />
                    <div className="flex-1">
                        <p className="font-semibold">
                            O cliente pediu alteração em {formatarDataHora(orcamento.alteracaoSolicitadaEm)}
                        </p>
                        {orcamento.alteracaoSolicitadaTexto && (
                            <p className="mt-1 whitespace-pre-wrap italic">"{orcamento.alteracaoSolicitadaTexto}"</p>
                        )}
                        <p className="mt-2 text-xs text-violet-700">
                            Reabra o orçamento para revisar os itens e envie a nova versão — a anterior deixa de valer.
                        </p>
                    </div>
                </div>
            )}

            {orcamento.status === 'ENVIADO' && orcamento.vencido && (
                <p className="flex items-center gap-2 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
                    <AlertTriangle className="h-4 w-4 shrink-0" />
                    A validade deste orçamento já passou: o cliente não consegue mais aprová-lo pelo WhatsApp.
                    Revise e envie de novo, ou registre a aprovação manualmente se ele confirmou por outro canal.
                </p>
            )}

            {orcamento.enviadoEm && (orcamento.status === 'ENVIADO' || orcamento.envioStatus) && (
                <div className="flex flex-wrap items-center gap-2 text-sm text-slate-500">
                    <span>
                        {orcamento.revisaoEnvio > 1 ? `Versão ${orcamento.revisaoEnvio} enviada` : 'Enviado'} em{' '}
                        {formatarDataHora(orcamento.enviadoEm)}
                    </span>
                    <EnvioStatus status={orcamento.envioStatus} erro={orcamento.envioErro} />
                </div>
            )}
            {orcamento.estimativaEnviadaEm && STATUS_EDITAVEL.includes(orcamento.status) && (
                <p className="text-sm text-slate-500">
                    Estimativa enviada ao cliente em {formatarDataHora(orcamento.estimativaEnviadaEm)}.
                </p>
            )}

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">
                    Cliente
                </p>
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <Campo label="Nome" valor={orcamento.clienteNome} />
                    <Campo label="Telefone" valor={formatarTelefone(orcamento.clienteTelefone) || null} />
                    <Campo label="WhatsApp" valor={formatarTelefone(orcamento.clienteWhatsapp) || null} />
                    {orcamento.clienteEmail && (
                        <Campo label="E-mail" valor={orcamento.clienteEmail} className="sm:col-span-3" />
                    )}
                </div>
            </div>

            {orcamento.especificacoes && (
                <div className="rounded-2xl border border-indigo-100 bg-indigo-50/50 p-6">
                    <div className="mb-2 flex items-center gap-2">
                        <FileText className="h-4 w-4 text-indigo-500" />
                        <p className="text-xs font-semibold uppercase tracking-wide text-indigo-600">
                            Especificações enviadas pelo cliente
                        </p>
                    </div>
                    <p className="whitespace-pre-wrap text-sm text-slate-700">
                        {orcamento.especificacoes}
                    </p>

                    {fotosWhatsapp && fotosWhatsapp.length > 0 && (
                        <div className="mt-4 flex flex-wrap gap-3">
                            {fotosWhatsapp.map((foto) => (
                                <MensagemImagem key={foto.id} mensagemId={foto.id} legenda={foto.conteudo} />
                            ))}
                        </div>
                    )}
                </div>
            )}

            {orcamento.motivoPerda && (
                <div className="rounded-2xl border border-rose-100 bg-rose-50/60 p-4 text-sm text-rose-700">
                    Perdido — {orcamento.motivoPerdaOutro || orcamento.motivoPerda.replaceAll('_', ' ').toLowerCase()}
                </div>
            )}

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <Campo
                        label="Válido até"
                        valor={orcamento.validoAte
                            ? formatarData(orcamento.validoAte) + (orcamento.vencido ? ' (vencido)' : '')
                            : 'definido no envio'}
                    />
                    <Campo label="Criado em" valor={formatarData(orcamento.criadoEm)} />
                    {orcamento.observacoes && (
                        <Campo label="Observações" valor={orcamento.observacoes} className="sm:col-span-2" />
                    )}
                </div>
            </div>

            <MedicaoPainel orcamentoId={orcamento.id} />

            {editavel ? (
                <CalculadoraOrcamento orcamentoId={orcamento.id} editavel={editavel} />
            ) : (
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
                        Valor final
                    </p>
                    <p className="text-2xl font-semibold text-slate-900">
                        {formatarMoeda(orcamento.valorTotal)}
                    </p>
                    {orcamento.margemReal != null && (
                        <p className="mt-1 text-xs text-slate-400">
                            Margem real: {orcamento.margemReal.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%
                        </p>
                    )}
                </div>
            )}

            <div className="flex flex-wrap gap-2">
                {podeEnviar && (
                    <button
                        type="button"
                        onClick={aoEnviar}
                        disabled={enviarMutation.isPending || !totais || bloqueiosEnvio.length > 0 || semWhatsapp}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                        title={semWhatsapp
                            ? 'Cadastre o WhatsApp do cliente para enviar por aqui'
                            : bloqueiosEnvio.length > 0
                                ? bloqueiosEnvio.join(' ')
                                : undefined}
                    >
                        <Send className="h-4 w-4" />
                        Enviar orçamento pelo WhatsApp
                    </button>
                )}

                {STATUS_ESTIMATIVA.includes(orcamento.status) && (
                    <button
                        type="button"
                        onClick={aoEnviarEstimativa}
                        disabled={enviarEstimativaMutation.isPending || semWhatsapp}
                        className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                        title="Faixa de preço com base nas medidas aproximadas, antes da medição"
                    >
                        <Send className="h-4 w-4" />
                        Enviar estimativa
                    </button>
                )}

                {(orcamento.status === 'ENVIADO' || orcamento.status === 'EXPIRADO') && (
                    <button
                        type="button"
                        onClick={() => {
                            setErro(null);
                            if (confirm(
                                'Reabrir o orçamento para revisão? A versão enviada deixa de valer e o cliente '
                                + 'não consegue mais aprová-la pelo WhatsApp até você enviar a nova.'
                            )) {
                                revisarMutation.mutate();
                            }
                        }}
                        disabled={revisarMutation.isPending}
                        className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        <RotateCcw className="h-4 w-4" />
                        Revisar orçamento
                    </button>
                )}

                {orcamento.status === 'ENVIADO' && (
                    <>
                        <p className="w-full text-sm text-slate-500">
                            Enviado ao cliente — assim que ele responder pelo WhatsApp, o status
                            muda sozinho. Os botões abaixo são só para marcar manualmente, se
                            precisar (ex.: cliente respondeu por telefone).
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                setErro(null);
                                if (confirm(
                                    `Registrar que ${orcamento.clienteNome} aprovou o orçamento por outro canal? `
                                    + 'O cliente recebe a confirmação pelo WhatsApp e a ordem de serviço é criada.'
                                )) {
                                    aprovarMutation.mutate();
                                }
                            }}
                            disabled={aprovarMutation.isPending}
                            className="flex items-center gap-2 rounded-lg border border-emerald-200 px-4 py-2.5 text-sm font-semibold text-emerald-700 hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <CheckCircle2 className="h-4 w-4" />
                            Marcar como aprovado
                        </button>
                        <button
                            type="button"
                            onClick={() => setModalPerda(true)}
                            disabled={perderMutation.isPending}
                            className="flex items-center gap-2 rounded-lg border border-rose-200 px-4 py-2.5 text-sm font-semibold text-rose-600 hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <XCircle className="h-4 w-4" />
                            Marcar como perdido
                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                if (confirm('Marcar este orçamento como expirado?')) {
                                    expirarMutation.mutate();
                                }
                            }}
                            disabled={expirarMutation.isPending}
                            className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <Clock className="h-4 w-4" />
                            Marcar como expirado
                        </button>
                    </>
                )}

                {orcamento.status === 'PERDIDO' && (
                    <button
                        type="button"
                        onClick={() => {
                            setErro(null);
                            const motivo = prompt(
                                'Reabrir este orçamento perdido? Ele volta para a etapa em que estava (um orçamento '
                                + 'já enviado volta para revisão e precisa ser enviado de novo).\n\n'
                                + 'Motivo (opcional):'
                            );
                            if (motivo !== null) {
                                reabrirMutation.mutate(motivo.trim() || undefined);
                            }
                        }}
                        disabled={reabrirMutation.isPending}
                        className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        <RotateCcw className="h-4 w-4" />
                        Reabrir orçamento
                    </button>
                )}

                {orcamento.status === 'APROVADO' && orcamento.ordemServicoId && (
                    <Link
                        to={`/ordens-servico/${orcamento.ordemServicoId}`}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-500"
                    >
                        <ClipboardList className="h-4 w-4" />
                        Ver ordem de serviço
                    </Link>
                )}

                {orcamento.status === 'APROVADO' && !orcamento.ordemServicoId && (
                    <button
                        type="button"
                        onClick={() => {
                            setErro(null);
                            criarOrdemServicoMutation.mutate();
                        }}
                        disabled={criarOrdemServicoMutation.isPending}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        <ClipboardList className="h-4 w-4" />
                        Criar ordem de serviço
                    </button>
                )}
            </div>

            {editandoHeader && (
                <OrcamentoFormModal
                    orcamento={orcamento}
                    onClose={() => setEditandoHeader(false)}
                    onCreated={() => setEditandoHeader(false)}
                />
            )}

            {medicaoPendente && (
                <Modal title="A medição ainda está em aberto" onClose={() => setMedicaoPendente(null)} largura="sm">
                    <div className="space-y-4">
                        <p className="text-sm text-slate-600">{medicaoPendente.mensagem}</p>
                        <p className="text-xs text-slate-500">Nada foi enviado ainda.</p>
                        <div className="flex flex-col gap-2">
                            <button
                                type="button"
                                disabled={enviarMutation.isPending}
                                onClick={() => enviarMutation.mutate({
                                    valorEsperado: medicaoPendente.valorEsperado,
                                    medicaoPendente: 'CANCELAR',
                                })}
                                className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
                            >
                                Cancelar a visita e enviar o orçamento
                            </button>
                            <button
                                type="button"
                                disabled={enviarMutation.isPending}
                                onClick={() => enviarMutation.mutate({
                                    valorEsperado: medicaoPendente.valorEsperado,
                                    medicaoPendente: 'MANTER',
                                })}
                                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-60"
                            >
                                Manter a visita (conferir medidas) e enviar
                            </button>
                            <button
                                type="button"
                                onClick={() => setMedicaoPendente(null)}
                                className="px-4 py-2 text-sm text-slate-500 hover:text-slate-700"
                            >
                                Voltar sem enviar
                            </button>
                        </div>
                    </div>
                </Modal>
            )}

            {modalPerda && (
                <MotivoPerdaModal
                    enviando={perderMutation.isPending}
                    onClose={() => setModalPerda(false)}
                    onConfirmar={(motivoPerda, motivoPerdaOutro) =>
                        perderMutation.mutate({ motivoPerda, motivoPerdaOutro })
                    }
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
            <p className="mt-0.5 whitespace-pre-wrap text-sm text-slate-800">{valor || '—'}</p>
        </div>
    );
}
