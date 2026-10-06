export type Perfil = 'ADMIN' | 'GERENTE' | 'FUNCIONARIO';

export interface Usuario {
    usuarioId: number;
    nome: string;
    email: string;
    perfil: Perfil;
    empresaId: number;
    empresaSlug: string;
    empresaNomeFantasia: string;
}

export interface LoginResponse extends Usuario {
    token: string;
    tipo: string;
}

export interface DashboardResumo {
    orcamentosRascunho: number;
    orcamentosEnviados: number;
    orcamentosAprovados: number;
    orcamentosRecusados: number;
    valorAprovado: number;
    valorTotalEnviado: number;
    totalOrcamentosEnviados: number;
    taxaConversaoPercentual: number;
    atendimentosPendentesWhatsapp: number;
    atendimentosAtrasadosWhatsapp: number;
    medicoesHoje: number;
    instalacoesHoje: number;
    acoesDaEmpresa: number;
    aguardandoCliente: number;
    mensagensNaoEntregues: number;
    medicoesAguardandoCliente: number;
    contrapropostasPendentes: number;
    instalacoesAguardandoAgendamento: number;
    orcamentosAprovadosSemOs: number;
    whatsappStatus: 'CONECTADO' | 'CONECTANDO' | 'DESCONECTADO' | 'NUNCA_CONECTADO';
    whatsappDesconectadoEm: string | null;
}

export type ResponsavelAcao = 'EMPRESA' | 'CLIENTE' | 'SISTEMA' | 'NINGUEM';

export type TipoProximaAcao = 'MEDICAO' | 'INSTALACAO' | 'ORCAMENTO' | 'ORDEM_SERVICO' | 'CONVERSA' | 'SOLICITACAO';

export interface ProximaAcaoItem {
    tipo: TipoProximaAcao;
    referenciaId: number;
    orcamentoId: number | null;
    ordemServicoId: number | null;
    atendimentoId: number | null;
    clienteNome: string | null;
    status: string;
    responsavel: ResponsavelAcao;
    descricao: string;
    data: string | null;
    urgente: boolean;
}

export interface AgendaItem {
    tipo: 'MEDICAO' | 'INSTALACAO';
    id: number;
    orcamentoId: number;
    ordemServicoId: number | null;
    clienteNome: string;
    endereco: string | null;
    equipe: string | null;
    status: StatusAgendamento;
    inicio: string;
    fim: string;
    cancelamentoSolicitado: boolean;
}

export interface AgendaEmpresa {
    duracaoMedicaoMinutos: number;
    duracaoInstalacaoMinutos: number;
    medicoesSimultaneas: number;
    horaInicioMensagens: number;
    horaFimMensagens: number;
    fusoHorario: string;
    antecedenciaMinimaMinutos: number;
    prazoRespostaAtendenteMinutos: number;
    mensagemForaHorario: string;
    condicoesPagamento: string;
    lembreteOrcamentoAtivo: boolean;
}

export interface PerguntaFrequente {
    id: number;
    pergunta: string;
    palavrasChave: string;
    resposta: string;
    ativa: boolean;
}

export type PerguntaFrequentePayload = Omit<PerguntaFrequente, 'id'>;

export type StatusAtendimento =
    | 'EM_FLUXO_BOT'
    | 'AGUARDANDO_ATENDENTE'
    | 'EM_ATENDIMENTO_HUMANO'
    | 'ENCERRADO';

export type EtapaFluxo =
    | 'COLETA_NOME'
    | 'MENU'
    | 'COLETA_SERVICO'
    | 'COLETA_DESCRICAO'
    | 'CONFIRMACAO'
    | 'CORRECAO'
    | 'DUVIDA';

export type RemetenteMensagem = 'CLIENTE' | 'BOT' | 'ATENDENTE';

export type TipoMensagem = 'TEXTO' | 'IMAGEM';

