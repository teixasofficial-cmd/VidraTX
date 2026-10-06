import { ApiRequestError } from './apiClient';

const API_BASE_URL: string =
    (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080';

const TOKEN_KEY = 'vidratx.superadmin.token';

export function getSuperAdminToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
}

export function setSuperAdminToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
}

export function clearSuperAdminToken(): void {
    localStorage.removeItem(TOKEN_KEY);
}

function tratarNaoAutorizado(): void {
    clearSuperAdminToken();
    localStorage.removeItem('vidratx.superadmin.admin');

    if (window.location.pathname !== '/superadmin/login') {
        window.location.href = '/superadmin/login';
    }
}

interface RequestOptions {
    method?: string;
    body?: unknown;
    auth?: boolean;
}

interface ApiErrorBody {
    mensagem?: string;
    erros?: Record<string, string>;
}

export async function superAdminApiRequest<T>(
    path: string,
    options: RequestOptions = {}
): Promise<T> {

    const { method = 'GET', body, auth = true } = options;

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };

    if (auth) {
        const token = getSuperAdminToken();
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
    }

    const resposta = await fetch(`${API_BASE_URL}${path}`, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (resposta.status === 204) {
        return undefined as T;
    }

    const contentType = resposta.headers.get('content-type') ?? '';
    const dados: unknown = contentType.includes('application/json')
        ? await resposta.json()
        : undefined;

    if (!resposta.ok) {

        if (auth && resposta.status === 401) {
            tratarNaoAutorizado();
        }

        const corpoErro = dados as ApiErrorBody | undefined;

        throw new ApiRequestError(
            resposta.status,
            corpoErro?.mensagem ?? 'Não foi possível completar a operação',
            corpoErro?.erros
        );
    }

    return dados as T;
}
