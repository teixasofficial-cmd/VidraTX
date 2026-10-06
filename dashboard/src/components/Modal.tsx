import type { ReactNode } from 'react';
import { X } from 'lucide-react';

interface ModalProps {
    title: string;
    onClose: () => void;
    children: ReactNode;
    largura?: 'sm' | 'md' | 'lg';
}

const larguraClasses = {
    sm: 'max-w-sm',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
};

export function Modal({ title, onClose, children, largura = 'md' }: ModalProps) {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div
                className="fixed inset-0 bg-slate-900/50"
                onClick={onClose}
                aria-hidden="true"
            />
            <div
                className={`relative max-h-[90vh] w-full ${larguraClasses[largura]} overflow-y-auto rounded-2xl bg-white p-6 shadow-xl`}
            >
                <div className="mb-5 flex items-center justify-between">
                    <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                        aria-label="Fechar"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>
                {children}
            </div>
        </div>
    );
}
