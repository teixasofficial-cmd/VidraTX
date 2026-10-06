import type { StatusAgendamento } from '../types';

const estilos: Record<StatusAgendamento, string> = {
    PROPOSTA_ENVIADA: 'bg-amber-100 text-amber-700',
    CONTRAPROPOSTA_CLIENTE: 'bg-violet-100 text-violet-700',
    RECUSADA_CLIENTE: 'bg-rose-100 text-rose-700',
    REAGENDAMENTO_NECESSARIO: 'bg-orange-100 text-orange-800',
    AGENDADA: 'bg-blue-100 text-blue-700',
    REALIZADA: 'bg-emerald-100 text-emerald-700',
    CANCELADA: 'bg-slate-200 text-slate-600',
};

export const rotuloStatusAgendamento: Record<StatusAgendamento, string> = {
    PROPOSTA_ENVIADA: 'Aguardando confirmação do cliente',
    CONTRAPROPOSTA_CLIENTE: 'Cliente sugeriu outra data',
    RECUSADA_CLIENTE: 'Cliente recusou a data',
    REAGENDAMENTO_NECESSARIO: 'Precisa de nova data',
    AGENDADA: 'Confirmada',
    REALIZADA: 'Realizada',
    CANCELADA: 'Cancelada',
};

export function StatusAgendamentoBadge({ status }: { status: StatusAgendamento }) {
    return (
        <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${estilos[status]}`}>
            {rotuloStatusAgendamento[status]}
        </span>
    );
}
