import { ArrowRightCircle } from 'lucide-react';
import type { ResponsavelAcao } from '../types';

const estilos: Record<ResponsavelAcao, string> = {
    EMPRESA: 'border-amber-200 bg-amber-50 text-amber-900',
    CLIENTE: 'border-sky-200 bg-sky-50 text-sky-900',
    SISTEMA: 'border-slate-200 bg-slate-50 text-slate-700',
    NINGUEM: 'border-slate-200 bg-slate-50 text-slate-500',
};

const rotuloResponsavel: Record<ResponsavelAcao, string> = {
    EMPRESA: 'Sua vez',
    CLIENTE: 'Aguardando o cliente',
    SISTEMA: 'Aguardando o sistema',
    NINGUEM: 'Nada pendente',
};

export function ResponsavelBadge({ responsavel }: { responsavel: ResponsavelAcao }) {
    return (
        <span className={`rounded-full border px-2 py-0.5 text-xs font-medium ${estilos[responsavel]}`}>
            {rotuloResponsavel[responsavel]}
        </span>
    );
}

export function ProximaAcaoAviso({
    responsavel,
    texto,
}: {
    responsavel: ResponsavelAcao | null;
    texto: string | null;
}) {
    if (!responsavel || !texto) {
        return null;
    }

    return (
        <div className={`flex items-start gap-2 rounded-xl border px-3 py-2 text-sm ${estilos[responsavel]}`}>
            <ArrowRightCircle className="mt-0.5 h-4 w-4 shrink-0" />
            <p>
                <span className="font-semibold">{rotuloResponsavel[responsavel]}:</span> {texto}
            </p>
        </div>
    );
}
