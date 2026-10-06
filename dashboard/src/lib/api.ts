import { apiRequest, apiRequestBlob } from './apiClient';
import type {
    AgendaEmpresa,
    AgendaItem,
    AtendenteResumo,
    AtendimentoWhatsapp,
    AtendimentoWhatsappDetalhe,
    AtendimentosPendentesContagem,
    CancelarAgendamentoPayload,
    Cliente,
    ClientePayload,
    DashboardResumo,
    EncerrarAtendimentoPayload,
    EnviarOrcamentoPayload,
    HistoricoEvento,
    Instalacao,
    InstalacaoPayload,
    InstalacaoReagendarPayload,
    InstalacaoRealizarPayload,
    LoginResponse,
    Material,
    MaterialPayload,
    Medicao,
    MedicaoPayload,
    MedicaoReagendarPayload,
    MedicaoRealizarPayload,
    MensagemAtendimento,
    MinhaEmpresa,
    MinhaEmpresaPayload,
    MoverPipelinePayload,
    Orcamento,
    OrcamentoItem,
    OrcamentoItemPayload,
    OrcamentoPayload,
    OrcamentoTotais,
    OrdemServico,
    OrdemServicoPayload,
    OrdemServicoUpdatePayload,
    ParametroCalculo,
    ParametroCalculoPayload,
    PerderOrcamentoPayload,
    PerguntaFrequente,
    PerguntaFrequentePayload,
    ReabrirOrcamentoPayload,
    PosVenda,
    PosVendaAtendimentoPayload,
    PosVendaPayload,
    PosVendaResolverPayload,
    PropostaAgendamento,
    ProximaAcaoItem,
    RecusarContrapropostaPayload,
    Servico,
    ServicoPayload,
    StatusAtendimento,
    StatusOrcamento,
    StatusProducao,
    TabelaPreco,
    TabelaPrecoPayload,
    Tipologia,
    TipologiaAjustePayload,
    UsuarioEmpresa,
    UsuarioEmpresaPayload,
    UsuarioEmpresaUpdatePayload,
    WhatsappInstancia,
} from '../types';

export interface LoginPayload {
    empresaSlug: string;
    email: string;
    senha: string;
}