export interface AtendimentoWhatsapp {
    id: number;
    clienteId: number | null;
    clienteNome: string | null;
    telefone: string;
    status: StatusAtendimento;
    etapaFluxo: EtapaFluxo | null;
    tentativasErro: number;
    solicitacaoOrcamentoId: number | null;
    atendenteId: number | null;
    atendenteNome: string | null;
    criadoEm: string;
    atualizadoEm: string;
    encerradoEm: string | null;
    motivoEncerramento: string | null;
    ultimaMensagemClienteEm: string | null;
    ultimaMensagemEmpresaEm: string | null;
    clienteAguardandoResposta: boolean;
    responsavelProximaAcao: ResponsavelAcao | null;
    proximaAcao: string | null;
    pendencias: string[] | null;
    minutosEsperando: number;
    respostaAtrasada: boolean;
}

export type StatusEnvio = 'PENDENTE' | 'ENVIANDO' | 'ENVIADA' | 'ENTREGUE' | 'LIDA' | 'FALHOU' | 'CANCELADA' | 'SEM_WHATSAPP';

export interface MensagemAtendimento {
    id: number;
    remetente: RemetenteMensagem;
    tipo: TipoMensagem;
    conteudo: string | null;
    midiaUrl: string | null;
    enviadoEm: string;
    envioStatus: StatusEnvio | null;
    envioErro: string | null;
}

export interface AtendimentoWhatsappDetalhe {
    atendimento: AtendimentoWhatsapp;
    mensagens: MensagemAtendimento[];
    historico: HistoricoEvento[];
}

export interface AtendenteResumo {
    id: number;
    nome: string;
}

export interface EncerrarAtendimentoPayload {
    forcar?: boolean;
    motivo?: string;
}

export interface AtendimentosPendentesContagem {
    quantidade: number;
}

export type StatusInstanciaWhatsapp = 'DESCONECTADO' | 'CONECTANDO' | 'CONECTADO';

export interface WhatsappInstancia {
    id: number;
    numero: string | null;
    status: StatusInstanciaWhatsapp;
    conectadoEm: string | null;
    criadoEm: string;
    qrCode: string | null;
    mensagensNaFila: number;
    mensagensComFalha: number;
}

export interface Cliente {
    id: number;
    empresaId: number;
    nome: string;
    telefone: string | null;
    whatsapp: string | null;
    email: string | null;
    cpfCnpj: string | null;
    endereco: string | null;
    observacoes: string | null;
    criadoEm: string;
    atualizadoEm: string;
}

export interface ClientePayload {
    nome: string;
    telefone?: string;
    whatsapp?: string;
    email?: string;
    cpfCnpj?: string;
    endereco?: string;
    observacoes?: string;
}

export type StatusOrcamento =
    | 'NOVO_CONTATO'
    | 'PRE_ORCAMENTO'
    | 'VISITA_AGENDADA'
    | 'MEDIDO'
    | 'ORCAMENTO_FINAL'
    | 'ENVIADO'
    | 'APROVADO'
    | 'PERDIDO'
    | 'EXPIRADO';

export type MotivoPerda =
    | 'PRECO_ALTO'
    | 'ESCOLHEU_CONCORRENTE'
    | 'DESISTIU_DO_SERVICO'
    | 'SEM_RETORNO_DO_CLIENTE'
    | 'RECUSADO_VIA_WHATSAPP'
    | 'OUTRO';

export interface Orcamento {
    id: number;
    empresaId: number;
    clienteId: number;
    clienteNome: string;
    clienteTelefone: string | null;
    clienteWhatsapp: string | null;
    clienteEmail: string | null;
    solicitacaoOrcamentoId: number | null;
    especificacoes: string | null;
    status: StatusOrcamento;
    observacoes: string | null;
    validoAte: string | null;
    valorTotal: number;
    criadoEm: string;
    atualizadoEm: string;
    enviadoEm: string | null;
    respondidoEm: string | null;
    motivoPerda: MotivoPerda | null;
    motivoPerdaOutro: string | null;
    custoTotal: number | null;
    precoSugerido: number | null;
    ajusteComercial: number;
    margemReal: number | null;
    revisaoEnvio: number;
    parcelasCartao: number | null;
    precoFinalManual: number | null;
    alteracaoSolicitadaEm: string | null;
    alteracaoSolicitadaTexto: string | null;
    estimativaEnviadaEm: string | null;
    vencido: boolean;
    envioStatus: StatusEnvio | null;
    envioErro: string | null;
    responsavelProximaAcao: ResponsavelAcao | null;
    proximaAcao: string | null;
    medicaoStatus: StatusAgendamento | null;
    medicaoData: string | null;
    ordemServicoId: number | null;
    aviso: string | null;
}

