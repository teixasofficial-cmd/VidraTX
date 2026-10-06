import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2, Plus, Trash2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import { usePodeAlterarPrecos } from '../lib/auth';
import type {
    AcabamentoVidro,
    ComponenteManualPayload,
    CorVidro,
    OrcamentoItem,
    TipoComponenteCusto,
    TipoVidro,
} from '../types';

interface ItemOrcamentoFormModalProps {
    orcamentoId: number;
    item?: OrcamentoItem;
    onClose: () => void;
    onSaved: () => void;
}

const tiposVidro: TipoVidro[] = ['COMUM', 'TEMPERADO', 'LAMINADO', 'ESPELHO'];
const cores: CorVidro[] = ['INCOLOR', 'FUME', 'VERDE', 'BRONZE'];
const acabamentos: AcabamentoVidro[] = ['JATEADO', 'ACIDATO', 'SERIGRAFADO', 'CANELADO'];

const tiposComponente: { valor: TipoComponenteCusto; rotulo: string }[] = [
    { valor: 'FERRAGEM', rotulo: 'Ferragem' },
    { valor: 'KIT', rotulo: 'Kit' },
    { valor: 'MAO_DE_OBRA', rotulo: 'Mão de obra' },
    { valor: 'DESLOCAMENTO', rotulo: 'Deslocamento' },
    { valor: 'LAPIDACAO', rotulo: 'Lapidação' },
    { valor: 'BISOTE', rotulo: 'Bisotê' },
    { valor: 'FURO', rotulo: 'Furo' },
    { valor: 'RECORTE', rotulo: 'Recorte' },
    { valor: 'ITEM_LIVRE', rotulo: 'Item livre' },
];

type ComponenteEdicao = ComponenteManualPayload & { chaveLocal: string; existente: boolean };

