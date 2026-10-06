import { useEffect, useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2, Save } from 'lucide-react';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { FormField, inputClass } from './FormField';
import { Spinner } from './Spinner';
import type { AgendaEmpresa } from '../types';

const FUSOS = [
    { valor: '', rotulo: 'Padrão do sistema' },
    { valor: 'America/Sao_Paulo', rotulo: 'Brasília' },
    { valor: 'America/Manaus', rotulo: 'Manaus (AM) −1h' },
    { valor: 'America/Cuiaba', rotulo: 'Cuiabá (MT) −1h' },
    { valor: 'America/Campo_Grande', rotulo: 'Campo Grande (MS) −1h' },
    { valor: 'America/Porto_Velho', rotulo: 'Porto Velho (RO) −1h' },
    { valor: 'America/Boa_Vista', rotulo: 'Boa Vista (RR) −1h' },
    { valor: 'America/Rio_Branco', rotulo: 'Rio Branco (AC) −2h' },
    { valor: 'America/Noronha', rotulo: 'Fernando de Noronha +1h' },
    { valor: 'America/Belem', rotulo: 'Belém (PA, AP)' },
    { valor: 'America/Fortaleza', rotulo: 'Fortaleza (CE, RN, PB, PI, MA)' },
    { valor: 'America/Recife', rotulo: 'Recife (PE)' },
    { valor: 'America/Bahia', rotulo: 'Salvador (BA)' },
    { valor: 'America/Araguaina', rotulo: 'Palmas (TO)' },
];
export function AgendaEmpresaPainel({ podeEditar }: { podeEditar: boolean }) {

    const queryClient = useQueryClient();
    const [form, setForm] = useState<AgendaEmpresa | null>(null);
    const [erro, setErro] = useState<string | null>(null);
    const [sucesso, setSucesso] = useState(false);

    const { data, isLoading, isError } = useQuery({
        queryKey: ['agenda-empresa'],
        queryFn: api.agendaEmpresa,
    });

    useEffect(() => {
        if (data) {
            setForm(data);
        }
    }, [data]);

    const mutation = useMutation({
        mutationFn: (payload: AgendaEmpresa) => api.atualizarAgendaEmpresa(payload),
        onSuccess: (resposta) => {
            queryClient.setQueryData(['agenda-empresa'], resposta);
            setSucesso(true);
            setTimeout(() => setSucesso(false), 3000);
        },
        onError: (excecao) =>
            setErro(excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível salvar a agenda'),
    });

    if (isLoading || (!form && !isError)) {
        return <Spinner />;
    }

    if (isError || !form) {
        return <p className="text-sm text-rose-600">Não foi possível carregar a configuração da agenda.</p>;
    }

    function campo(
        chave: Exclude<keyof AgendaEmpresa, 'fusoHorario' | 'mensagemForaHorario' | 'condicoesPagamento' | 'lembreteOrcamentoAtivo'>,
        valor: string,
    ) {
        setForm((atual) => (atual ? { ...atual, [chave]: Number(valor) } : atual));
    }

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);

        if (form && form.horaFimMensagens <= form.horaInicioMensagens) {
            setErro('O fim do horário de atendimento precisa ser depois do início');
            return;
        }

        if (form) {
            mutation.mutate(form);
        }
    }

    return (
        <form onSubmit={aoSubmeter} className="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div>
                <p className="text-sm font-semibold text-slate-900">Agenda, horário de atendimento e orçamento</p>
                <p className="mt-0.5 text-xs text-slate-500">
                    Usado para evitar dois agendamentos no mesmo horário, para não mandar lembretes de madrugada e
                    para avisar o cliente que chama fora do horário.
                </p>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <FormField label="Duração da medição (min)">
                    <input
                        type="number"
                        min={15}
                        max={600}
                        required
                        disabled={!podeEditar}
                        value={form.duracaoMedicaoMinutos}
                        onChange={(e) => campo('duracaoMedicaoMinutos', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
                <FormField label="Duração da instalação (min)">
                    <input
                        type="number"
                        min={30}
                        max={1440}
                        required
                        disabled={!podeEditar}
                        value={form.duracaoInstalacaoMinutos}
                        onChange={(e) => campo('duracaoInstalacaoMinutos', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
                <FormField label="Medições ao mesmo tempo">
                    <input
                        type="number"
                        min={1}
                        max={50}
                        required
                        disabled={!podeEditar}
                        value={form.medicoesSimultaneas}
                        onChange={(e) => campo('medicoesSimultaneas', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
            </div>

            <div className="grid grid-cols-2 gap-4">
                <FormField label="Atendimento de segunda a sábado, a partir de (hora)">
                    <input
                        type="number"
                        min={0}
                        max={23}
                        required
                        disabled={!podeEditar}
                        value={form.horaInicioMensagens}
                        onChange={(e) => campo('horaInicioMensagens', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
                <FormField label="Até (hora)">
                    <input
                        type="number"
                        min={1}
                        max={24}
                        required
                        disabled={!podeEditar}
                        value={form.horaFimMensagens}
                        onChange={(e) => campo('horaFimMensagens', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
            </div>

            <p className="text-xs text-slate-500">
                Instalações de equipes diferentes podem acontecer no mesmo horário; a mesma equipe não.
            </p>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <FormField label="Fuso horário da agenda">
                    <select
                        disabled={!podeEditar}
                        value={form.fusoHorario ?? ''}
                        onChange={(e) => setForm((atual) => (atual ? { ...atual, fusoHorario: e.target.value } : atual))}
                        className={inputClass}
                    >
                        {FUSOS.some((f) => f.valor === (form.fusoHorario ?? '')) ? null : (
                            <option value={form.fusoHorario}>{form.fusoHorario}</option>
                        )}
                        {FUSOS.map((f) => (
                            <option key={f.valor} value={f.valor}>{f.rotulo}</option>
                        ))}
                    </select>
                </FormField>
                <FormField label="Antecedência mínima para propor data (min)">
                    <input
                        type="number"
                        min={0}
                        max={10080}
                        required
                        disabled={!podeEditar}
                        value={form.antecedenciaMinimaMinutos}
                        onChange={(e) => campo('antecedenciaMinimaMinutos', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
                <FormField label="Prazo para responder quem pediu atendente (min)">
                    <input
                        type="number"
                        min={5}
                        max={1440}
                        required
                        disabled={!podeEditar}
                        value={form.prazoRespostaAtendenteMinutos}
                        onChange={(e) => campo('prazoRespostaAtendenteMinutos', e.target.value)}
                        className={inputClass}
                    />
                </FormField>
            </div>

            <p className="text-xs text-slate-500">
                Datas, "hoje", validade de orçamentos e horário de mensagens seguem o fuso da agenda. Passado o
                prazo de resposta, a conversa aparece como atrasada e o cliente recebe um aviso (uma vez), podendo
                responder 0 para voltar ao atendimento automático.
            </p>

            <FormField label="Mensagem para quem pede uma pessoa fora do horário">
                <textarea
                    rows={2}
                    maxLength={500}
                    disabled={!podeEditar}
                    value={form.mensagemForaHorario ?? ''}
                    placeholder="Estamos fora do horário de atendimento (segunda a sábado, das {inicio}h às {fim}h). Um atendente te responde a partir de {abertura}."
                    onChange={(e) => setForm((atual) => (atual ? { ...atual, mensagemForaHorario: e.target.value } : atual))}
                    className={inputClass}
                />
            </FormField>
            <p className="-mt-2 text-xs text-slate-500">
                Deixe vazio para usar o texto padrão. {'{abertura}'} vira o próximo horário, como "amanhã às 8h".
                O robô continua recebendo pedidos a qualquer hora.
            </p>

            <FormField label="Formas de pagamento (vão na mensagem do orçamento)">
                <input
                    maxLength={500}
                    disabled={!podeEditar}
                    value={form.condicoesPagamento ?? ''}
                    placeholder="Ex.: PIX, dinheiro ou cartão em até 10x"
                    onChange={(e) => setForm((atual) => (atual ? { ...atual, condicoesPagamento: e.target.value } : atual))}
                    className={inputClass}
                />
            </FormField>

            <label className="flex items-center gap-2 text-sm text-slate-700">
                <input
                    type="checkbox"
                    disabled={!podeEditar}
                    checked={form.lembreteOrcamentoAtivo ?? true}
                    onChange={(e) => setForm((atual) => (atual ? { ...atual, lembreteOrcamentoAtivo: e.target.checked } : atual))}
                    className="h-4 w-4 rounded border-slate-300"
                />
                Mandar um lembrete automático quando o cliente não responde o orçamento em 24 h
            </label>

            {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}
            {sucesso && <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-600">Configurações salvas.</p>}

            {podeEditar && (
                <div className="flex justify-end">
                    <button
                        type="submit"
                        disabled={mutation.isPending}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {mutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        <Save className="h-4 w-4" />
                        Salvar
                    </button>
                </div>
            )}
        </form>
    );
}