export interface MoverPipelinePayload {
    status: StatusOrcamento;
    motivoPerda?: MotivoPerda;
    motivoPerdaOutro?: string;
}

export interface PerderOrcamentoPayload {
    motivoPerda: MotivoPerda;
    motivoPerdaOutro?: string;
}

export interface OrcamentoPayload {
    clienteId: number;
    observacoes?: string;
    validoAte?: string;
}

export interface EnviarOrcamentoPayload {
    valorEsperado?: number;
    medicaoPendente?: 'CANCELAR' | 'MANTER';
}

export interface ReabrirOrcamentoPayload {
    motivo?: string;
}

export type TipoEventoHistorico =
    | 'ORCAMENTO_CRIADO'
    | 'ORCAMENTO_ALTERADO'
    | 'ORCAMENTO_ENVIADO'
    | 'ORCAMENTO_APROVADO'
    | 'ORCAMENTO_PERDIDO'
    | 'ORCAMENTO_EXPIRADO'
    | 'ORCAMENTO_MOVIDO_PIPELINE'
    | 'ORCAMENTO_ITEM_ADICIONADO'
    | 'ORCAMENTO_ITEM_REMOVIDO'
    | 'ORCAMENTO_LINHA_EDITADA'
    | 'ORCAMENTO_REVISAO'
    | 'ORCAMENTO_ALTERACAO_SOLICITADA'
    | 'ORCAMENTO_ESTIMATIVA_ENVIADA'
    | 'ORCAMENTO_REABERTO'
    | 'ORDEM_SERVICO_CRIADA'
    | 'SOLICITACAO_DESCARTADA'
    | 'TABELA_PRECO_ATUALIZADA'
    | 'PARAMETRO_CALCULO_ATUALIZADO'
    | 'INSTALACAO_AGENDADA'
    | 'INSTALACAO_REAGENDADA'
    | 'INSTALACAO_CONFIRMADA_CLIENTE'
    | 'INSTALACAO_RECUSADA_CLIENTE'
    | 'INSTALACAO_CONTRAPROPOSTA_CLIENTE'
    | 'INSTALACAO_CONTRAPROPOSTA_ACEITA'
    | 'INSTALACAO_CONTRAPROPOSTA_RECUSADA'
    | 'INSTALACAO_CONFIRMADA_MANUAL'
    | 'INSTALACAO_REAGENDAMENTO_NECESSARIO'
    | 'INSTALACAO_CANCELADA'
    | 'INSTALACAO_REALIZADA'
    | 'INSTALACAO_CANCELAMENTO_SOLICITADO'
    | 'INSTALACAO_DATA_MANTIDA'
    | 'MEDICAO_AGENDADA'
    | 'MEDICAO_REAGENDADA'
    | 'MEDICAO_CONFIRMADA_CLIENTE'
    | 'MEDICAO_RECUSADA_CLIENTE'
    | 'MEDICAO_CONTRAPROPOSTA_CLIENTE'
    | 'MEDICAO_CONTRAPROPOSTA_ACEITA'
    | 'MEDICAO_CONTRAPROPOSTA_RECUSADA'
    | 'MEDICAO_CONFIRMADA_MANUAL'
    | 'MEDICAO_REAGENDAMENTO_NECESSARIO'
    | 'MEDICAO_CANCELADA'
    | 'MEDICAO_REALIZADA'
    | 'MEDICAO_CANCELAMENTO_SOLICITADO'
    | 'MEDICAO_DATA_MANTIDA'
    | 'ATENDIMENTO_ESCALADO_ATENDENTE'
    | 'ATENDIMENTO_ASSUMIDO'
    | 'ATENDIMENTO_ENCERRADO'
    | 'ATENDIMENTO_TRANSFERIDO'
    | 'ATENDIMENTO_DEVOLVIDO_BOT'
    | 'ATENDIMENTO_REABERTO'
    | 'MENSAGEM_NAO_ENTREGUE';

