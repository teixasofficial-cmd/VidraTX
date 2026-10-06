import type { StatusOrcamento } from '../types';

const estilos: Record<StatusOrcamento, string> = {
    NOVO_CONTATO: 'bg-slate-100 text-slate-600',
    PRE_ORCAMENTO: 'bg-slate-100 text-slate-600',
    VISITA_AGENDADA: 'bg-indigo-100 text-indigo-700',
    MEDIDO: 'bg-indigo-100 text-indigo-700',
    ORCAMENTO_FINAL: 'bg-violet-100 text-violet-700',
    ENVIADO: 'bg-blue-100 text-blue-700',
    APROVADO: 'bg-emerald-100 text-emerald-700',
    PERDIDO: 'bg-rose-100 text-rose-700',
    EXPIRADO: 'bg-amber-100 text-amber-700',
};

const rotulos: Record<StatusOrcamento, string> = {
    NOVO_CONTATO: 'Novo contato',
    PRE_ORCAMENTO: 'Pré-orçamento',
    VISITA_AGENDADA: 'Visita agendada',
    MEDIDO: 'Medido',
    ORCAMENTO_FINAL: 'Pronto para enviar',
    ENVIADO: 'Aguardando resposta do cliente',
    APROVADO: 'Aprovado',
    PERDIDO: 'Perdido',
    EXPIRADO: 'Expirado sem resposta',
};

export function StatusOrcamentoBadge({ status }: { status: StatusOrcamento }) {
    return (
        <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${estilos[status]}`}>
            {rotulos[status]}
        </span>
    );
}
