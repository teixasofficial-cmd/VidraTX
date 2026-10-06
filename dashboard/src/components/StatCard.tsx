import type { LucideIcon } from 'lucide-react';

interface StatCardProps {
    label: string;
    value: string;
    icon: LucideIcon;
    accent?: 'default' | 'success' | 'danger' | 'warning';
    hint?: string;
}

const accentClasses: Record<NonNullable<StatCardProps['accent']>, string> = {
    default: 'bg-indigo-50 text-indigo-600',
    success: 'bg-emerald-50 text-emerald-600',
    danger: 'bg-rose-50 text-rose-600',
    warning: 'bg-amber-50 text-amber-600',
};

export function StatCard({ label, value, icon: Icon, accent = 'default', hint }: StatCardProps) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-slate-500">{label}</p>
                <div
                    className={`flex h-9 w-9 items-center justify-center rounded-xl ${accentClasses[accent]}`}
                >
                    <Icon className="h-5 w-5" />
                </div>
            </div>
            <p className="mt-3 text-2xl font-semibold tracking-tight text-slate-900">{value}</p>
            {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
        </div>
    );
}
