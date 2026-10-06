import type { ReactNode } from 'react';

export const inputClass =
    'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20';

interface FormFieldProps {
    label: string;
    children: ReactNode;
    erro?: string;
}

export function FormField({ label, children, erro }: FormFieldProps) {
    return (
        <label className="block">
            <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
            {children}
            {erro && <span className="mt-1 block text-xs text-rose-600">{erro}</span>}
        </label>
    );
}
