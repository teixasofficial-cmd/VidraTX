import { DndContext, useDroppable, type DragEndEvent } from '@dnd-kit/core';
import { useNavigate } from 'react-router-dom';
import { KanbanCard } from './KanbanCard';
import type { Orcamento, StatusOrcamento } from '../types';

const COLUNAS: { status: StatusOrcamento; titulo: string }[] = [
    { status: 'NOVO_CONTATO', titulo: 'Novo contato' },
    { status: 'PRE_ORCAMENTO', titulo: 'Pré-orçamento' },
    { status: 'VISITA_AGENDADA', titulo: 'Visita agendada' },
    { status: 'MEDIDO', titulo: 'Medido' },
    { status: 'ORCAMENTO_FINAL', titulo: 'Orçamento final' },
    { status: 'ENVIADO', titulo: 'Enviado' },
    { status: 'APROVADO', titulo: 'Aprovado' },
    { status: 'PERDIDO', titulo: 'Perdido' },
];

const STATUS_ARRASTAVEL = new Set<StatusOrcamento>([
    'NOVO_CONTATO', 'PRE_ORCAMENTO', 'VISITA_AGENDADA', 'MEDIDO', 'ORCAMENTO_FINAL',
]);

function colunaDoOrcamento(status: StatusOrcamento): StatusOrcamento {
    return status === 'EXPIRADO' ? 'ENVIADO' : status;
}

interface KanbanBoardProps {
    orcamentos: Orcamento[];
    onMover: (orcamento: Orcamento, novoStatus: StatusOrcamento) => void;
}

function Coluna({
    status,
    titulo,
    orcamentos,
    onAbrir,
}: {
    status: StatusOrcamento;
    titulo: string;
    orcamentos: Orcamento[];
    onAbrir: (id: number) => void;
}) {

    const { setNodeRef, isOver } = useDroppable({ id: status });

    return (
        <div className="flex w-72 shrink-0 flex-col rounded-2xl bg-slate-100/70 p-3">
            <div className="mb-3 flex items-center justify-between px-1">
                <h3 className="text-sm font-semibold text-slate-700">{titulo}</h3>
                <span className="rounded-full bg-white px-2 py-0.5 text-xs font-medium text-slate-500">
                    {orcamentos.length}
                </span>
            </div>
            <div
                ref={setNodeRef}
                className={`flex min-h-[120px] flex-1 flex-col gap-2 rounded-xl p-1 transition-colors ${
                    isOver ? 'bg-indigo-100/60' : ''
                }`}
            >
                {orcamentos.map((orcamento) => (
                    <KanbanCard
                        key={orcamento.id}
                        orcamento={orcamento}
                        arrastavel={STATUS_ARRASTAVEL.has(orcamento.status)}
                        onAbrir={() => onAbrir(orcamento.id)}
                    />
                ))}
            </div>
        </div>
    );
}

export function KanbanBoard({ orcamentos, onMover }: KanbanBoardProps) {

    const navigate = useNavigate();

    function aoTerminarArrastar(evento: DragEndEvent) {

        const { active, over } = evento;

        if (!over) return;

        const orcamento = orcamentos.find((o) => o.id === active.id);
        const novoStatus = over.id as StatusOrcamento;

        if (!orcamento || orcamento.status === novoStatus) return;

        if (!STATUS_ARRASTAVEL.has(orcamento.status)) return;

        if (novoStatus !== 'PERDIDO' && !STATUS_ARRASTAVEL.has(novoStatus)) {
            return;
        }

        onMover(orcamento, novoStatus);
    }

    return (
        <DndContext onDragEnd={aoTerminarArrastar}>
            <div className="flex gap-3 overflow-x-auto pb-3">
                {COLUNAS.map((coluna) => (
                    <Coluna
                        key={coluna.status}
                        status={coluna.status}
                        titulo={coluna.titulo}
                        orcamentos={orcamentos.filter((o) => colunaDoOrcamento(o.status) === coluna.status)}
                        onAbrir={(id) => navigate(`/orcamentos/${id}`)}
                    />
                ))}
            </div>
        </DndContext>
    );
}
