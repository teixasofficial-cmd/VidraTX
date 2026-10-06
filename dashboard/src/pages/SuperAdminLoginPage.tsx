import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Loader2, ShieldCheck } from 'lucide-react';
import { useSuperAdminAuth } from '../lib/superAdminAuth';
import { ApiRequestError } from '../lib/apiClient';

export function SuperAdminLoginPage() {

    const { entrar, carregando } = useSuperAdminAuth();
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [erro, setErro] = useState<string | null>(null);

    async function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);

        try {

            await entrar({ email: email.trim(), senha });

            navigate('/superadmin', { replace: true });

        } catch (excecao) {

            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível entrar. Tente novamente.'
            );
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-950 px-4">
            <div className="w-full max-w-sm">
                <div className="mb-8 flex flex-col items-center">
                    <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-800 text-white">
                        <ShieldCheck className="h-6 w-6" />
                    </div>
                    <h1 className="text-xl font-semibold text-white">VidraTX</h1>
                    <p className="text-sm text-slate-400">Painel do super admin</p>
                </div>

                <form
                    onSubmit={aoSubmeter}
                    className="rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-sm"
                >
                    <div className="space-y-4">
                        <label className="block">
                            <span className="mb-1.5 block text-sm font-medium text-slate-300">
                                E-mail
                            </span>
                            <input
                                type="email"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                autoComplete="username"
                                className="w-full rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                            />
                        </label>
                        <label className="block">
                            <span className="mb-1.5 block text-sm font-medium text-slate-300">
                                Senha
                            </span>
                            <input
                                type="password"
                                required
                                value={senha}
                                onChange={(e) => setSenha(e.target.value)}
                                autoComplete="current-password"
                                className="w-full rounded-lg border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                            />
                        </label>
                    </div>

                    {erro && (
                        <p className="mt-4 rounded-lg bg-rose-950 px-3 py-2 text-sm text-rose-400">
                            {erro}
                        </p>
                    )}

                    <button
                        type="submit"
                        disabled={carregando}
                        className="mt-6 flex w-full items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {carregando && <Loader2 className="h-4 w-4 animate-spin" />}
                        Entrar
                    </button>
                </form>
            </div>
        </div>
    );
}
