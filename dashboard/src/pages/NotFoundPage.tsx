import { Link } from 'react-router-dom';

export function NotFoundPage() {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-slate-50 px-4 text-center">
            <p className="text-5xl font-bold text-slate-300">404</p>
            <p className="mt-2 text-sm text-slate-500">Página não encontrada.</p>
            <Link
                to="/"
                className="mt-6 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
            >
                Voltar ao painel
            </Link>
        </div>
    );
}