export interface HistoricoEvento {
    id: number;
    tipo: TipoEventoHistorico;
    descricao: string;
    usuarioId: number | null;
    usuarioNome: string | null;
    criadoEm: string;
}

export interface Servico {
    id: number;
    empresaId: number;
    nome: string;
    descricao: string | null;
    ativo: boolean;
    criadoEm: string;
    atualizadoEm: string;
}

export interface ServicoPayload {
    nome: string;
    descricao?: string;
    ativo?: boolean;
}

export interface Material {
    id: number;
    empresaId: number;
    nome: string;
    descricao: string | null;
    ativo: boolean;
    criadoEm: string;
    atualizadoEm: string;
}

export interface MaterialPayload {
    nome: string;
    descricao?: string;
    ativo?: boolean;
}

export type StatusProducao = 'AGUARDANDO_PRODUCAO' | 'EM_PRODUCAO' | 'PRONTO' | 'CONFERIDO';

export interface OrdemServico {
    id: number;
    empresaId: number;
    orcamentoId: number;
    clienteNome: string;
    necessitaProducao: boolean;
    statusProducao: StatusProducao | null;
    observacoes: string | null;
    criadoEm: string;
    atualizadoEm: string;
    producaoIniciadaEm: string | null;
    producaoConcluidaEm: string | null;
    producaoConferidaEm: string | null;
}

export interface OrdemServicoPayload {
    orcamentoId: number;
    necessitaProducao?: boolean;
    observacoes?: string;
}

export interface OrdemServicoUpdatePayload {
    observacoes?: string;
}

export type StatusAgendamento =
    | 'PROPOSTA_ENVIADA'
    | 'CONTRAPROPOSTA_CLIENTE'
    | 'RECUSADA_CLIENTE'
    | 'REAGENDAMENTO_NECESSARIO'
    | 'AGENDADA'
    | 'REALIZADA'
    | 'CANCELADA';

export interface AgendamentoNegociacao {
    status: StatusAgendamento;
    dataAgendada: string;
    contrapropostaTexto: string | null;
    contrapropostaData: string | null;
    contrapropostaEm: string | null;
    versaoProposta: number | null;
    envioStatus: StatusEnvio | null;
    envioErro: string | null;
    responsavelProximaAcao: ResponsavelAcao | null;
    proximaAcao: string | null;
    aviso: string | null;
    cancelamentoSolicitadoEm: string | null;
    cancelamentoSolicitadoTexto: string | null;
}

export type OrigemProposta = 'EMPRESA' | 'CLIENTE';
export type StatusProposta = 'PENDENTE' | 'ACEITA' | 'RECUSADA' | 'SUBSTITUIDA' | 'CANCELADA' | 'EXPIRADA';

export interface PropostaAgendamento {
    id: number;
    versao: number;
    origem: OrigemProposta;
    dataProposta: string | null;
    textoCliente: string | null;
    equipe: string | null;
    status: StatusProposta;
    motivo: string | null;
    criadoPorNome: string | null;
    respondidoPeloCliente: boolean | null;
    respondidoPorNome: string | null;
    criadoEm: string;
    respondidoEm: string | null;
    envioStatus: StatusEnvio | null;
}

export interface RecusarContrapropostaPayload {
    dataAgendada: string;
    motivo?: string;
    equipeResponsavel?: string;
}

export interface CancelarAgendamentoPayload {
    motivo?: string;
}

