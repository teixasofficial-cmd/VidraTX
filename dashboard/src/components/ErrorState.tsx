import { AlertTriangle } from 'lucide-react';

interface ErrorStateProps {
    title?: string;
    description?: string;
    onRetry?: () => void;
}

export function ErrorState({
    title = 'Não foi possível carregar os dados',
    description = 'Verifique sua conexão e tente novamente.',
    onRetry,
}: ErrorStateProps) {
    return (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-red-200 bg-red-50 px-6 py-16 text-center">
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-red-100 text-red-500">
                <AlertTriangle className="h-6 w-6" />
            </div>
            <p className="text-sm font-semibold text-slate-900">{title}</p>
            <p className="mt-1 max-w-xs text-sm text-slate-500">{description}</p>
            {onRetry && (
                <button
                    type="button"
                    onClick={onRetry}
                    className="mt-4 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700"
                >
                    Tentar novamente
                </button>
            )}
        </div>
    );
}
