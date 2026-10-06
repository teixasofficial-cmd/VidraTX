import { AlertTriangle, Check, CheckCheck, Clock } from 'lucide-react';
import type { StatusEnvio } from '../types';

const rotulos: Record<StatusEnvio, string> = {
    PENDENTE: 'Na fila de envio',
    ENVIANDO: 'Enviando',
    ENVIADA: 'Enviada',
    ENTREGUE: 'Entregue',
    LIDA: 'Lida',
    FALHOU: 'Não entregue',
    CANCELADA: 'Envio cancelado',
    SEM_WHATSAPP: 'Cliente sem WhatsApp cadastrado',
};

export function envioComProblema(status: StatusEnvio | null | undefined): boolean {
    return status === 'FALHOU' || status === 'SEM_WHATSAPP' || status === 'CANCELADA';
}

export function EnvioStatus({
    status,
    erro,
    compacto = false,
    sobreFundoEscuro = false,
}: {
    status: StatusEnvio | null | undefined;
    erro?: string | null;
    compacto?: boolean;
    sobreFundoEscuro?: boolean;
}) {
    if (!status) {
        return null;
    }

    const problema = envioComProblema(status);
    const Icone = problema
        ? AlertTriangle
        : status === 'LIDA' || status === 'ENTREGUE'
            ? CheckCheck
            : status === 'ENVIADA'
                ? Check
                : Clock;

    const cor = problema
        ? (sobreFundoEscuro ? 'text-rose-200' : 'text-rose-600')
        : sobreFundoEscuro
            ? (status === 'LIDA' ? 'text-sky-200' : 'text-indigo-100')
            : status === 'LIDA'
                ? 'text-sky-600'
                : 'text-slate-500';

    return (
        <span className={`inline-flex items-center gap-1 text-xs ${cor}`} title={erro ?? rotulos[status]}>
            <Icone className="h-3.5 w-3.5" />
            {!compacto && (
                <span>
                    {rotulos[status]}
                    {problema && erro ? ` — ${erro}` : ''}
                </span>
            )}
        </span>
    );
}
