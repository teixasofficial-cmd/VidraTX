import { useDraggable } from '@dnd-kit/core';
import { AlertTriangle, CalendarClock, Clock, FileText } from 'lucide-react';
import type { Orcamento } from '../types';
import { formatarDataHora, formatarMoeda } from '../lib/formato';
import { rotuloStatusAgendamento } from './StatusAgendamentoBadge';
import { ResponsavelBadge } from './ProximaAcao';
import { envioComProblema } from './EnvioStatus';

function tempoRelativo(iso: string): string {

    const diffMs = Date.now() - new Date(iso).getTime();
    const minutos = Math.floor(diffMs / 60000);

    if (minutos < 1) return 'agora mesmo';
    if (minutos < 60) return `há ${minutos} min`;

    const horas = Math.floor(minutos / 60);
    if (horas < 24) return `há ${horas}h`;

    const dias = Math.floor(horas / 24);
    return `há ${dias}d`;
}

interface KanbanCardProps {
    orcamento: Orcamento;
    arrastavel: boolean;
    onAbrir: () => void;
}

export function KanbanCard({ orcamento, arrastavel, onAbrir }: KanbanCardProps) {

    const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
        id: orcamento.id,
        disabled: !arrastavel,
    });

    const style = transform
        ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`, zIndex: 50 }
        : undefined;

    return (
        <div
            ref={setNodeRef}
            style={style}
            {...(arrastavel ? { ...listeners, ...attributes } : {})}
            onClick={onAbrir}
            className={`cursor-pointer rounded-xl border border-slate-200 bg-white p-3 shadow-sm transition-shadow hover:shadow-md ${
                isDragging ? 'opacity-50' : ''
            } ${arrastavel ? 'cursor-grab active:cursor-grabbing' : ''}`}
        >
            <p className="text-sm font-semibold text-slate-900">{orcamento.clienteNome}</p>

            {orcamento.especificacoes && (
                <p className="mt-1 flex items-start gap-1 text-xs text-slate-500">
                    <FileText className="mt-0.5 h-3 w-3 shrink-0" />
                    <span className="line-clamp-2">{orcamento.especificacoes}</span>
                </p>
            )}

            <div className="mt-2 flex items-center justify-between">
                <span className="text-sm font-semibold text-indigo-600">
                    {formatarMoeda(orcamento.valorTotal)}
                </span>
                <span className="flex items-center gap-1 text-xs text-slate-400">
                    <Clock className="h-3 w-3" />
                    {tempoRelativo(orcamento.atualizadoEm)}
                </span>
            </div>

            {orcamento.medicaoStatus && orcamento.medicaoStatus !== 'REALIZADA' && orcamento.medicaoStatus !== 'CANCELADA' && (
                <p className="mt-1.5 flex items-center gap-1 text-[11px] text-slate-500">
                    <CalendarClock className="h-3 w-3 shrink-0" />
                    Medição: {rotuloStatusAgendamento[orcamento.medicaoStatus].toLowerCase()}
                    {orcamento.medicaoData ? ` · ${formatarDataHora(orcamento.medicaoData)}` : ''}
                </p>
            )}

            {orcamento.vencido && (
                <p className="mt-1.5 rounded bg-amber-50 px-1.5 py-0.5 text-[11px] font-medium text-amber-700">
                    Validade vencida
                </p>
            )}

            {envioComProblema(orcamento.envioStatus) && (
                <p className="mt-1.5 flex items-center gap-1 rounded bg-rose-50 px-1.5 py-0.5 text-[11px] font-medium text-rose-700">
                    <AlertTriangle className="h-3 w-3 shrink-0" />
                    {orcamento.envioStatus === 'SEM_WHATSAPP' ? 'Cliente sem WhatsApp cadastrado' : 'Mensagem não entregue ao cliente'}
                </p>
            )}

            {orcamento.responsavelProximaAcao && orcamento.responsavelProximaAcao !== 'NINGUEM' && orcamento.proximaAcao && (
                <div className="mt-2 border-t border-slate-100 pt-2">
                    <ResponsavelBadge responsavel={orcamento.responsavelProximaAcao} />
                    <p className="mt-1 line-clamp-2 text-[11px] text-slate-500">{orcamento.proximaAcao}</p>
                </div>
            )}
        </div>
    );
}
