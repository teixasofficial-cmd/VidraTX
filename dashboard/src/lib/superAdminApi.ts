import { superAdminApiRequest } from './superAdminApiClient';
import type {
    EmpresaSuperAdmin,
    EmpresaSuperAdminPayload,
    SuperAdminLoginResponse,
    SuperAdminResumo,
} from '../types';

export interface SuperAdminLoginPayload {
    email: string;
    senha: string;
}

export const superAdminApi = {
    login: (payload: SuperAdminLoginPayload) =>
        superAdminApiRequest<SuperAdminLoginResponse>('/auth/superadmin/login', {
            method: 'POST',
            body: payload,
            auth: false,
        }),

    resumo: () => superAdminApiRequest<SuperAdminResumo>('/api/superadmin/empresas/resumo'),

    empresas: () => superAdminApiRequest<EmpresaSuperAdmin[]>('/api/superadmin/empresas'),

    criarEmpresa: (payload: EmpresaSuperAdminPayload) =>
        superAdminApiRequest<EmpresaSuperAdmin>('/api/superadmin/empresas', {
            method: 'POST',
            body: payload,
        }),

    atualizarEmpresa: (id: number, payload: Omit<EmpresaSuperAdminPayload, 'senha'>) =>
        superAdminApiRequest<EmpresaSuperAdmin>(`/api/superadmin/empresas/${id}`, {
            method: 'PUT',
            body: payload,
        }),

    alternarStatusEmpresa: (id: number) =>
        superAdminApiRequest<EmpresaSuperAdmin>(`/api/superadmin/empresas/${id}/alternar-status`, {
            method: 'POST',
        }),

    redefinirSenhaEmpresa: (id: number, senha: string) =>
        superAdminApiRequest<void>(`/api/superadmin/empresas/${id}/redefinir-senha`, {
            method: 'POST',
            body: { senha },
        }),

    trocarMinhaSenha: (senhaAtual: string, novaSenha: string) =>
        superAdminApiRequest<void>('/api/superadmin/me/senha', {
            method: 'PUT',
            body: { senhaAtual, novaSenha },
        }),
};
