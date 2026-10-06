import type { StatusProducao } from '../types';

const estilos: Record<StatusProducao, string> = {
    AGUARDANDO_PRODUCAO: 'bg-slate-100 text-slate-600',
    EM_PRODUCAO: 'bg-blue-100 text-blue-700',
    PRONTO: 'bg-amber-100 text-amber-700',
    CONFERIDO: 'bg-emerald-100 text-emerald-700',
};

const rotulos: Record<StatusProducao, string> = {
    AGUARDANDO_PRODUCAO: 'Aguardando início da produção',
    EM_PRODUCAO: 'Em produção',
    PRONTO: 'Pronto — aguardando conferência',
    CONFERIDO: 'Conferido',
};

export function StatusProducaoBadge({ status }: { status: StatusProducao }) {
    return (
        <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${estilos[status]}`}>
            {rotulos[status]}
        </span>
    );
}
