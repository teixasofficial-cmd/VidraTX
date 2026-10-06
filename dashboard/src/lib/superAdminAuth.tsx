import {
    createContext,
    useCallback,
    useContext,
    useState,
    type ReactNode,
} from 'react';
import { superAdminApi, type SuperAdminLoginPayload } from './superAdminApi';
import {
    clearSuperAdminToken,
    getSuperAdminToken,
    setSuperAdminToken,
} from './superAdminApiClient';

interface SuperAdminSessao {
    administradorId: number;
    email: string;
}

interface SuperAdminAuthContextValue {
    admin: SuperAdminSessao | null;
    autenticado: boolean;
    carregando: boolean;
    entrar: (payload: SuperAdminLoginPayload) => Promise<void>;
    sair: () => void;
}

const SuperAdminAuthContext = createContext<SuperAdminAuthContextValue | undefined>(undefined);

const ADMIN_KEY = 'vidratx.superadmin.admin';

function carregarAdminSalvo(): SuperAdminSessao | null {

    const bruto = localStorage.getItem(ADMIN_KEY);

    if (!bruto) {
        return null;
    }

    try {
        return JSON.parse(bruto) as SuperAdminSessao;
    } catch {
        return null;
    }
}

export function SuperAdminAuthProvider({ children }: { children: ReactNode }) {

    const [admin, setAdmin] = useState<SuperAdminSessao | null>(() =>
        getSuperAdminToken() ? carregarAdminSalvo() : null
    );

    const [carregando, setCarregando] = useState(false);

    const entrar = useCallback(async (payload: SuperAdminLoginPayload) => {

        setCarregando(true);

        try {

            const resposta = await superAdminApi.login(payload);

            setSuperAdminToken(resposta.token);

            const sessao: SuperAdminSessao = {
                administradorId: resposta.administradorId,
                email: resposta.email,
            };

            localStorage.setItem(ADMIN_KEY, JSON.stringify(sessao));
            setAdmin(sessao);

        } finally {
            setCarregando(false);
        }
    }, []);

    const sair = useCallback(() => {
        clearSuperAdminToken();
        localStorage.removeItem(ADMIN_KEY);
        setAdmin(null);
    }, []);

    return (
        <SuperAdminAuthContext.Provider
            value={{ admin, autenticado: admin !== null, carregando, entrar, sair }}
        >
            {children}
        </SuperAdminAuthContext.Provider>
    );
}

export function useSuperAdminAuth(): SuperAdminAuthContextValue {

    const contexto = useContext(SuperAdminAuthContext);

    if (!contexto) {
        throw new Error('useSuperAdminAuth precisa ser usado dentro de um SuperAdminAuthProvider');
    }

    return contexto;
}
