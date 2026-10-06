import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ChevronRight, Plus, Search, Users } from 'lucide-react';
import { api } from '../lib/api';
import { Spinner } from '../components/Spinner';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { ClienteFormModal } from '../components/ClienteFormModal';
import { inputClass } from '../components/FormField';
import { formatarTelefone } from '../lib/formato';

export function ClientesPage() {

    const [busca, setBusca] = useState('');
    const [modalAberto, setModalAberto] = useState(false);

    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['clientes', busca],
        queryFn: () => api.clientes(busca || undefined),
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-900">Clientes</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Cadastro de clientes da empresa.
                    </p>
                </div>
                <button
                    type="button"
                    onClick={() => setModalAberto(true)}
                    className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500"
                >
                    <Plus className="h-4 w-4" />
                    Novo cliente
                </button>
            </div>

            <div className="relative max-w-sm">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                    value={busca}
                    onChange={(e) => setBusca(e.target.value)}
                    placeholder="Buscar por nome..."
                    className={`${inputClass} pl-9`}
                />
            </div>

            {isLoading ? (
                <Spinner />
            ) : isError ? (
                <ErrorState onRetry={() => refetch()} />
            ) : !data || data.length === 0 ? (
                <EmptyState
                    icon={Users}
                    title={busca ? 'Nenhum cliente encontrado' : 'Nenhum cliente cadastrado'}
                    description={
                        busca
                            ? 'Tente buscar por outro nome.'
                            : 'Cadastre o primeiro cliente para começar a criar orçamentos.'
                    }
                />
            ) : (
                <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <ul className="divide-y divide-slate-100">
                        {data.map((cliente) => (
                            <li key={cliente.id}>
                                <Link
                                    to={`/clientes/${cliente.id}`}
                                    className="flex items-center justify-between px-5 py-4 transition-colors hover:bg-slate-50"
                                >
                                    <div className="flex items-center gap-3">
                                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100 text-sm font-semibold text-indigo-700">
                                            {cliente.nome.charAt(0).toUpperCase()}
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-slate-900">
                                                {cliente.nome}
                                            </p>
                                            <p className="text-sm text-slate-500">
                                                {formatarTelefone(cliente.telefone ?? cliente.whatsapp) || cliente.email || '—'}
                                            </p>
                                        </div>
                                    </div>
                                    <ChevronRight className="h-4 w-4 text-slate-400" />
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {modalAberto && (
                <ClienteFormModal
                    onClose={() => setModalAberto(false)}
                    onSaved={() => setModalAberto(false)}
                />
            )}
        </div>
    );
}
