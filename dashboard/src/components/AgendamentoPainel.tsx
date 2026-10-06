import { useState, type FormEvent, type ReactNode } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
    CalendarCheck,
    CalendarClock,
    CheckCircle2,
    History,
    Loader2,
    PhoneCall,
    XCircle,
} from 'lucide-react';
import { ApiRequestError } from '../lib/apiClient';
import { formatarDataHora, paraInputDataHora } from '../lib/formato';
import { Spinner } from './Spinner';
import { StatusAgendamentoBadge } from './StatusAgendamentoBadge';
import { FormField, inputClass } from './FormField';
import { ProximaAcaoAviso } from './ProximaAcao';
import { EnvioStatus, envioComProblema } from './EnvioStatus';
import type {
    AgendamentoNegociacao,
    PropostaAgendamento,
    RecusarContrapropostaPayload,
    StatusAgendamento,
} from '../types';

export interface AgendamentoBase extends AgendamentoNegociacao {
    id: number;
    endereco: string | null;
    observacoes: string | null;
    dataRealizada: string | null;
    equipeResponsavel?: string | null;
    checklist?: string | null;
}

export interface DadosAgendamento {
    dataAgendada: string;
    endereco?: string;
    observacoes?: string;
    equipeResponsavel?: string;
    checklist?: string;
}

export interface AdaptadorAgendamento<T extends AgendamentoBase> {
    nome: string;
    titulo: string;
    chave: readonly unknown[];
    comEquipe: boolean;
    equipeObrigatoria?: boolean;
    comChecklist: boolean;
    buscar: () => Promise<T>;
    propostas: () => Promise<PropostaAgendamento[]>;
    agendar: (dados: DadosAgendamento) => Promise<T>;
    reagendar: (dados: DadosAgendamento) => Promise<T>;
    aceitarContraproposta: (dados: DadosAgendamento) => Promise<T>;
    recusarContraproposta: (dados: RecusarContrapropostaPayload) => Promise<T>;
    confirmarManualmente: () => Promise<T>;
    realizar: (dados: DadosAgendamento) => Promise<T>;
    cancelar: (motivo?: string) => Promise<T>;
    manterData: () => Promise<T>;
    invalidarRelacionadas: () => void;
}

type Formulario =
    | 'agendar'
    | 'reagendar'
    | 'realizar'
    | 'aceitar-contraproposta'
    | 'recusar-contraproposta'
    | 'cancelar';

const PODE_REAGENDAR_OU_CANCELAR: StatusAgendamento[] = [
    'PROPOSTA_ENVIADA',
    'AGENDADA',
    'RECUSADA_CLIENTE',
    'CONTRAPROPOSTA_CLIENTE',
    'REAGENDAMENTO_NECESSARIO',
];

function hojeOuAntes(iso: string): boolean {
    const fimDeHoje = new Date();
    fimDeHoje.setHours(23, 59, 59, 999);
    return new Date(iso).getTime() <= fimDeHoje.getTime();
}

