import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { WifiOff } from 'lucide-react';
import { api } from '../lib/api';
import { formatarDataHora } from '../lib/formato';

export function AvisoWhatsappDesconectado() {

    const { data } = useQuery({
        queryKey: ['dashboard-resumo'],
        queryFn: api.dashboardResumo,
        refetchInterval: 60000,
    });

    if (!data || data.whatsappStatus === 'CONECTADO' || data.whatsappStatus === 'CONECTANDO') {
        return null;
    }

    const nunca = data.whatsappStatus === 'NUNCA_CONECTADO';

    return (
        <div className="flex flex-wrap items-center gap-3 border-b border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800 lg:px-8">
            <WifiOff className="h-5 w-5 shrink-0 text-rose-600" />
            <p className="flex-1">
                {nunca
                    ? 'O WhatsApp da empresa ainda não foi conectado: o robô não atende e nenhum cliente recebe mensagens.'
                    : `WhatsApp desconectado${data.whatsappDesconectadoEm ? ` desde ${formatarDataHora(data.whatsappDesconectadoEm)}` : ''}: `
                        + 'os clientes não recebem orçamentos nem respostas até você conectar de novo.'}
            </p>
            <Link
                to="/configuracoes/whatsapp"
                className="rounded-lg bg-rose-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-rose-500"
            >
                Conectar agora
            </Link>
        </div>
    );
}
