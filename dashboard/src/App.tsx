import type { ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './lib/auth';
import { SuperAdminAuthProvider, useSuperAdminAuth } from './lib/superAdminAuth';
import { AppLayout } from './components/AppLayout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { AtendimentosPage } from './pages/AtendimentosPage';
import { AtendimentoDetalhePage } from './pages/AtendimentoDetalhePage';
import { ConfiguracoesWhatsappPage } from './pages/ConfiguracoesWhatsappPage';
import { ClientesPage } from './pages/ClientesPage';
import { ClienteDetalhePage } from './pages/ClienteDetalhePage';
import { OrcamentosPipelinePage } from './pages/OrcamentosPipelinePage';
import { OrcamentoDetalhePage } from './pages/OrcamentoDetalhePage';
import { TabelaPrecosPage } from './pages/TabelaPrecosPage';
import { ParametrosCalculoPage } from './pages/ParametrosCalculoPage';
import { ServicosPage } from './pages/ServicosPage';
import { MateriaisPage } from './pages/MateriaisPage';
import { OrdensServicoPage } from './pages/OrdensServicoPage';
import { OrdemServicoDetalhePage } from './pages/OrdemServicoDetalhePage';
import { UsuariosPage } from './pages/UsuariosPage';
import { ConfiguracoesEmpresaPage } from './pages/ConfiguracoesEmpresaPage';
import { SuperAdminLoginPage } from './pages/SuperAdminLoginPage';
import { SuperAdminDashboardPage } from './pages/SuperAdminDashboardPage';
import { NotFoundPage } from './pages/NotFoundPage';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
});

function RotaProtegida({ children }: { children: ReactNode }) {

    const { autenticado } = useAuth();

    if (!autenticado) {
        return <Navigate to="/entrar" replace />;
    }

    return <>{children}</>;
}

function RotaProtegidaSuperAdmin({ children }: { children: ReactNode }) {

    const { autenticado } = useSuperAdminAuth();

    if (!autenticado) {
        return <Navigate to="/superadmin/login" replace />;
    }

    return <>{children}</>;
}

function RotasApp() {

    const { autenticado } = useAuth();
    const { autenticado: superAdminAutenticado } = useSuperAdminAuth();

    return (
        <Routes>
            <Route
                path="/entrar"
                element={autenticado ? <Navigate to="/" replace /> : <LoginPage />}
            />

            <Route
                path="/"
                element={
                    <RotaProtegida>
                        <AppLayout />
                    </RotaProtegida>
                }
            >
                <Route index element={<DashboardPage />} />
                <Route path="atendimentos" element={<AtendimentosPage />} />
                <Route path="atendimentos/:id" element={<AtendimentoDetalhePage />} />
                <Route path="configuracoes/whatsapp" element={<ConfiguracoesWhatsappPage />} />
                <Route path="clientes" element={<ClientesPage />} />
                <Route path="clientes/:id" element={<ClienteDetalhePage />} />
                <Route path="orcamentos" element={<OrcamentosPipelinePage />} />
                <Route path="orcamentos/:id" element={<OrcamentoDetalhePage />} />
                <Route path="tabela-precos" element={<TabelaPrecosPage />} />
                <Route path="parametros-calculo" element={<ParametrosCalculoPage />} />
                <Route path="servicos" element={<ServicosPage />} />
                <Route path="materiais" element={<MateriaisPage />} />
                <Route path="ordens-servico" element={<OrdensServicoPage />} />
                <Route path="ordens-servico/:id" element={<OrdemServicoDetalhePage />} />
                <Route path="usuarios" element={<UsuariosPage />} />
                <Route path="configuracoes/empresa" element={<ConfiguracoesEmpresaPage />} />
            </Route>

            <Route
                path="/superadmin/login"
                element={
                    superAdminAutenticado
                        ? <Navigate to="/superadmin" replace />
                        : <SuperAdminLoginPage />
                }
            />
            <Route
                path="/superadmin"
                element={
                    <RotaProtegidaSuperAdmin>
                        <SuperAdminDashboardPage />
                    </RotaProtegidaSuperAdmin>
                }
            />

            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}

export function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <SuperAdminAuthProvider>
                    <BrowserRouter>
                        <RotasApp />
                    </BrowserRouter>
                </SuperAdminAuthProvider>
            </AuthProvider>
        </QueryClientProvider>
    );
}
