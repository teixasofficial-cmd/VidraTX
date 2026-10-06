import type { StatusAtendimento } from '../types';

const estilos: Record<StatusAtendimento, string> = {
    EM_FLUXO_BOT: 'bg-slate-100 text-slate-600',
    AGUARDANDO_ATENDENTE: 'bg-amber-100 text-amber-700',
    EM_ATENDIMENTO_HUMANO: 'bg-blue-100 text-blue-700',
    ENCERRADO: 'bg-slate-100 text-slate-500',
};

const rotulos: Record<StatusAtendimento, string> = {
    EM_FLUXO_BOT: 'Com o bot',
    AGUARDANDO_ATENDENTE: 'Aguardando atendente',
    EM_ATENDIMENTO_HUMANO: 'Em atendimento',
    ENCERRADO: 'Encerrado',
};

export function StatusAtendimentoBadge({ status }: { status: StatusAtendimento }) {
    return (
        <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${estilos[status]}`}>
            {rotulos[status]}
        </span>
    );
}
