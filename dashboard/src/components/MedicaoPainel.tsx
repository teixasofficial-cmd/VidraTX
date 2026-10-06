import { useMemo } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import type { Medicao } from '../types';
import { AgendamentoPainel, type AdaptadorAgendamento } from './AgendamentoPainel';

interface MedicaoPainelProps {
    orcamentoId: number;
}

export function MedicaoPainel({ orcamentoId }: MedicaoPainelProps) {

    const queryClient = useQueryClient();

    const adaptador = useMemo<AdaptadorAgendamento<Medicao>>(() => ({
        nome: 'medição',
        titulo: 'Medição',
        chave: ['medicao', orcamentoId],
        comEquipe: false,
        comChecklist: false,
        buscar: () => api.medicao(orcamentoId),
        propostas: () => api.propostasMedicao(orcamentoId),
        agendar: (d) => api.agendarMedicao(orcamentoId, {
            dataAgendada: d.dataAgendada,
            endereco: d.endereco,
            observacoes: d.observacoes,
        }),
        reagendar: (d) => api.reagendarMedicao(orcamentoId, { dataAgendada: d.dataAgendada }),
        aceitarContraproposta: (d) => api.aceitarContrapropostaMedicao(orcamentoId, { dataAgendada: d.dataAgendada }),
        recusarContraproposta: (d) => api.recusarContrapropostaMedicao(orcamentoId, d),
        confirmarManualmente: () => api.confirmarMedicaoManualmente(orcamentoId),
        realizar: (d) => api.realizarMedicao(orcamentoId, { observacoes: d.observacoes }),
        cancelar: (motivo) => api.cancelarMedicao(orcamentoId, { motivo }),
        manterData: () => api.manterDataMedicao(orcamentoId),
        invalidarRelacionadas: () => {
            queryClient.invalidateQueries({ queryKey: ['orcamento', orcamentoId] });
            queryClient.invalidateQueries({ queryKey: ['orcamentos'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard-proximas-acoes'] });
        },
    }), [orcamentoId, queryClient]);

    return <AgendamentoPainel adaptador={adaptador} />;
}
