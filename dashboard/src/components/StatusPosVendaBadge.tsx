import type { StatusPosVenda } from '../types';

const estilos: Record<StatusPosVenda, string> = {
    ABERTA: 'bg-rose-100 text-rose-700',
    EM_ATENDIMENTO: 'bg-blue-100 text-blue-700',
    RESOLVIDA: 'bg-amber-100 text-amber-700',
    ENCERRADA: 'bg-emerald-100 text-emerald-700',
};

const rotulos: Record<StatusPosVenda, string> = {
    ABERTA: 'Aberta — aguardando atendimento',
    EM_ATENDIMENTO: 'Em atendimento',
    RESOLVIDA: 'Resolvida — pronta para encerrar',
    ENCERRADA: 'Encerrada',
};

export function StatusPosVendaBadge({ status }: { status: StatusPosVenda }) {
    return (
        <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${estilos[status]}`}>
            {rotulos[status]}
        </span>
    );
}