export const api = {
    login: (payload: LoginPayload) =>
        apiRequest<LoginResponse>('/auth/login', {
            method: 'POST',
            body: payload,
            auth: false,
        }),

    renovarSessao: () => apiRequest<LoginResponse>('/auth/renovar', { method: 'POST' }),

    dashboardResumo: () => apiRequest<DashboardResumo>('/api/dashboard/resumo'),

    proximasAcoes: () => apiRequest<ProximaAcaoItem[]>('/api/dashboard/proximas-acoes'),

    agenda: (de: string, ate: string) =>
        apiRequest<AgendaItem[]>(`/api/dashboard/agenda?de=${de}&ate=${ate}`),

    atendimentosPendentes: () =>
        apiRequest<AtendimentoWhatsapp[]>('/api/atendimentos-whatsapp/pendentes'),

    atendimentos: (status?: StatusAtendimento) =>
        apiRequest<AtendimentoWhatsapp[]>(
            `/api/atendimentos-whatsapp${status ? `?status=${status}` : ''}`
        ),

    atendimentosPendentesContagem: () =>
        apiRequest<AtendimentosPendentesContagem>(
            '/api/atendimentos-whatsapp/pendentes/contagem'
        ),

    atendimentoDetalhe: (id: number) =>
        apiRequest<AtendimentoWhatsappDetalhe>(`/api/atendimentos-whatsapp/${id}`),

    assumirAtendimento: (id: number) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/assumir`, {
            method: 'POST',
        }),

    responderAtendimento: (id: number, mensagem: string) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/responder`, {
            method: 'POST',
            body: { mensagem },
        }),

    encerrarAtendimento: (id: number, payload: EncerrarAtendimentoPayload = {}) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/encerrar`, {
            method: 'POST',
            body: payload,
        }),

    atendentes: () => apiRequest<AtendenteResumo[]>('/api/atendimentos-whatsapp/atendentes'),

    transferirAtendimento: (id: number, usuarioId: number) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/transferir`, {
            method: 'POST',
            body: { usuarioId },
        }),

    reabrirAtendimento: (id: number) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/reabrir`, {
            method: 'POST',
        }),

    devolverAtendimentoAoBot: (id: number) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/devolver-ao-bot`, {
            method: 'POST',
        }),

    vincularClienteAtendimento: (id: number, clienteId: number) =>
        apiRequest<AtendimentoWhatsapp>(`/api/atendimentos-whatsapp/${id}/vincular-cliente`, {
            method: 'POST',
            body: { clienteId },
        }),

    mensagemMidia: (mensagemId: number) =>
        apiRequestBlob(`/api/atendimentos-whatsapp/midia/${mensagemId}`),

    whatsappInstanciaStatus: () =>
        apiRequest<WhatsappInstancia>('/api/whatsapp/instancia'),

    whatsappInstanciaQr: () =>
        apiRequest<WhatsappInstancia>('/api/whatsapp/instancia/qr'),

    whatsappInstanciaConectar: () =>
        apiRequest<WhatsappInstancia>('/api/whatsapp/instancia/conectar', {
            method: 'POST',
        }),

    clientes: (nome?: string) =>
        apiRequest<Cliente[]>(`/api/clientes${nome ? `?nome=${encodeURIComponent(nome)}` : ''}`),

    cliente: (id: number) => apiRequest<Cliente>(`/api/clientes/${id}`),

    criarCliente: (payload: ClientePayload) =>
        apiRequest<Cliente>('/api/clientes', { method: 'POST', body: payload }),

    atualizarCliente: (id: number, payload: ClientePayload) =>
        apiRequest<Cliente>(`/api/clientes/${id}`, { method: 'PUT', body: payload }),

    excluirCliente: (id: number) =>
        apiRequest<void>(`/api/clientes/${id}`, { method: 'DELETE' }),

    anonimizarCliente: (id: number) =>
        apiRequest<void>(`/api/clientes/${id}/anonimizar`, { method: 'POST' }),

    historicoCliente: (clienteId: number) =>
        apiRequest<HistoricoEvento[]>(`/api/clientes/${clienteId}/historico`),

    orcamentos: (filtros?: { status?: StatusOrcamento; clienteId?: number }) => {
        const params = new URLSearchParams();
        if (filtros?.status) params.set('status', filtros.status);
        if (filtros?.clienteId) params.set('clienteId', String(filtros.clienteId));
        const query = params.toString();
        return apiRequest<Orcamento[]>(`/api/orcamentos${query ? `?${query}` : ''}`);
    },

    orcamento: (id: number) => apiRequest<Orcamento>(`/api/orcamentos/${id}`),

    criarOrcamento: (payload: OrcamentoPayload) =>
        apiRequest<Orcamento>('/api/orcamentos', { method: 'POST', body: payload }),

    atualizarOrcamento: (id: number, payload: OrcamentoPayload) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}`, { method: 'PUT', body: payload }),

    excluirOrcamento: (id: number) =>
        apiRequest<void>(`/api/orcamentos/${id}`, { method: 'DELETE' }),

    criarOrcamentoDeSolicitacao: (solicitacaoId: number) =>
        apiRequest<Orcamento>(`/api/solicitacoes-orcamento/${solicitacaoId}/criar-orcamento`, {
            method: 'POST',
        }),

    orcamentoFotosWhatsapp: (orcamentoId: number) =>
        apiRequest<MensagemAtendimento[]>(`/api/orcamentos/${orcamentoId}/fotos-whatsapp`),

    enviarOrcamento: (id: number, payload: EnviarOrcamentoPayload = {}) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/enviar`, { method: 'POST', body: payload }),

    enviarEstimativaOrcamento: (id: number) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/enviar-estimativa`, { method: 'POST' }),

    revisarOrcamento: (id: number) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/revisar`, { method: 'POST' }),

    aprovarOrcamento: (id: number) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/aprovar`, { method: 'POST' }),

    perderOrcamento: (id: number, payload: PerderOrcamentoPayload) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/perder`, { method: 'POST', body: payload }),

    reabrirOrcamento: (id: number, payload: ReabrirOrcamentoPayload = {}) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/reabrir`, { method: 'POST', body: payload }),

    expirarOrcamento: (id: number) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/expirar`, { method: 'POST' }),

    moverPipelineOrcamento: (id: number, payload: MoverPipelinePayload) =>
        apiRequest<Orcamento>(`/api/orcamentos/${id}/mover-pipeline`, { method: 'POST', body: payload }),

    orcamentoItens: (orcamentoId: number) =>
        apiRequest<OrcamentoItem[]>(`/api/orcamentos/${orcamentoId}/itens`),

    criarOrcamentoItem: (orcamentoId: number, payload: OrcamentoItemPayload) =>
        apiRequest<OrcamentoItem>(`/api/orcamentos/${orcamentoId}/itens`, { method: 'POST', body: payload }),

    atualizarOrcamentoItem: (orcamentoId: number, itemId: number, payload: OrcamentoItemPayload) =>
        apiRequest<OrcamentoItem>(`/api/orcamentos/${orcamentoId}/itens/${itemId}`, {
            method: 'PUT',
            body: payload,
        }),

    excluirOrcamentoItem: (orcamentoId: number, itemId: number) =>
        apiRequest<void>(`/api/orcamentos/${orcamentoId}/itens/${itemId}`, { method: 'DELETE' }),

    ajustarLinhaOrcamento: (orcamentoId: number, itemId: number, linhaId: number, valorFinal: number | null) =>
        apiRequest<OrcamentoTotais>(`/api/orcamentos/${orcamentoId}/itens/${itemId}/linhas/${linhaId}`, {
            method: 'PUT',
            body: { valorFinal },
        }),

    aceitarTodasSugestoes: (orcamentoId: number) =>
        apiRequest<OrcamentoTotais>(`/api/orcamentos/${orcamentoId}/aceitar-sugestoes`, { method: 'POST' }),

    ajustarPrecoFinalOrcamento: (orcamentoId: number, valorFinal: number | null) =>
        apiRequest<OrcamentoTotais>(`/api/orcamentos/${orcamentoId}/preco-final`, {
            method: 'PUT',
            body: { valorFinal },
        }),

    definirParcelasOrcamento: (orcamentoId: number, parcelas: number | null) =>
        apiRequest<OrcamentoTotais>(`/api/orcamentos/${orcamentoId}/parcelas`, {
            method: 'PUT',
            body: { parcelas },
        }),

    totaisOrcamento: (orcamentoId: number) =>
        apiRequest<OrcamentoTotais>(`/api/orcamentos/${orcamentoId}/totais`),

    parametrosCalculo: () => apiRequest<ParametroCalculo>('/api/parametros-calculo'),

    atualizarParametrosCalculo: (payload: ParametroCalculoPayload) =>
        apiRequest<ParametroCalculo>('/api/parametros-calculo', { method: 'PUT', body: payload }),

    tabelaPrecos: (apenasAtivos?: boolean) =>
        apiRequest<TabelaPreco[]>(`/api/tabela-precos?apenasAtivos=${apenasAtivos ?? false}`),

    criarTabelaPreco: (payload: TabelaPrecoPayload) =>
        apiRequest<TabelaPreco>('/api/tabela-precos', { method: 'POST', body: payload }),

    atualizarTabelaPreco: (id: number, payload: TabelaPrecoPayload) =>
        apiRequest<TabelaPreco>(`/api/tabela-precos/${id}`, { method: 'PUT', body: payload }),

    excluirTabelaPreco: (id: number) =>
        apiRequest<void>(`/api/tabela-precos/${id}`, { method: 'DELETE' }),

    tipologias: (apenasAtivas?: boolean) =>
        apiRequest<Tipologia[]>(`/api/tipologias?apenasAtivas=${apenasAtivas ?? false}`),

    ajustarFolgasTipologia: (id: number, payload: TipologiaAjustePayload) =>
        apiRequest<Tipologia>(`/api/tipologias/${id}/folgas`, { method: 'PUT', body: payload }),

    trocarMinhaSenha: (senhaAtual: string, novaSenha: string) =>
        apiRequest<void>('/api/usuarios/me/senha', {
            method: 'PUT',
            body: { senhaAtual, novaSenha },
        }),

    servicos: (apenasAtivos?: boolean) =>
        apiRequest<Servico[]>(`/api/servicos?apenasAtivos=${apenasAtivos ?? false}`),

    servico: (id: number) => apiRequest<Servico>(`/api/servicos/${id}`),

    criarServico: (payload: ServicoPayload) =>
        apiRequest<Servico>('/api/servicos', { method: 'POST', body: payload }),

    atualizarServico: (id: number, payload: ServicoPayload) =>
        apiRequest<Servico>(`/api/servicos/${id}`, { method: 'PUT', body: payload }),

    excluirServico: (id: number) =>
        apiRequest<void>(`/api/servicos/${id}`, { method: 'DELETE' }),

    materiais: (apenasAtivos?: boolean) =>
        apiRequest<Material[]>(`/api/materiais?apenasAtivos=${apenasAtivos ?? false}`),

    material: (id: number) => apiRequest<Material>(`/api/materiais/${id}`),

    criarMaterial: (payload: MaterialPayload) =>
        apiRequest<Material>('/api/materiais', { method: 'POST', body: payload }),

    atualizarMaterial: (id: number, payload: MaterialPayload) =>
        apiRequest<Material>(`/api/materiais/${id}`, { method: 'PUT', body: payload }),

    excluirMaterial: (id: number) =>
        apiRequest<void>(`/api/materiais/${id}`, { method: 'DELETE' }),

    ordensServico: (statusProducao?: StatusProducao) =>
        apiRequest<OrdemServico[]>(
            `/api/ordens-servico${statusProducao ? `?statusProducao=${statusProducao}` : ''}`
        ),

    ordemServico: (id: number) => apiRequest<OrdemServico>(`/api/ordens-servico/${id}`),

    criarOrdemServico: (payload: OrdemServicoPayload) =>
        apiRequest<OrdemServico>('/api/ordens-servico', { method: 'POST', body: payload }),

    atualizarOrdemServico: (id: number, payload: OrdemServicoUpdatePayload) =>
        apiRequest<OrdemServico>(`/api/ordens-servico/${id}`, { method: 'PUT', body: payload }),

    iniciarProducaoOrdemServico: (id: number) =>
        apiRequest<OrdemServico>(`/api/ordens-servico/${id}/iniciar-producao`, { method: 'POST' }),

    concluirProducaoOrdemServico: (id: number) =>
        apiRequest<OrdemServico>(`/api/ordens-servico/${id}/concluir-producao`, { method: 'POST' }),

    conferirProducaoOrdemServico: (id: number) =>
        apiRequest<OrdemServico>(`/api/ordens-servico/${id}/conferir-producao`, { method: 'POST' }),

    alterarNecessitaProducaoOrdemServico: (id: number, valor: boolean) =>
        apiRequest<OrdemServico>(`/api/ordens-servico/${id}/necessita-producao?valor=${valor}`, {
            method: 'PUT',
        }),

    instalacao: (ordemServicoId: number) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao`),

    agendarInstalacao: (ordemServicoId: number, payload: InstalacaoPayload) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao`, {
            method: 'POST',
            body: payload,
        }),

    reagendarInstalacao: (ordemServicoId: number, payload: InstalacaoReagendarPayload) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/reagendar`, {
            method: 'PUT',
            body: payload,
        }),

    realizarInstalacao: (ordemServicoId: number, payload: InstalacaoRealizarPayload) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/realizar`, {
            method: 'POST',
            body: payload,
        }),

    cancelarInstalacao: (ordemServicoId: number, payload: CancelarAgendamentoPayload = {}) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/cancelar`, {
            method: 'POST',
            body: payload,
        }),

    manterDataInstalacao: (ordemServicoId: number) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/manter-data`, {
            method: 'POST',
        }),

    recusarContrapropostaInstalacao: (ordemServicoId: number, payload: RecusarContrapropostaPayload) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/recusar-contraproposta`, {
            method: 'PUT',
            body: payload,
        }),

    confirmarInstalacaoManualmente: (ordemServicoId: number) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/confirmar-manualmente`, {
            method: 'POST',
        }),

    propostasInstalacao: (ordemServicoId: number) =>
        apiRequest<PropostaAgendamento[]>(`/api/ordens-servico/${ordemServicoId}/instalacao/propostas`),

    aceitarContrapropostaInstalacao: (ordemServicoId: number, payload: InstalacaoReagendarPayload) =>
        apiRequest<Instalacao>(`/api/ordens-servico/${ordemServicoId}/instalacao/aceitar-contraproposta`, {
            method: 'PUT',
            body: payload,
        }),

    medicao: (orcamentoId: number) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao`),

    agendarMedicao: (orcamentoId: number, payload: MedicaoPayload) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao`, {
            method: 'POST',
            body: payload,
        }),

    reagendarMedicao: (orcamentoId: number, payload: MedicaoReagendarPayload) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/reagendar`, {
            method: 'PUT',
            body: payload,
        }),

    realizarMedicao: (orcamentoId: number, payload: MedicaoRealizarPayload) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/realizar`, {
            method: 'POST',
            body: payload,
        }),

    cancelarMedicao: (orcamentoId: number, payload: CancelarAgendamentoPayload = {}) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/cancelar`, {
            method: 'POST',
            body: payload,
        }),

    manterDataMedicao: (orcamentoId: number) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/manter-data`, {
            method: 'POST',
        }),

    recusarContrapropostaMedicao: (orcamentoId: number, payload: RecusarContrapropostaPayload) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/recusar-contraproposta`, {
            method: 'PUT',
            body: payload,
        }),

    confirmarMedicaoManualmente: (orcamentoId: number) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/confirmar-manualmente`, {
            method: 'POST',
        }),

    propostasMedicao: (orcamentoId: number) =>
        apiRequest<PropostaAgendamento[]>(`/api/orcamentos/${orcamentoId}/medicao/propostas`),

    aceitarContrapropostaMedicao: (orcamentoId: number, payload: MedicaoReagendarPayload) =>
        apiRequest<Medicao>(`/api/orcamentos/${orcamentoId}/medicao/aceitar-contraproposta`, {
            method: 'PUT',
            body: payload,
        }),

    posVendas: (ordemServicoId: number) =>
        apiRequest<PosVenda[]>(`/api/ordens-servico/${ordemServicoId}/pos-venda`),

    abrirPosVenda: (ordemServicoId: number, payload: PosVendaPayload) =>
        apiRequest<PosVenda>(`/api/ordens-servico/${ordemServicoId}/pos-venda`, {
            method: 'POST',
            body: payload,
        }),

    iniciarAtendimentoPosVenda: (
        ordemServicoId: number,
        posVendaId: number,
        payload: PosVendaAtendimentoPayload
    ) =>
        apiRequest<PosVenda>(
            `/api/ordens-servico/${ordemServicoId}/pos-venda/${posVendaId}/iniciar-atendimento`,
            { method: 'POST', body: payload }
        ),

    resolverPosVenda: (ordemServicoId: number, posVendaId: number, payload: PosVendaResolverPayload) =>
        apiRequest<PosVenda>(`/api/ordens-servico/${ordemServicoId}/pos-venda/${posVendaId}/resolver`, {
            method: 'POST',
            body: payload,
        }),

    encerrarPosVenda: (ordemServicoId: number, posVendaId: number) =>
        apiRequest<PosVenda>(`/api/ordens-servico/${ordemServicoId}/pos-venda/${posVendaId}/encerrar`, {
            method: 'POST',
        }),

    usuariosEmpresa: () => apiRequest<UsuarioEmpresa[]>('/api/usuarios'),

    usuarioEmpresa: (id: number) => apiRequest<UsuarioEmpresa>(`/api/usuarios/${id}`),

    criarUsuarioEmpresa: (payload: UsuarioEmpresaPayload) =>
        apiRequest<UsuarioEmpresa>('/api/usuarios', { method: 'POST', body: payload }),

    atualizarUsuarioEmpresa: (id: number, payload: UsuarioEmpresaUpdatePayload) =>
        apiRequest<UsuarioEmpresa>(`/api/usuarios/${id}`, { method: 'PUT', body: payload }),

    redefinirSenhaUsuario: (id: number, senha: string) =>
        apiRequest<void>(`/api/usuarios/${id}/senha`, { method: 'PUT', body: { senha } }),

    excluirUsuarioEmpresa: (id: number) =>
        apiRequest<void>(`/api/usuarios/${id}`, { method: 'DELETE' }),

    minhaEmpresa: () => apiRequest<MinhaEmpresa>('/api/empresa'),

    atualizarMinhaEmpresa: (payload: MinhaEmpresaPayload) =>
        apiRequest<MinhaEmpresa>('/api/empresa', { method: 'PUT', body: payload }),

    agendaEmpresa: () => apiRequest<AgendaEmpresa>('/api/empresa/agenda'),

    atualizarAgendaEmpresa: (payload: AgendaEmpresa) =>
        apiRequest<AgendaEmpresa>('/api/empresa/agenda', { method: 'PUT', body: payload }),

    perguntasFrequentes: () => apiRequest<PerguntaFrequente[]>('/api/perguntas-frequentes'),

    criarPerguntaFrequente: (payload: PerguntaFrequentePayload) =>
        apiRequest<PerguntaFrequente>('/api/perguntas-frequentes', { method: 'POST', body: payload }),

    atualizarPerguntaFrequente: (id: number, payload: PerguntaFrequentePayload) =>
        apiRequest<PerguntaFrequente>(`/api/perguntas-frequentes/${id}`, { method: 'PUT', body: payload }),

    excluirPerguntaFrequente: (id: number) =>
        apiRequest<void>(`/api/perguntas-frequentes/${id}`, { method: 'DELETE' }),
};
