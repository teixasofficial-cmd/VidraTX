import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useAuth } from '../lib/auth';
import { ApiRequestError } from '../lib/apiClient';

interface CampoProps {
    label: string;
    value: string;
    onChange: (valor: string) => void;
    type?: string;
    placeholder?: string;
    autoComplete?: string;
}

function Campo({ label, value, onChange, type = 'text', placeholder, autoComplete }: CampoProps) {
    return (
        <label className="block">
            <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
            <input
                type={type}
                required
                value={value}
                onChange={(evento) => onChange(evento.target.value)}
                placeholder={placeholder}
                autoComplete={autoComplete}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
        </label>
    );
}

export function LoginPage() {

    const { entrar, carregando } = useAuth();
    const navigate = useNavigate();

    const [empresaSlug, setEmpresaSlug] = useState('');
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [erro, setErro] = useState<string | null>(null);

    async function aoSubmeter(evento: FormEvent) {

        evento.preventDefault();
        setErro(null);

        try {

            await entrar({
                empresaSlug: empresaSlug.trim(),
                email: email.trim(),
                senha,
            });

            navigate('/', { replace: true });

        } catch (excecao) {

            setErro(
                excecao instanceof ApiRequestError
                    ? excecao.message
                    : 'Não foi possível entrar. Tente novamente.'
            );
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
            <div className="w-full max-w-sm">
                <div className="mb-8 flex flex-col items-center">
                    <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-600 text-xl font-bold text-white">
                        V
                    </div>
                    <h1 className="text-xl font-semibold text-slate-900">VidraTX</h1>
                    <p className="text-sm text-slate-500">Painel administrativo</p>
                </div>

                <form
                    onSubmit={aoSubmeter}
                    className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
                >
                    <div className="space-y-4">
                        <Campo
                            label="Empresa"
                            value={empresaSlug}
                            onChange={setEmpresaSlug}
                            placeholder="minha-vidracaria"
                            autoComplete="organization"
                        />
                        <Campo
                            label="E-mail"
                            type="email"
                            value={email}
                            onChange={setEmail}
                            placeholder="voce@empresa.com"
                            autoComplete="username"
                        />
                        <Campo
                            label="Senha"
                            type="password"
                            value={senha}
                            onChange={setSenha}
                            placeholder="••••••••"
                            autoComplete="current-password"
                        />
                    </div>

                    {erro && (
                        <p className="mt-4 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
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

                    <details className="mt-4 text-sm text-slate-500">
                        <summary className="cursor-pointer text-center text-indigo-600 hover:text-indigo-500">
                            Esqueci minha senha
                        </summary>
                        <p className="mt-2">
                            Peça ao administrador da sua vidraçaria: em <strong>Usuários</strong>, ele define uma
                            senha nova para você. Se você é o administrador, fale com o suporte do VidraTX.
                        </p>
                    </details>
                </form>
            </div>
        </div>
    );
}
