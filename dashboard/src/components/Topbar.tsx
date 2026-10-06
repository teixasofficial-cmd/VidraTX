import { useState } from 'react';
import { KeyRound, LogOut, Menu } from 'lucide-react';
import { useAuth } from '../lib/auth';
import { TrocarMinhaSenhaModal } from './TrocarMinhaSenhaModal';

interface TopbarProps {
    onMenuClick: () => void;
}

export function Topbar({ onMenuClick }: TopbarProps) {

    const { usuario, sair } = useAuth();
    const [trocandoSenha, setTrocandoSenha] = useState(false);

    return (
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 lg:px-8">
            <button
                type="button"
                onClick={onMenuClick}
                className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 lg:hidden"
                aria-label="Abrir menu"
            >
                <Menu className="h-5 w-5" />
            </button>

            <div className="hidden text-sm font-medium text-slate-500 lg:block">
                {usuario?.empresaNomeFantasia}
            </div>

            <div className="flex items-center gap-3">
                <div className="flex items-center gap-2 rounded-full bg-slate-100 py-1 pl-1 pr-3">
                    <div className="flex h-7 w-7 items-center justify-center rounded-full bg-indigo-600 text-xs font-semibold text-white">
                        {usuario?.nome.charAt(0).toUpperCase()}
                    </div>
                    <div className="hidden text-left sm:block">
                        <p className="text-sm font-medium leading-none text-slate-900">
                            {usuario?.nome}
                        </p>
                        <p className="mt-0.5 text-xs capitalize leading-none text-slate-500">
                            {usuario?.perfil.toLowerCase()}
                        </p>
                    </div>
                </div>

                <button
                    type="button"
                    onClick={() => setTrocandoSenha(true)}
                    className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                    aria-label="Trocar minha senha"
                    title="Trocar minha senha"
                >
                    <KeyRound className="h-5 w-5" />
                </button>

                <button
                    type="button"
                    onClick={sair}
                    className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                    aria-label="Sair"
                    title="Sair"
                >
                    <LogOut className="h-5 w-5" />
                </button>
            </div>

            {trocandoSenha && (
                <TrocarMinhaSenhaModal
                    onClose={() => setTrocandoSenha(false)}
                    onSaved={() => setTrocandoSenha(false)}
                />
            )}
        </header>
    );
}