export function ItemOrcamentoFormModal({ orcamentoId, item, onClose, onSaved }: ItemOrcamentoFormModalProps) {

    const queryClient = useQueryClient();
    const podeAlterarPrecos = usePodeAlterarPrecos();
    const [erro, setErro] = useState<string | null>(null);

    const { data: tipologias } = useQuery({ queryKey: ['tipologias'], queryFn: () => api.tipologias(true) });
    const { data: tabelaPrecos } = useQuery({ queryKey: ['tabela-precos'], queryFn: () => api.tabelaPrecos(true) });

    const [tipologiaId, setTipologiaId] = useState(item?.tipologiaId ? String(item.tipologiaId) : '');
    const [ambiente, setAmbiente] = useState(item?.ambiente ?? '');
    const [larguraCm, setLarguraCm] = useState(item?.larguraVaoMm ? String(item.larguraVaoMm / 10) : '');
    const [alturaCm, setAlturaCm] = useState(item?.alturaVaoMm ? String(item.alturaVaoMm / 10) : '');
    const [tipoVidro, setTipoVidro] = useState<TipoVidro | ''>(item?.tipoVidro ?? '');
    const [espessuraMm, setEspessuraMm] = useState(item?.espessuraMm ? String(item.espessuraMm) : '');
    const [cor, setCor] = useState<CorVidro | ''>(item?.cor ?? '');
    const [acabamento, setAcabamento] = useState<AcabamentoVidro | ''>(item?.acabamento ?? '');
    const [corFerragem, setCorFerragem] = useState(item?.corFerragem ?? '');
    const [quantidade, setQuantidade] = useState(item ? String(item.quantidade) : '1');

    const [componentes, setComponentes] = useState<ComponenteEdicao[]>(
        item
            ? item.linhas
                .filter((l) => l.tipo !== 'VIDRO' && l.tipo !== 'PERDAS')
                .map((l, indice) => ({
                    chaveLocal: `existente-${indice}`,
                    existente: true,
                    tipo: l.tipo,
                    descricao: l.componenteDescricao ?? l.descricao,
                    quantidade: Number(l.componenteQuantidade ?? l.quantidade),
                    tabelaPrecoId: l.tabelaPrecoId ?? undefined,
                    valorUnitario: l.tabelaPrecoId ? undefined : (l.valorUnitario ?? undefined),
                }))
            : []
    );

    function adicionarComponente() {
        setComponentes((atual) => [
            ...atual,
            { chaveLocal: `novo-${Date.now()}`, existente: false, tipo: 'FERRAGEM', descricao: '', quantidade: 1 },
        ]);
    }

    function atualizarComponente(chave: string, patch: Partial<ComponenteEdicao>) {
        setComponentes((atual) => atual.map((c) => (c.chaveLocal === chave ? { ...c, ...patch } : c)));
    }

    function removerComponente(chave: string) {
        setComponentes((atual) => atual.filter((c) => c.chaveLocal !== chave));
    }

    const mutation = useMutation({
        mutationFn: () => {

            const payload = {
                tipologiaId: Number(tipologiaId),
                ambiente: ambiente.trim() || undefined,
                larguraVaoMm: larguraCm ? Math.round(Number(larguraCm.replace(',', '.')) * 10) : undefined,
                alturaVaoMm: alturaCm ? Math.round(Number(alturaCm.replace(',', '.')) * 10) : undefined,
                tipoVidro: tipoVidro || undefined,
                espessuraMm: espessuraMm ? Number(espessuraMm) : undefined,
                cor: cor || undefined,
                acabamento: acabamento || undefined,
                corFerragem: corFerragem.trim() || undefined,
                quantidade: Number(quantidade) || 1,
                componentes: componentes
                    .filter((c) => c.descricao.trim())
                    .map(({ chaveLocal: _chaveLocal, existente: _existente, ...resto }) => resto),
            };

            return item
                ? api.atualizarOrcamentoItem(orcamentoId, item.id, payload)
                : api.criarOrcamentoItem(orcamentoId, payload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['orcamento-itens', orcamentoId] });
            queryClient.invalidateQueries({ queryKey: ['orcamento-totais', orcamentoId] });
            queryClient.invalidateQueries({ queryKey: ['orcamento', orcamentoId] });
            queryClient.invalidateQueries({ queryKey: ['orcamentos'] });
            onSaved();
        },
        onError: (excecao) =>
            setErro(excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível salvar o item'),
    });

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    const tabelaFiltrada = (tipo: TipoComponenteCusto) => {

        const categoriaEquivalente: Record<string, string> = {
            FERRAGEM: 'FERRAGEM', KIT: 'KIT', MAO_DE_OBRA: 'MAO_DE_OBRA', DESLOCAMENTO: 'DESLOCAMENTO',
        };

        const categoria = categoriaEquivalente[tipo];

        return (tabelaPrecos ?? []).filter((t) => !categoria || t.categoria === categoria);
    };

    return (
        <Modal title={item ? 'Editar item' : 'Novo item'} onClose={onClose} largura="lg">
            <form onSubmit={aoSubmeter} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField label="Tipologia">
                        <select
                            required
                            value={tipologiaId}
                            onChange={(e) => setTipologiaId(e.target.value)}
                            className={inputClass}
                        >
                            <option value="" disabled>Selecione...</option>
                            {tipologias?.map((t) => (
                                <option key={t.id} value={t.id}>{t.nome}</option>
                            ))}
                        </select>
                    </FormField>
                    <FormField label="Ambiente (opcional)">
                        <input
                            type="text"
                            value={ambiente}
                            onChange={(e) => setAmbiente(e.target.value)}
                            className={inputClass}
                            placeholder="Ex.: banheiro da suíte"
                        />
                    </FormField>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <FormField label="Largura do vão (cm)">
                        <input
                            type="text"
                            inputMode="decimal"
                            value={larguraCm}
                            onChange={(e) => setLarguraCm(e.target.value)}
                            className={inputClass}
                            placeholder="120"
                        />
                    </FormField>
                    <FormField label="Altura do vão (cm)">
                        <input
                            type="text"
                            inputMode="decimal"
                            value={alturaCm}
                            onChange={(e) => setAlturaCm(e.target.value)}
                            className={inputClass}
                            placeholder="190"
                        />
                    </FormField>
                </div>

                <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                    <FormField label="Vidro">
                        <select value={tipoVidro} onChange={(e) => setTipoVidro(e.target.value as TipoVidro)} className={inputClass}>
                            <option value="">—</option>
                            {tiposVidro.map((t) => <option key={t} value={t}>{t}</option>)}
                        </select>
                    </FormField>
                    <FormField label="Espessura (mm)">
                        <input type="number" value={espessuraMm} onChange={(e) => setEspessuraMm(e.target.value)} className={inputClass} />
                    </FormField>
                    <FormField label="Cor">
                        <select value={cor} onChange={(e) => setCor(e.target.value as CorVidro)} className={inputClass}>
                            <option value="">—</option>
                            {cores.map((c) => <option key={c} value={c}>{c}</option>)}
                        </select>
                    </FormField>
                    <FormField label="Acabamento">
                        <select value={acabamento} onChange={(e) => setAcabamento(e.target.value as AcabamentoVidro)} className={inputClass}>
                            <option value="">—</option>
                            {acabamentos.map((a) => <option key={a} value={a}>{a}</option>)}
                        </select>
                    </FormField>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <FormField label="Cor da ferragem (opcional)">
                        <input type="text" value={corFerragem} onChange={(e) => setCorFerragem(e.target.value)} className={inputClass} />
                    </FormField>
                    <FormField label="Quantidade">
                        <input type="number" min={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} className={inputClass} />
                    </FormField>
                </div>

                <div>
                    <div className="mb-2 flex items-center justify-between">
                        <span className="text-sm font-medium text-slate-700">
                            Ferragens, kit, mão de obra, deslocamento...
                        </span>
                        <button
                            type="button"
                            onClick={adicionarComponente}
                            className="flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-500"
                        >
                            <Plus className="h-3.5 w-3.5" />
                            Adicionar
                        </button>
                    </div>

                    <div className="space-y-2">
                        {componentes.map((componente) => (
                            <div key={componente.chaveLocal} className="flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 p-2">
                                <select
                                    value={componente.tipo}
                                    onChange={(e) => atualizarComponente(componente.chaveLocal, {
                                        tipo: e.target.value as TipoComponenteCusto, tabelaPrecoId: undefined,
                                    })}
                                    className="rounded-md border border-slate-300 px-2 py-1 text-xs"
                                >
                                    {tiposComponente.map((t) => <option key={t.valor} value={t.valor}>{t.rotulo}</option>)}
                                </select>
                                <input
                                    type="text"
                                    placeholder="Descrição"
                                    value={componente.descricao}
                                    onChange={(e) => atualizarComponente(componente.chaveLocal, { descricao: e.target.value })}
                                    className="min-w-[120px] flex-1 rounded-md border border-slate-300 px-2 py-1 text-xs"
                                />
                                <input
                                    type="number"
                                    step="0.01"
                                    min={0.001}
                                    value={componente.quantidade}
                                    onChange={(e) => atualizarComponente(componente.chaveLocal, { quantidade: Number(e.target.value) })}
                                    className="w-16 rounded-md border border-slate-300 px-2 py-1 text-xs"
                                    title="Quantidade"
                                />
                                <select
                                    value={componente.tabelaPrecoId ?? ''}
                                    onChange={(e) => atualizarComponente(componente.chaveLocal, {
                                        tabelaPrecoId: e.target.value ? Number(e.target.value) : undefined,
                                    })}
                                    className="rounded-md border border-slate-300 px-2 py-1 text-xs"
                                >
                                    {podeAlterarPrecos || (componente.existente && !componente.tabelaPrecoId) ? (
                                        <option value="">Valor manual</option>
                                    ) : (
                                        <option value="" disabled>Escolha da Tabela de Preços</option>
                                    )}
                                    {tabelaFiltrada(componente.tipo).map((t) => (
                                        <option key={t.id} value={t.id}>{t.descricao}</option>
                                    ))}
                                </select>
                                {!componente.tabelaPrecoId && (podeAlterarPrecos || componente.existente) && (
                                    <input
                                        type="number"
                                        step="0.01"
                                        min={0}
                                        placeholder="R$"
                                        value={componente.valorUnitario ?? ''}
                                        disabled={!podeAlterarPrecos}
                                        title={podeAlterarPrecos ? undefined : 'Só gerente ou administrador altera valores digitados à mão'}
                                        onChange={(e) => atualizarComponente(componente.chaveLocal, {
                                            valorUnitario: e.target.value === '' ? undefined : Number(e.target.value),
                                        })}
                                        className="w-20 rounded-md border border-slate-300 px-2 py-1 text-xs disabled:bg-slate-50 disabled:text-slate-500"
                                    />
                                )}
                                <button
                                    type="button"
                                    onClick={() => removerComponente(componente.chaveLocal)}
                                    className="rounded-md p-1 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                                >
                                    <Trash2 className="h-3.5 w-3.5" />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}

                <div className="flex justify-end gap-2 pt-2">
                    <button type="button" onClick={onClose} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50">
                        Cancelar
                    </button>
                    <button
                        type="submit"
                        disabled={mutation.isPending || !tipologiaId}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {mutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        {item ? 'Salvar' : 'Adicionar item'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}
