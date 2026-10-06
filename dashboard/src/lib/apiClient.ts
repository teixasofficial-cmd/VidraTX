const API_BASE_URL: string =
    (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080';

const TOKEN_KEY = 'vidratx.token';

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
}

export function expiracaoDoToken(token: string | null): number | null {

    try {
        const payload = token?.split('.')[1];
        if (!payload) {
            return null;
        }
        const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
        return typeof json.exp === 'number' ? json.exp * 1000 : null;
    } catch {
        return null;
    }
}

function tratarNaoAutorizado(): void {
    clearToken();
    localStorage.removeItem('vidratx.usuario');

    if (window.location.pathname !== '/entrar') {
        window.location.href = '/entrar';
    }
}

export class ApiRequestError extends Error {
    status: number;
    erros?: Record<string, string>;

    constructor(status: number, message: string, erros?: Record<string, string>) {
        super(message);
        this.name = 'ApiRequestError';
        this.status = status;
        this.erros = erros;
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

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {

    const { method = 'GET', body, auth = true } = options;

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };

    if (auth) {
        const token = getToken();
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

export async function apiRequestBlob(path: string): Promise<Blob> {

    const headers: Record<string, string> = {};

    const token = getToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const resposta = await fetch(`${API_BASE_URL}${path}`, { headers });

    if (!resposta.ok) {

        if (resposta.status === 401) {
            tratarNaoAutorizado();
        }

        throw new ApiRequestError(resposta.status, 'Não foi possível carregar a mídia');
    }

    return resposta.blob();
}