export interface Instalacao extends AgendamentoNegociacao {
    id: number;
    ordemServicoId: number;
    clienteNome: string;
    dataRealizada: string | null;
    endereco: string | null;
    equipeResponsavel: string | null;
    checklist: string | null;
    observacoes: string | null;
    criadoEm: string;
    atualizadoEm: string;
}

export interface InstalacaoPayload {
    dataAgendada: string;
    endereco?: string;
    observacoes?: string;
    equipeResponsavel?: string;
}

export interface InstalacaoReagendarPayload {
    dataAgendada: string;
    equipeResponsavel?: string;
}

export interface InstalacaoRealizarPayload {
    checklist?: string;
    observacoes?: string;
}

export interface Medicao extends AgendamentoNegociacao {
    id: number;
    orcamentoId: number;
    clienteNome: string;
    dataRealizada: string | null;
    endereco: string | null;
    observacoes: string | null;
    criadoEm: string;
    atualizadoEm: string;
}

export interface MedicaoPayload {
    dataAgendada: string;
    endereco?: string;
    observacoes?: string;
}

export interface MedicaoReagendarPayload {
    dataAgendada: string;
}

export interface MedicaoRealizarPayload {
    observacoes?: string;
}

export type StatusPosVenda = 'ABERTA' | 'EM_ATENDIMENTO' | 'RESOLVIDA' | 'ENCERRADA';

export interface PosVenda {
    id: number;
    ordemServicoId: number;
    clienteNome: string;
    status: StatusPosVenda;
    problema: string;
    atendimento: string | null;
    solucao: string | null;
    criadoEm: string;
    atualizadoEm: string;
    resolvidoEm: string | null;
    encerradoEm: string | null;
}

export interface PosVendaPayload {
    problema: string;
}

export interface PosVendaAtendimentoPayload {
    atendimento: string;
}

export interface PosVendaResolverPayload {
    solucao: string;
}

export interface UsuarioEmpresa {
    id: number;
    empresaId: number;
    nome: string;
    email: string;
    perfil: Perfil;
    ativo: boolean;
    criadoEm: string;
    atualizadoEm: string;
}

export interface UsuarioEmpresaPayload {
    nome: string;
    email: string;
    senha: string;
    perfil: Perfil;
    ativo?: boolean;
}

export interface UsuarioEmpresaUpdatePayload {
    nome: string;
    email: string;
    perfil: Perfil;
    ativo: boolean;
}

export interface MinhaEmpresa {
    id: number;
    razaoSocial: string;
    nomeFantasia: string;
    cnpj: string;
    slug: string;
    email: string | null;
    telefone: string | null;
    whatsapp: string | null;
    logoUrl: string | null;
    corPrimaria: string | null;
    corSecundaria: string | null;
    endereco: string | null;
    sobre: string | null;
    ativa: boolean;
    criadoEm: string;
    atualizadoEm: string;
}

export interface MinhaEmpresaPayload {
    razaoSocial: string;
    nomeFantasia: string;
    cnpj: string;
    email?: string;
    telefone?: string;
    whatsapp?: string;
    logoUrl?: string;
    corPrimaria?: string;
    corSecundaria?: string;
    endereco?: string;
    sobre?: string;
}

export interface SuperAdminLoginResponse {
    token: string;
    tipo: string;
    administradorId: number;
    email: string;
}

export interface EmpresaSuperAdmin {
    id: number;
    nome: string;
    cnpj: string;
    slug: string;
    email: string;
    telefone: string | null;
    endereco: string | null;
    ativa: boolean;
    totalUsuarios: number;
    criadoEm: string;
    whatsappStatus: StatusInstanciaWhatsapp | 'NAO_CONFIGURADO';
    whatsappDesconectadoDesde: string | null;
}

export interface EmpresaSuperAdminPayload {
    nome: string;
    cnpj: string;
    email: string;
    telefone: string;
    endereco: string;
    senha?: string;
}

