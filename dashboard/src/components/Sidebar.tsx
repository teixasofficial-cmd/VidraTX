import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
    Boxes,
    Building2,
    ClipboardList,
    FileText,
    LayoutDashboard,
    MessageCircle,
    Settings2,
    Smartphone,
    Tags,
    UserCog,
    Users,
    BellRing,
    Wrench,
} from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuth } from '../lib/auth';
import { pedirNotificacao, podePedirNotificacao, useAvisoClientesEsperando } from '../lib/avisos';

interface SidebarProps {
    open: boolean;
    onClose: () => void;
}

const itemBase =
    'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors';

const linkAtivo = `${itemBase} bg-indigo-600 text-white`;
const linkInativo = `${itemBase} text-slate-300 hover:bg-slate-800 hover:text-white`;

export function Sidebar({ open, onClose }: SidebarProps) {

    const { usuario } = useAuth();

    const { data } = useQuery({
        queryKey: ['atendimentos-pendentes-contagem'],
        queryFn: api.atendimentosPendentesContagem,
        refetchInterval: 15000,
    });

    const pendentes = data?.quantidade ?? 0;
    const [pedirAviso, setPedirAviso] = useState(podePedirNotificacao);

    useAvisoClientesEsperando(data?.quantidade);
    const podeVerOrdensServico = !!usuario;
    const podeVerUsuarios = usuario?.perfil === 'ADMIN';

    return (
        <>
            {open && (
                <div
                    className="fixed inset-0 z-30 bg-slate-900/50 lg:hidden"
                    onClick={onClose}
                    aria-hidden="true"
                />
            )}

            <aside
                className={`fixed inset-y-0 left-0 z-40 flex w-64 transform flex-col bg-slate-900 px-4 py-6 transition-transform duration-200 ease-out lg:translate-x-0 ${
                    open ? 'translate-x-0' : '-translate-x-full'
                }`}
            >
                <div className="mb-8 flex items-center gap-2.5 px-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-500 text-sm font-bold text-white">
                        V
                    </div>
                    <span className="text-lg font-semibold text-white">VidraTX</span>
                </div>

                <nav className="flex flex-1 flex-col gap-1">
                    <NavLink
                        to="/"
                        end
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <LayoutDashboard className="h-5 w-5" />
                        Início
                    </NavLink>

                    <NavLink
                        to="/clientes"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Users className="h-5 w-5" />
                        Clientes
                    </NavLink>

                    <NavLink
                        to="/orcamentos"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <FileText className="h-5 w-5" />
                        Orçamentos
                    </NavLink>

                    <NavLink
                        to="/tabela-precos"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Tags className="h-5 w-5" />
                        Tabela de Preços
                    </NavLink>

                    <NavLink
                        to="/parametros-calculo"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Settings2 className="h-5 w-5" />
                        Parâmetros de Cálculo
                    </NavLink>

                    <NavLink
                        to="/atendimentos"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <MessageCircle className="h-5 w-5" />
                        <span className="flex-1">Atendimentos</span>
                        {pendentes > 0 && (
                            <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-emerald-500 px-1.5 text-xs font-semibold text-white">
                                {pendentes}
                            </span>
                        )}
                    </NavLink>

                    <NavLink
                        to="/servicos"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Wrench className="h-5 w-5" />
                        Serviços
                    </NavLink>

                    <NavLink
                        to="/materiais"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Boxes className="h-5 w-5" />
                        Materiais
                    </NavLink>

                    {podeVerOrdensServico && (
                        <NavLink
                            to="/ordens-servico"
                            onClick={onClose}
                            className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                        >
                            <ClipboardList className="h-5 w-5" />
                            Ordens de serviço
                        </NavLink>
                    )}

                    <div className="mt-6 px-3 text-xs font-semibold uppercase tracking-wider text-slate-500">
                        Configurações
                    </div>

                    <NavLink
                        to="/configuracoes/whatsapp"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Smartphone className="h-5 w-5" />
                        WhatsApp
                    </NavLink>

                    <NavLink
                        to="/configuracoes/empresa"
                        onClick={onClose}
                        className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                    >
                        <Building2 className="h-5 w-5" />
                        Empresa
                    </NavLink>

                    {podeVerUsuarios && (
                        <NavLink
                            to="/usuarios"
                            onClick={onClose}
                            className={({ isActive }) => (isActive ? linkAtivo : linkInativo)}
                        >
                            <UserCog className="h-5 w-5" />
                            Usuários
                        </NavLink>
                    )}

                    {pedirAviso && (
                        <button
                            type="button"
                            onClick={() => void pedirNotificacao().then(() => setPedirAviso(podePedirNotificacao()))}
                            className="mt-6 flex items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm font-medium text-slate-300 hover:bg-slate-800 hover:text-white"
                        >
                            <BellRing className="h-5 w-5 shrink-0" />
                            Avisar quando um cliente chamar
                        </button>
                    )}
                </nav>
            </aside>
        </>
    );
}