export function AgendamentoPainel<T extends AgendamentoBase>({
    adaptador,
    podeAgendar = true,
    bloqueioAgendamento,
}: {
    adaptador: AdaptadorAgendamento<T>;
    podeAgendar?: boolean;
    bloqueioAgendamento?: string;
}) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);
    const [aviso, setAviso] = useState<string | null>(null);
    const [formulario, setFormulario] = useState<Formulario | null>(null);
    const [mostrarPropostas, setMostrarPropostas] = useState(false);

    const [dataAgendada, setDataAgendada] = useState('');
    const [endereco, setEndereco] = useState('');
    const [equipe, setEquipe] = useState('');
    const [observacoes, setObservacoes] = useState('');
    const [checklist, setChecklist] = useState('');
    const [motivo, setMotivo] = useState('');

    const { data: agendamento, isLoading, error } = useQuery({
        queryKey: adaptador.chave,
        queryFn: adaptador.buscar,
        retry: false,
    });

    const propostas = useQuery({
        queryKey: [...adaptador.chave, 'propostas'],
        queryFn: adaptador.propostas,
        enabled: mostrarPropostas && !!agendamento,
    });

    const naoAgendado = error instanceof ApiRequestError && error.status === 404;
    const erroInesperado = error && !naoAgendado;

    const acao = useMutation({
        mutationFn: async (tipo: Formulario | 'confirmar-manualmente' | 'manter-data'): Promise<T> => {

            const dados: DadosAgendamento = {
                dataAgendada,
                endereco: endereco.trim() || undefined,
                observacoes: observacoes.trim() || undefined,
                equipeResponsavel: adaptador.comEquipe ? equipe.trim() || undefined : undefined,
                checklist: adaptador.comChecklist ? checklist.trim() || undefined : undefined,
            };

            switch (tipo) {
                case 'agendar':
                    return adaptador.agendar(dados);
                case 'reagendar':
                    return adaptador.reagendar(dados);
                case 'aceitar-contraproposta':
                    return adaptador.aceitarContraproposta(dados);
                case 'recusar-contraproposta':
                    return adaptador.recusarContraproposta({
                        dataAgendada,
                        motivo: motivo.trim() || undefined,
                        equipeResponsavel: dados.equipeResponsavel,
                    });
                case 'realizar':
                    return adaptador.realizar(dados);
                case 'cancelar':
                    return adaptador.cancelar(motivo.trim() || undefined);
                case 'confirmar-manualmente':
                    return adaptador.confirmarManualmente();
                case 'manter-data':
                    return adaptador.manterData();
            }
        },
        onSuccess: (resposta) => {
            setAviso(resposta.aviso);
            queryClient.setQueryData(adaptador.chave, resposta);
            queryClient.invalidateQueries({ queryKey: adaptador.chave });
            adaptador.invalidarRelacionadas();
            setFormulario(null);
        },
        onError: (e) => {
            setErro(e instanceof ApiRequestError ? e.message : `Não foi possível atualizar a ${adaptador.nome}`);
        },
    });

    function abrirFormulario(tipo: Formulario) {
        setErro(null);
        setAviso(null);
        setDataAgendada(
            tipo === 'aceitar-contraproposta' ? paraInputDataHora(agendamento?.contrapropostaData) : ''
        );
        setEndereco(agendamento?.endereco ?? '');
        setEquipe(agendamento?.equipeResponsavel ?? '');
        setObservacoes('');
        setChecklist('');
        setMotivo('');
        setFormulario(tipo);
    }

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);

        if (formulario) {
            acao.mutate(formulario);
        }
    }

    const salvando = acao.isPending;

    const campoData = (rotulo: string) => (
        <FormField label={rotulo}>
            <input
                required
                type="datetime-local"
                value={dataAgendada}
                onChange={(e) => setDataAgendada(e.target.value)}
                className={inputClass}
            />
        </FormField>
    );

    const campoEquipe = adaptador.comEquipe && (
        <FormField label={adaptador.equipeObrigatoria ? 'Equipe/responsável' : 'Equipe/responsável (opcional)'}>
            <input
                required={adaptador.equipeObrigatoria}
                maxLength={120}
                value={equipe}
                onChange={(e) => setEquipe(e.target.value)}
                className={inputClass}
                placeholder="Ex.: João e Pedro"
            />
        </FormField>
    );

    const botoes = (rotulo: string, cor: 'indigo' | 'emerald' | 'rose' = 'indigo') => (
        <div className="flex justify-end gap-2">
            <button
                type="button"
                onClick={() => setFormulario(null)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
            >
                Voltar
            </button>
            <button
                type="submit"
                disabled={salvando}
                className={`flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60 ${
                    cor === 'emerald'
                        ? 'bg-emerald-600 hover:bg-emerald-500'
                        : cor === 'rose'
                            ? 'bg-rose-600 hover:bg-rose-500'
                            : 'bg-indigo-600 hover:bg-indigo-500'
                }`}
            >
                {salvando && <Loader2 className="h-4 w-4 animate-spin" />}
                {rotulo}
            </button>
        </div>
    );

    const formularioAgendar = (
        <form onSubmit={aoSubmeter} className="space-y-3 rounded-2xl border border-slate-200 bg-white p-5">
            {campoData('Data e hora a propor ao cliente')}
            <FormField label="Endereço (opcional)">
                <input value={endereco} onChange={(e) => setEndereco(e.target.value)} className={inputClass} />
            </FormField>
            {campoEquipe}
            <FormField label="Observações (opcional)">
                <textarea
                    value={observacoes}
                    onChange={(e) => setObservacoes(e.target.value)}
                    className={inputClass}
                    rows={2}
                />
            </FormField>
            <p className="text-xs text-slate-500">
                O cliente recebe a proposta pelo WhatsApp e confirma, recusa ou sugere outra data.
            </p>
            {botoes('Enviar proposta')}
        </form>
    );

    return (
        <div className="space-y-3">
            <h2 className="text-lg font-semibold text-slate-900">{adaptador.titulo}</h2>

            {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}
            {aviso && <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">{aviso}</p>}

            {isLoading ? (
                <Spinner />
            ) : erroInesperado ? (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
                    Não foi possível carregar a {adaptador.nome}.
                </p>
            ) : naoAgendado ? (
                !podeAgendar ? (
                    <p className="rounded-2xl border border-dashed border-slate-200 bg-white px-5 py-4 text-sm text-slate-500">
                        {bloqueioAgendamento}
                    </p>
                ) : formulario === 'agendar' ? (
                    formularioAgendar
                ) : (
                    <button
                        type="button"
                        onClick={() => abrirFormulario('agendar')}
                        className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
                    >
                        <CalendarClock className="h-4 w-4" />
                        Agendar {adaptador.nome}
                    </button>
                )
            ) : agendamento ? (
                <div className="space-y-3 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                        <StatusAgendamentoBadge status={agendamento.status} />
                        <span className="text-sm font-medium text-slate-700">
                            {formatarDataHora(agendamento.dataAgendada)}
                        </span>
                    </div>

                    <ProximaAcaoAviso
                        responsavel={agendamento.responsavelProximaAcao}
                        texto={agendamento.proximaAcao}
                    />

                    {agendamento.envioStatus && agendamento.status === 'PROPOSTA_ENVIADA' && (
                        <div
                            className={`flex flex-wrap items-center gap-2 rounded-lg px-3 py-2 text-sm ${
                                envioComProblema(agendamento.envioStatus) ? 'bg-rose-50' : 'bg-slate-50'
                            }`}
                        >
                            <span className="text-slate-600">
                                Proposta{agendamento.versaoProposta ? ` nº ${agendamento.versaoProposta}` : ''} pelo WhatsApp:
                            </span>
                            <EnvioStatus status={agendamento.envioStatus} erro={agendamento.envioErro} />
                        </div>
                    )}

                    <ExplicacaoStatus agendamento={agendamento} nome={adaptador.nome} />

                    {agendamento.status === 'AGENDADA' && agendamento.cancelamentoSolicitadoEm && (
                        <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-800">
                            <p className="font-medium">
                                O cliente pediu pelo WhatsApp para cancelar
                                ({formatarDataHora(agendamento.cancelamentoSolicitadoEm)}):
                            </p>
                            {agendamento.cancelamentoSolicitadoTexto && (
                                <p className="mt-1 whitespace-pre-wrap italic">
                                    "{agendamento.cancelamentoSolicitadoTexto}"
                                </p>
                            )}
                            <p className="mt-2 text-xs text-rose-700">
                                A data continua marcada até alguém decidir: confirme com o cliente e cancele,
                                reagende ou mantenha a data.
                            </p>
                        </div>
                    )}

                    {agendamento.endereco && <p className="text-sm text-slate-600">{agendamento.endereco}</p>}
                    {agendamento.equipeResponsavel && (
                        <p className="text-sm text-slate-600">
                            <span className="font-medium text-slate-700">Equipe: </span>
                            {agendamento.equipeResponsavel}
                        </p>
                    )}
                    {agendamento.dataRealizada && (
                        <p className="text-sm text-slate-500">
                            Realizada em {formatarDataHora(agendamento.dataRealizada)}
                        </p>
                    )}
                    {agendamento.checklist && (
                        <p className="whitespace-pre-wrap text-sm text-slate-600">
                            <span className="font-medium text-slate-700">Checklist: </span>
                            {agendamento.checklist}
                        </p>
                    )}
                    {agendamento.observacoes && (
                        <p className="whitespace-pre-wrap text-sm text-slate-600">{agendamento.observacoes}</p>
                    )}

                    {formulario === 'aceitar-contraproposta' && (
                        <SubFormulario onSubmit={aoSubmeter}>
                            {campoData('Data e hora combinadas com o cliente')}
                            {campoEquipe}
                            <p className="text-xs text-slate-500">
                                O cliente recebe a confirmação pelo WhatsApp.
                            </p>
                            {botoes('Confirmar com esta data', 'emerald')}
                        </SubFormulario>
                    )}

                    {formulario === 'recusar-contraproposta' && (
                        <SubFormulario onSubmit={aoSubmeter}>
                            {campoData('Nova data e hora a propor')}
                            <FormField label="Motivo (vai na mensagem ao cliente)">
                                <input
                                    value={motivo}
                                    onChange={(e) => setMotivo(e.target.value)}
                                    className={inputClass}
                                    maxLength={300}
                                    placeholder="não temos disponibilidade nesse horário"
                                />
                            </FormField>
                            {campoEquipe}
                            {botoes('Recusar e propor esta data')}
                        </SubFormulario>
                    )}

                    {formulario === 'reagendar' && (
                        <SubFormulario onSubmit={aoSubmeter}>
                            {campoData('Nova data e hora')}
                            {campoEquipe}
                            {agendamento.status === 'AGENDADA' && (
                                <p className="text-xs text-amber-700">
                                    A data já confirmada deixa de valer: o cliente precisa confirmar a nova.
                                </p>
                            )}
                            {botoes(agendamento.status === 'AGENDADA' ? 'Reagendar' : 'Propor data')}
                        </SubFormulario>
                    )}

                    {formulario === 'realizar' && (
                        <SubFormulario onSubmit={aoSubmeter}>
                            {adaptador.comChecklist && (
                                <FormField label="Checklist (opcional)">
                                    <textarea
                                        value={checklist}
                                        onChange={(e) => setChecklist(e.target.value)}
                                        className={inputClass}
                                        rows={2}
                                    />
                                </FormField>
                            )}
                            <FormField label="Observações (opcional)">
                                <textarea
                                    value={observacoes}
                                    onChange={(e) => setObservacoes(e.target.value)}
                                    className={inputClass}
                                    rows={2}
                                />
                            </FormField>
                            {botoes(`Confirmar ${adaptador.nome} realizada`, 'emerald')}
                        </SubFormulario>
                    )}

                    {formulario === 'cancelar' && (
                        <SubFormulario onSubmit={aoSubmeter}>
                            <FormField label="Motivo (opcional — vai no aviso ao cliente)">
                                <input
                                    value={motivo}
                                    onChange={(e) => setMotivo(e.target.value)}
                                    className={inputClass}
                                    maxLength={300}
                                />
                            </FormField>
                            <p className="text-xs text-slate-500">O cliente é avisado do cancelamento pelo WhatsApp.</p>
                            {botoes(`Cancelar ${adaptador.nome}`, 'rose')}
                        </SubFormulario>
                    )}

                    {agendamento.status === 'CANCELADA' && (
                        !podeAgendar ? (
                            <p className="rounded-lg bg-slate-50 px-3 py-2 text-sm text-slate-600">
                                {bloqueioAgendamento}
                            </p>
                        ) : formulario === 'agendar' ? (
                            formularioAgendar
                        ) : (
                            <button
                                type="button"
                                onClick={() => abrirFormulario('agendar')}
                                className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
                            >
                                <CalendarClock className="h-4 w-4" />
                                Agendar novamente
                            </button>
                        )
                    )}

                    {PODE_REAGENDAR_OU_CANCELAR.includes(agendamento.status) && !formulario && (
                        <div className="flex flex-wrap gap-2 border-t border-slate-100 pt-3">
                            {agendamento.status === 'CONTRAPROPOSTA_CLIENTE' && (
                                <>
                                    <BotaoAcao cor="emerald" onClick={() => abrirFormulario('aceitar-contraproposta')}>
                                        <CheckCircle2 className="h-4 w-4" />
                                        Aceitar a sugestão
                                    </BotaoAcao>
                                    <BotaoAcao onClick={() => abrirFormulario('recusar-contraproposta')}>
                                        <XCircle className="h-4 w-4" />
                                        Recusar e propor outra
                                    </BotaoAcao>
                                </>
                            )}

                            {agendamento.status === 'PROPOSTA_ENVIADA' && (
                                <BotaoAcao
                                    cor="emerald"
                                    disabled={salvando}
                                    onClick={() => {
                                        if (confirm(
                                            `Registrar que o cliente confirmou a ${adaptador.nome} por outro canal `
                                            + '(telefone, pessoalmente)? Fica registrado no histórico que a confirmação '
                                            + 'não veio pelo WhatsApp.'
                                        )) {
                                            setErro(null);
                                            acao.mutate('confirmar-manualmente');
                                        }
                                    }}
                                >
                                    <PhoneCall className="h-4 w-4" />
                                    Cliente confirmou por telefone
                                </BotaoAcao>
                            )}

                            {agendamento.status !== 'CONTRAPROPOSTA_CLIENTE' && (
                                <BotaoAcao onClick={() => abrirFormulario('reagendar')}>
                                    <CalendarClock className="h-4 w-4" />
                                    {agendamento.status === 'AGENDADA' ? 'Reagendar' : 'Propor outra data'}
                                </BotaoAcao>
                            )}

                            {agendamento.status === 'AGENDADA' && agendamento.cancelamentoSolicitadoEm && (
                                <BotaoAcao
                                    disabled={salvando}
                                    onClick={() => {
                                        if (confirm(
                                            `Manter a ${adaptador.nome} em ${formatarDataHora(agendamento.dataAgendada)}? `
                                            + 'O cliente recebe pelo WhatsApp que a data continua marcada.'
                                        )) {
                                            setErro(null);
                                            acao.mutate('manter-data');
                                        }
                                    }}
                                >
                                    <CalendarCheck className="h-4 w-4" />
                                    Manter a data
                                </BotaoAcao>
                            )}

                            {agendamento.status === 'AGENDADA' && (
                                <BotaoAcao
                                    cor="emerald"
                                    disabled={!hojeOuAntes(agendamento.dataAgendada)}
                                    title={hojeOuAntes(agendamento.dataAgendada)
                                        ? undefined
                                        : 'Só pode ser marcada como realizada a partir do dia agendado'}
                                    onClick={() => abrirFormulario('realizar')}
                                >
                                    <CheckCircle2 className="h-4 w-4" />
                                    Marcar como realizada
                                </BotaoAcao>
                            )}

                            <BotaoAcao cor="rose" onClick={() => abrirFormulario('cancelar')}>
                                <XCircle className="h-4 w-4" />
                                Cancelar
                            </BotaoAcao>
                        </div>
                    )}

                    <div className="border-t border-slate-100 pt-3">
                        <button
                            type="button"
                            onClick={() => setMostrarPropostas((v) => !v)}
                            className="flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-slate-700"
                        >
                            <History className="h-4 w-4" />
                            {mostrarPropostas ? 'Ocultar' : 'Ver'} histórico de datas propostas
                        </button>
                        {mostrarPropostas && (
                            <ListaPropostas
                                propostas={propostas.data}
                                carregando={propostas.isLoading}
                                erro={propostas.isError}
                            />
                        )}
                    </div>
                </div>
            ) : null}
        </div>
    );
}

function SubFormulario({ onSubmit, children }: { onSubmit: (e: FormEvent) => void; children: ReactNode }) {
    return (
        <form onSubmit={onSubmit} className="space-y-3 border-t border-slate-100 pt-3">
            {children}
        </form>
    );
}

function BotaoAcao({
    cor = 'slate',
    children,
    ...props
}: {
    cor?: 'slate' | 'emerald' | 'rose';
    children: ReactNode;
    onClick: () => void;
    disabled?: boolean;
    title?: string;
}) {
    const estilo = cor === 'emerald'
        ? 'border-emerald-200 text-emerald-700 hover:bg-emerald-50'
        : cor === 'rose'
            ? 'border-rose-200 text-rose-600 hover:bg-rose-50'
            : 'border-slate-300 text-slate-600 hover:bg-slate-50';

    return (
        <button
            type="button"
            {...props}
            className={`flex items-center gap-2 rounded-lg border px-3 py-1.5 text-sm font-medium disabled:cursor-not-allowed disabled:opacity-50 ${estilo}`}
        >
            {children}
        </button>
    );
}

function ExplicacaoStatus({ agendamento, nome }: { agendamento: AgendamentoBase; nome: string }) {

    switch (agendamento.status) {
        case 'PROPOSTA_ENVIADA':
            return (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
                    Data proposta ao cliente pelo WhatsApp — ainda não confirmada. A {nome} só é certa
                    depois que o cliente confirmar.
                </p>
            );
        case 'RECUSADA_CLIENTE':
            return (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">
                    O cliente não pode nessa data. Proponha uma nova data.
                </p>
            );
        case 'REAGENDAMENTO_NECESSARIO':
            return (
                <p className="rounded-lg bg-orange-50 px-3 py-2 text-sm text-orange-800">
                    Esta data não vale mais (a proposta venceu sem resposta, o horário foi ocupado ou o
                    cliente pediu para remarcar). Proponha uma nova data.
                </p>
            );
        case 'CONTRAPROPOSTA_CLIENTE':
            return (
                <div className="rounded-lg bg-violet-50 px-3 py-2 text-sm text-violet-700">
                    <p className="font-medium">
                        O cliente sugeriu outra data
                        {agendamento.contrapropostaEm ? ` em ${formatarDataHora(agendamento.contrapropostaEm)}` : ''}:
                    </p>
                    <p className="mt-1 whitespace-pre-wrap italic">"{agendamento.contrapropostaTexto}"</p>
                    {agendamento.contrapropostaData ? (
                        <p className="mt-2 text-xs text-violet-600">
                            Entendemos: {formatarDataHora(agendamento.contrapropostaData)}. Confira antes de aceitar.
                        </p>
                    ) : (
                        <p className="mt-2 text-xs text-violet-600">
                            Não foi possível entender uma data exata: combine com o cliente e informe ao aceitar.
                        </p>
                    )}
                </div>
            );
        case 'CANCELADA':
            return <p className="rounded-lg bg-slate-50 px-3 py-2 text-sm text-slate-600">{`A ${nome} foi cancelada.`}</p>;
        default:
            return null;
    }
}

const rotuloStatusProposta: Record<PropostaAgendamento['status'], string> = {
    PENDENTE: 'Aguardando resposta',
    ACEITA: 'Aceita',
    RECUSADA: 'Recusada',
    SUBSTITUIDA: 'Substituída',
    CANCELADA: 'Cancelada',
    EXPIRADA: 'Venceu sem resposta',
};

function ListaPropostas({
    propostas,
    carregando,
    erro,
}: {
    propostas: PropostaAgendamento[] | undefined;
    carregando: boolean;
    erro: boolean;
}) {

    if (carregando) {
        return <Spinner />;
    }

    if (erro || !propostas) {
        return <p className="mt-2 text-sm text-rose-600">Não foi possível carregar o histórico.</p>;
    }

    if (propostas.length === 0) {
        return <p className="mt-2 text-sm text-slate-500">Nenhuma proposta registrada.</p>;
    }

    return (
        <ol className="mt-2 space-y-2">
            {propostas.map((proposta) => (
                <li key={proposta.id} className="rounded-lg border border-slate-100 px-3 py-2 text-sm">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className="font-medium text-slate-800">
                            {proposta.origem === 'EMPRESA' ? 'Empresa propôs' : 'Cliente sugeriu'}
                            {proposta.dataProposta ? ` ${formatarDataHora(proposta.dataProposta)}` : ''}
                        </span>
                        <span className="text-xs text-slate-500">{rotuloStatusProposta[proposta.status]}</span>
                    </div>
                    {proposta.textoCliente && (
                        <p className="mt-1 italic text-slate-600">"{proposta.textoCliente}"</p>
                    )}
                    {proposta.motivo && <p className="mt-1 text-slate-600">Motivo: {proposta.motivo}</p>}
                    <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-400">
                        <span>
                            {formatarDataHora(proposta.criadoEm)}
                            {proposta.criadoPorNome ? ` · ${proposta.criadoPorNome}` : ''}
                        </span>
                        {proposta.respondidoEm && (
                            <span>
                                respondida em {formatarDataHora(proposta.respondidoEm)}
                                {proposta.respondidoPeloCliente
                                    ? ' pelo cliente'
                                    : proposta.respondidoPorNome
                                        ? ` por ${proposta.respondidoPorNome}`
                                        : ''}
                            </span>
                        )}
                        {proposta.origem === 'EMPRESA' && <EnvioStatus status={proposta.envioStatus} />}
                    </div>
                </li>
            ))}
        </ol>
    );
}
