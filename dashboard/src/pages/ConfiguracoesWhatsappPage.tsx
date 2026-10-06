import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AlertTriangle, CheckCircle2, Inbox, Loader2, QrCode, Smartphone } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { PerguntasFrequentesPainel } from '../components/PerguntasFrequentesPainel';
import { ErrorState } from '../components/ErrorState';
import { useAuth } from '../lib/auth';
import { ApiRequestError } from '../lib/apiClient';
import { formatarDataHora, formatarTelefone } from '../lib/formato';
import type { StatusInstanciaWhatsapp } from '../types';

const statusInfo: Record<
    StatusInstanciaWhatsapp,
    { rotulo: string; cor: string; ponto: string }
> = {
    DESCONECTADO: { rotulo: 'Desconectado', cor: 'text-slate-500', ponto: 'bg-slate-400' },
    CONECTANDO: { rotulo: 'Conectando...', cor: 'text-amber-600', ponto: 'bg-amber-500' },
    CONECTADO: { rotulo: 'Conectado', cor: 'text-emerald-600', ponto: 'bg-emerald-500' },
};

export function ConfiguracoesWhatsappPage() {

    const { usuario } = useAuth();
    const podeGerenciar = usuario?.perfil === 'ADMIN' || usuario?.perfil === 'GERENTE';

    if (!podeGerenciar) {
        return (
            <div className="max-w-xl space-y-6">
                <h1 className="text-2xl font-semibold text-slate-900">WhatsApp</h1>
                <p className="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-500 shadow-sm">
                    Apenas ADMIN ou GERENTE podem ver e conectar o WhatsApp da empresa.
                </p>
            </div>
        );
    }

    return (
        <div className="max-w-3xl space-y-6">
            <PainelInstancia />
            <PerguntasFrequentesPainel />
        </div>
    );
}

function PainelInstancia() {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['whatsapp-instancia'],
        queryFn: api.whatsappInstanciaQr,
        refetchInterval: (query) =>
            query.state.data?.status === 'CONECTANDO' ? 3000 : 30000,
    });

    const conectarMutation = useMutation({
        mutationFn: api.whatsappInstanciaConectar,
        onSuccess: (resposta) => {
            queryClient.setQueryData(['whatsapp-instancia'], resposta);
            queryClient.invalidateQueries({ queryKey: ['whatsapp-instancia'] });
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível iniciar a conexão'
            ),
    });

    if (isLoading) {
        return <Spinner />;
    }

    if (isError || !data) {
        return <ErrorState onRetry={() => refetch()} />;
    }

    const info = statusInfo[data.status];

    return (
        <div className="max-w-xl space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-slate-900">WhatsApp</h1>
                <p className="mt-1 text-sm text-slate-500">
                    Conecte o número da empresa para começar a receber e responder
                    clientes pelo fluxo automático.
                </p>
            </div>

            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100">
                        <Smartphone className="h-6 w-6 text-slate-500" />
                    </div>
                    <div className="flex-1">
                        <div className="flex items-center gap-2">
                            <span className={`h-2 w-2 rounded-full ${info.ponto}`} />
                            <span className={`text-sm font-semibold ${info.cor}`}>
                                {info.rotulo}
                            </span>
                        </div>
                        {data.status === 'CONECTADO' && data.numero && (
                            <p className="mt-0.5 text-sm text-slate-500">
                                Número {formatarTelefone(data.numero)}
                                {data.conectadoEm && (
                                    <> · conectado em {formatarDataHora(data.conectadoEm)}</>
                                )}
                            </p>
                        )}
                        {data.status === 'DESCONECTADO' && (
                            <p className="mt-0.5 text-sm text-slate-500">
                                {data.numero
                                    ? `O número ${formatarTelefone(data.numero)} foi desconectado. Conecte de novo para voltar a enviar.`
                                    : 'Nenhum número conectado ainda.'}
                            </p>
                        )}
                    </div>
                </div>

                {data.status === 'CONECTANDO' && (
                    <div className="mt-5 rounded-xl bg-amber-50 px-4 py-3">
                        <div className="flex items-start gap-3">
                            <QrCode className="mt-0.5 h-5 w-5 shrink-0 text-amber-600" />
                            <p className="text-sm text-amber-800">
                                Abra o WhatsApp no celular da empresa em{' '}
                                <strong>Aparelhos conectados → Conectar um aparelho</strong> e
                                escaneie o código abaixo. Esta tela atualiza sozinha assim que conectar.
                            </p>
                        </div>
                        {data.qrCode ? (
                            <img
                                src={data.qrCode}
                                alt="QR code para conectar o WhatsApp"
                                className="mx-auto mt-4 h-64 w-64 rounded-lg bg-white p-2"
                            />
                        ) : (
                            <p className="mt-3 flex items-center justify-center gap-2 text-sm text-amber-700">
                                <Loader2 className="h-4 w-4 animate-spin" />
                                Gerando o código...
                            </p>
                        )}
                    </div>
                )}

                {erro && (
                    <p className="mt-4 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
                        {erro}
                    </p>
                )}

                {data.status === 'CONECTADO' ? (
                    <div className="mt-5 flex items-center gap-2 text-sm font-medium text-emerald-600">
                        <CheckCircle2 className="h-4 w-4" />
                        Tudo certo por aqui
                    </div>
                ) : (
                    <button
                        type="button"
                        onClick={() => {
                            setErro(null);
                            conectarMutation.mutate();
                        }}
                        disabled={conectarMutation.isPending || data.status === 'CONECTANDO'}
                        className="mt-5 flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {(conectarMutation.isPending || data.status === 'CONECTANDO') && (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        )}
                        Conectar WhatsApp
                    </button>
                )}
            </div>

            {(data.mensagensNaFila > 0 || data.mensagensComFalha > 0) && (
                <div className="space-y-2 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Mensagens para clientes</p>
                    {data.mensagensNaFila > 0 && (
                        <p className="flex items-center gap-2 text-sm text-slate-600">
                            <Inbox className="h-4 w-4 text-slate-400" />
                            {data.mensagensNaFila === 1
                                ? '1 mensagem esperando para ser enviada'
                                : `${data.mensagensNaFila} mensagens esperando para ser enviadas`}
                            {data.status !== 'CONECTADO' && ' — saem assim que o WhatsApp conectar'}
                        </p>
                    )}
                    {data.mensagensComFalha > 0 && (
                        <p className="flex items-center gap-2 text-sm text-rose-600">
                            <AlertTriangle className="h-4 w-4" />
                            {data.mensagensComFalha === 1
                                ? '1 mensagem não pôde ser entregue'
                                : `${data.mensagensComFalha} mensagens não puderam ser entregues`}
                            {' '}nos últimos 7 dias
                            {' '}— veja as ações na visão geral.
                        </p>
                    )}
                </div>
            )}
        </div>
    );
}
