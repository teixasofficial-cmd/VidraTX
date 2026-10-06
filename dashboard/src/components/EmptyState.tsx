import type { LucideIcon } from 'lucide-react';

interface EmptyStateProps {
    icon: LucideIcon;
    title: string;
    description: string;
    action?: { label: string; onClick: () => void };
}

export function EmptyState({ icon: Icon, title, description, action }: EmptyStateProps) {
    return (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-white px-6 py-16 text-center">
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-400">
                <Icon className="h-6 w-6" />
            </div>
            <p className="text-sm font-semibold text-slate-900">{title}</p>
            <p className="mt-1 max-w-xs text-sm text-slate-500">{description}</p>
            {action && (
                <button
                    type="button"
                    onClick={action.onClick}
                    className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                >
                    {action.label}
                </button>
            )}
        </div>
    );
}
