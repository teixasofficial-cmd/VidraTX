import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ChevronRight, MessageCircle } from 'lucide-react';
import { api } from '../lib/api';
import type { StatusAtendimento } from '../types';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { StatusAtendimentoBadge } from '../components/StatusAtendimentoBadge';
import { formatarTelefone } from '../lib/formato';

function formatarQuando(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(iso));
}

function descreverEspera(minutos: number): string {
    if (minutos < 1) {
        return 'agora há pouco';
    }
    if (minutos < 60) {
        return `há ${minutos} min`;
    }
    const horas = Math.floor(minutos / 60);
    if (horas < 48) {
        const resto = minutos % 60;
        return `há ${horas} h${resto > 0 ? ` ${resto} min` : ''}`;
    }
    return `há ${Math.floor(horas / 24)} dias`;
}

type Aba = 'PENDENTES' | 'EM_ATENDIMENTO_HUMANO' | 'ENCERRADO' | 'TODOS';

const ABAS: { chave: Aba; rotulo: string; status?: StatusAtendimento }[] = [
    { chave: 'PENDENTES', rotulo: 'Esperando resposta' },
    { chave: 'EM_ATENDIMENTO_HUMANO', rotulo: 'Em atendimento', status: 'EM_ATENDIMENTO_HUMANO' },
    { chave: 'ENCERRADO', rotulo: 'Encerrados', status: 'ENCERRADO' },
    { chave: 'TODOS', rotulo: 'Todos' },
];

export function AtendimentosPage() {

    const [aba, setAba] = useState<Aba>('PENDENTES');
    const abaAtual = ABAS.find((a) => a.chave === aba) ?? ABAS[0];

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['atendimentos', aba],
        queryFn: () => (aba === 'PENDENTES' ? api.atendimentosPendentes() : api.atendimentos(abaAtual.status)),
        refetchInterval: aba === 'PENDENTES' ? 15000 : undefined,
    });

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold text-slate-900">Atendimentos</h1>
                <p className="mt-1 text-sm text-slate-500">
                    Conversas do WhatsApp — pendentes, em andamento e encerradas.
                </p>
            </div>

            <div className="flex gap-1 overflow-x-auto border-b border-slate-200">
                {ABAS.map((item) => (
                    <button
                        key={item.chave}
                        type="button"
                        onClick={() => setAba(item.chave)}
                        className={`shrink-0 whitespace-nowrap px-4 py-2 text-sm font-medium transition-colors ${
                            aba === item.chave
                                ? 'border-b-2 border-indigo-600 text-indigo-700'
                                : 'text-slate-500 hover:text-slate-700'
                        }`}
                    >
                        {item.rotulo}
                    </button>
                ))}
            </div>

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <ErrorState onRetry={() => refetch()} />
            ) : !data || data.length === 0 ? (
                <EmptyState
                    icon={MessageCircle}
                    title={aba === 'PENDENTES' ? 'Nenhum atendimento pendente' : 'Nenhum atendimento aqui'}
                    description={
                        aba === 'PENDENTES'
                            ? 'Quando um cliente estiver esperando a resposta de uma pessoa, a conversa aparece aqui.'
                            : 'Não há conversas nesta aba no momento.'
                    }
                />
            ) : (
                <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <ul className="divide-y divide-slate-100">
                        {data.map((atendimento) => (
                            <li key={atendimento.id}>
                                <Link
                                    to={`/atendimentos/${atendimento.id}`}
                                    className="flex items-center justify-between px-5 py-4 transition-colors hover:bg-slate-50"
                                >
                                    <div className="flex items-center gap-3">
                                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100 text-sm font-semibold text-indigo-700">
                                            {(atendimento.clienteNome ?? atendimento.telefone)
                                                .charAt(0)
                                                .toUpperCase()}
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-slate-900">
                                                {atendimento.clienteNome ?? formatarTelefone(atendimento.telefone)}
                                            </p>
                                            <p className="text-sm text-slate-500">
                                                {formatarTelefone(atendimento.telefone)}
                                                {atendimento.atendenteNome && (
                                                    <> · {atendimento.atendenteNome}</>
                                                )}
                                            </p>
                                            {atendimento.clienteAguardandoResposta && atendimento.ultimaMensagemClienteEm && (
                                                <p
                                                    className={`text-xs font-medium ${
                                                        atendimento.respostaAtrasada ? 'text-rose-600' : 'text-amber-700'
                                                    }`}
                                                >
                                                    Cliente esperando resposta {descreverEspera(atendimento.minutosEsperando)}
                                                    {atendimento.respostaAtrasada && ' — passou do prazo de resposta'}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <StatusAtendimentoBadge status={atendimento.status} />
                                        <span className="text-xs text-slate-400">
                                            {formatarQuando(atendimento.atualizadoEm)}
                                        </span>
                                        <ChevronRight className="h-4 w-4 text-slate-400" />
                                    </div>
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
