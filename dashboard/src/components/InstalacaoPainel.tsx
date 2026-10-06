import { useMemo } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import type { Instalacao } from '../types';
import { AgendamentoPainel, type AdaptadorAgendamento } from './AgendamentoPainel';

interface InstalacaoPainelProps {
    ordemServicoId: number;
    podeAgendar: boolean;
}

export function InstalacaoPainel({ ordemServicoId, podeAgendar }: InstalacaoPainelProps) {

    const queryClient = useQueryClient();

    const adaptador = useMemo<AdaptadorAgendamento<Instalacao>>(() => ({
        nome: 'instalação',
        titulo: 'Instalação',
        chave: ['instalacao', ordemServicoId],
        comEquipe: true,
        equipeObrigatoria: true,
        comChecklist: true,
        buscar: () => api.instalacao(ordemServicoId),
        propostas: () => api.propostasInstalacao(ordemServicoId),
        agendar: (d) => api.agendarInstalacao(ordemServicoId, {
            dataAgendada: d.dataAgendada,
            endereco: d.endereco,
            observacoes: d.observacoes,
            equipeResponsavel: d.equipeResponsavel,
        }),
        reagendar: (d) => api.reagendarInstalacao(ordemServicoId, {
            dataAgendada: d.dataAgendada,
            equipeResponsavel: d.equipeResponsavel,
        }),
        aceitarContraproposta: (d) => api.aceitarContrapropostaInstalacao(ordemServicoId, {
            dataAgendada: d.dataAgendada,
            equipeResponsavel: d.equipeResponsavel,
        }),
        recusarContraproposta: (d) => api.recusarContrapropostaInstalacao(ordemServicoId, d),
        confirmarManualmente: () => api.confirmarInstalacaoManualmente(ordemServicoId),
        realizar: (d) => api.realizarInstalacao(ordemServicoId, {
            checklist: d.checklist,
            observacoes: d.observacoes,
        }),
        cancelar: (motivo) => api.cancelarInstalacao(ordemServicoId, { motivo }),
        manterData: () => api.manterDataInstalacao(ordemServicoId),
        invalidarRelacionadas: () => {
            queryClient.invalidateQueries({ queryKey: ['ordens-servico'] });
            queryClient.invalidateQueries({ queryKey: ['ordem-servico', ordemServicoId] });
            queryClient.invalidateQueries({ queryKey: ['dashboard-proximas-acoes'] });
        },
    }), [ordemServicoId, queryClient]);

    return (
        <AgendamentoPainel
            adaptador={adaptador}
            podeAgendar={podeAgendar}
            bloqueioAgendamento="A instalação só pode ser agendada depois que a produção estiver conferida."
        />
    );
}