export interface SuperAdminResumo {
    totalEmpresas: number;
    empresasAtivas: number;
    empresasInativas: number;
    whatsappDesconectados: number;
}

export type CategoriaItemPreco = 'VIDRO' | 'FERRAGEM' | 'KIT' | 'MAO_DE_OBRA' | 'DESLOCAMENTO' | 'OUTRO';
export type TipoVidro = 'COMUM' | 'TEMPERADO' | 'LAMINADO' | 'ESPELHO';
export type CorVidro = 'INCOLOR' | 'FUME' | 'VERDE' | 'BRONZE';
export type AcabamentoVidro = 'JATEADO' | 'ACIDATO' | 'SERIGRAFADO' | 'CANELADO';
export type UnidadeMedida = 'M2' | 'ML' | 'UN' | 'KIT' | 'HORA' | 'KM';
export type OrigemPreco = 'MANUAL' | 'DERIVADO' | 'REFERENCIA_REGIONAL';

export interface TabelaPreco {
    id: number;
    empresaId: number;
    categoria: CategoriaItemPreco;
    descricao: string;
    tipoVidro: TipoVidro | null;
    espessuraMm: number | null;
    cor: CorVidro | null;
    acabamento: AcabamentoVidro | null;
    unidade: UnidadeMedida;
    custo: number | null;
    precoVenda: number;
    precoAbaixoDoCusto: boolean;
    fornecedor: string | null;
    origem: OrigemPreco;
    formulaOrigem: string | null;
    ativo: boolean;
    criadoEm: string;
    atualizadoEm: string;
}

export interface TabelaPrecoPayload {
    categoria: CategoriaItemPreco;
    descricao: string;
    tipoVidro?: TipoVidro;
    espessuraMm?: number;
    cor?: CorVidro;
    acabamento?: AcabamentoVidro;
    unidade: UnidadeMedida;
    custo?: number;
    precoVenda: number;
    fornecedor?: string;
    ativo?: boolean;
    origem?: OrigemPreco;
    formulaOrigem?: string;
}

export type ModoPrecificacao = 'CUSTO' | 'VENDA';
export type ArredondamentoComercial = 'NENHUM' | 'PROXIMA_DEZENA' | 'TERMINAR_90' | 'PROXIMA_CENTENA';
export type RegraDeslocamento = 'FIXO' | 'POR_KM' | 'POR_BAIRRO';
export type RegimeTributario = 'MEI' | 'SIMPLES_NACIONAL' | 'OUTRO';

export interface ParametroCalculo {
    id: number;
    empresaId: number;
    modoPrecificacao: ModoPrecificacao;
    multiploArredondamentoMm: number;
    areaMinimaM2: number;
    percentualPerdas: number;
    percentualImpostos: number;
    percentualComissao: number;
    percentualMargemDesejada: number;
    percentualMargemMinima: number;
    taxaCartaoParcelas: Record<string, number>;
    arredondamentoComercial: ArredondamentoComercial;
    variacaoPreOrcamentoMinPct: number;
    variacaoPreOrcamentoMaxPct: number;
    valorVisitaTecnica: number | null;
    regraDeslocamento: RegraDeslocamento;
    valorDeslocamento: number | null;
    validadePadraoDias: number;
    tamanhoMaximoChapaLarguraMm: number;
    tamanhoMaximoChapaAlturaMm: number;
    toleranciaPrumoNivelMm: number;
    regimeTributario: RegimeTributario | null;
}

export type ParametroCalculoPayload = Omit<ParametroCalculo, 'id' | 'empresaId'>;

export type CategoriaTipologia =
    | 'BOX'
    | 'ESPELHO'
    | 'PORTA'
    | 'JANELA'
    | 'VIDRO_AVULSO'
    | 'GUARDA_CORPO'
    | 'ENVIDRACAMENTO_SACADA'
    | 'TAMPO_PRATELEIRA'
    | 'ITEM_LIVRE';

