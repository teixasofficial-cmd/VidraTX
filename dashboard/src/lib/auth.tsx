import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useState,
    type ReactNode,
} from 'react';
import { api, type LoginPayload } from './api';
import { clearToken, expiracaoDoToken, getToken, setToken } from './apiClient';
import type { Usuario } from '../types';

interface AuthContextValue {
    usuario: Usuario | null;
    autenticado: boolean;
    carregando: boolean;
    entrar: (payload: LoginPayload) => Promise<void>;
    sair: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const USUARIO_KEY = 'vidratx.usuario';

const RENOVAR_ANTES_MS = 20 * 60 * 1000;
const VERIFICAR_A_CADA_MS = 5 * 60 * 1000;

function carregarUsuarioSalvo(): Usuario | null {

    const bruto = localStorage.getItem(USUARIO_KEY);

    if (!bruto) {
        return null;
    }

    try {
        return JSON.parse(bruto) as Usuario;
    } catch {
        return null;
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {

    const [usuario, setUsuario] = useState<Usuario | null>(() =>
        getToken() ? carregarUsuarioSalvo() : null
    );

    const [carregando, setCarregando] = useState(false);

    const entrar = useCallback(async (payload: LoginPayload) => {

        setCarregando(true);

        try {

            const resposta = await api.login(payload);

            setToken(resposta.token);

            const usuarioLogado: Usuario = {
                usuarioId: resposta.usuarioId,
                nome: resposta.nome,
                email: resposta.email,
                perfil: resposta.perfil,
                empresaId: resposta.empresaId,
                empresaSlug: resposta.empresaSlug,
                empresaNomeFantasia: resposta.empresaNomeFantasia,
            };

            localStorage.setItem(USUARIO_KEY, JSON.stringify(usuarioLogado));
            setUsuario(usuarioLogado);

        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => {

        if (!usuario) {
            return;
        }

        async function renovarSePreciso() {

            const expira = expiracaoDoToken(getToken());

            if (document.visibilityState !== 'visible' || expira === null || expira - Date.now() > RENOVAR_ANTES_MS) {
                return;
            }

            try {
                const resposta = await api.renovarSessao();
                setToken(resposta.token);
            } catch {
                return;
            }
        }

        void renovarSePreciso();
        const intervalo = window.setInterval(() => void renovarSePreciso(), VERIFICAR_A_CADA_MS);
        document.addEventListener('visibilitychange', renovarSePreciso);

        return () => {
            window.clearInterval(intervalo);
            document.removeEventListener('visibilitychange', renovarSePreciso);
        };
    }, [usuario]);

    const sair = useCallback(() => {
        clearToken();
        localStorage.removeItem(USUARIO_KEY);
        setUsuario(null);
    }, []);

    return (
        <AuthContext.Provider
            value={{ usuario, autenticado: usuario !== null, carregando, entrar, sair }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {

    const contexto = useContext(AuthContext);

    if (!contexto) {
        throw new Error('useAuth precisa ser usado dentro de um AuthProvider');
    }

    return contexto;
}

export function usePodeAlterarPrecos(): boolean {
    const { usuario } = useAuth();
    return usuario?.perfil === 'ADMIN' || usuario?.perfil === 'GERENTE';
}