export interface Tipologia {
    id: number;
    empresaId: number;
    codigo: string;
    nome: string;
    categoria: CategoriaTipologia;
    numeroFolhas: number;
    formulaPecas: string;
    descontoLarguraMm: number;
    descontoAlturaMm: number;
    transpasseMm: number;
    alertasNormativos: string[];
    ativo: boolean;
}

export interface TipologiaAjustePayload {
    descontoLarguraMm: number;
    descontoAlturaMm: number;
    transpasseMm: number;
    ativo?: boolean;
}

export type TipoComponenteCusto =
    | 'VIDRO'
    | 'LAPIDACAO'
    | 'BISOTE'
    | 'FURO'
    | 'RECORTE'
    | 'FERRAGEM'
    | 'KIT'
    | 'MAO_DE_OBRA'
    | 'DESLOCAMENTO'
    | 'PERDAS'
    | 'AJUSTE_COMERCIAL'
    | 'ITEM_LIVRE';

export type OrigemSugestao = 'TABELA' | 'DERIVADO' | 'REFERENCIA_REGIONAL' | 'HISTORICO';

export interface AlertaCalculo {
    codigo: string;
    mensagem: string;
}

export interface OrcamentoPeca {
    id: number;
    descricao: string;
    larguraCorteMm: number;
    alturaCorteMm: number;
    quantidade: number;
    areaM2: number;
    excedeTamanhoMaximo: boolean;
}

export interface OrcamentoLinha {
    id: number;
    tipo: TipoComponenteCusto;
    descricao: string;
    componenteDescricao: string | null;
    componenteQuantidade: number | null;
    quantidade: number;
    tabelaPrecoId: number | null;
    valorUnitario: number | null;
    valorSugerido: number;
    valorFinal: number | null;
    valorExibido: number;
    origemSugestao: OrigemSugestao;
    editado: boolean;
    editadoPorNome: string | null;
    editadoEm: string | null;
}

export interface OrcamentoItem {
    id: number;
    orcamentoId: number;
    tipologiaId: number;
    tipologiaNome: string;
    ambiente: string | null;
    larguraVaoMm: number | null;
    alturaVaoMm: number | null;
    larguraVao2Mm: number | null;
    alturaVao2Mm: number | null;
    medidaTextoOriginal: string | null;
    medidaAproximada: boolean;
    tipoVidro: TipoVidro | null;
    espessuraMm: number | null;
    cor: CorVidro | null;
    acabamento: AcabamentoVidro | null;
    corFerragem: string | null;
    quantidade: number;
    observacoes: string | null;
    ordem: number;
    custoItem: number;
    pecas: OrcamentoPeca[];
    linhas: OrcamentoLinha[];
    alertas: AlertaCalculo[];
}

export interface ComponenteManualPayload {
    tipo: TipoComponenteCusto;
    descricao: string;
    quantidade: number;
    tabelaPrecoId?: number;
    valorUnitario?: number;
}

export interface OrcamentoItemPayload {
    tipologiaId: number;
    ambiente?: string;
    larguraVaoMm?: number;
    alturaVaoMm?: number;
    larguraVao2Mm?: number;
    alturaVao2Mm?: number;
    medidaTexto?: string;
    tipoVidro?: TipoVidro;
    espessuraMm?: number;
    cor?: CorVidro;
    acabamento?: AcabamentoVidro;
    corFerragem?: string;
    quantidade?: number;
    observacoes?: string;
    componentes?: ComponenteManualPayload[];
}

export interface OrcamentoTotais {
    custoTotal: number;
    deslocamento?: number | null;
    precoSugerido: number;
    ajusteComercial: number;
    valorFinal: number;
    margemReal: number | null;
    custoRealTotal: number | null;
    margemSobreCustoReal: number | null;
    custoRealIncompleto: boolean;
    valoresNaoRevisados: number;
    alertas: AlertaCalculo[];
    bloqueiosEnvio: string[];
    precoFinalManual: number | null;
    modoPrecificacao: ModoPrecificacao | null;
    parcelasCartao: number | null;
}
