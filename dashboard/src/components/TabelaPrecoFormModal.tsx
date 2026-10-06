import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import { ApiRequestError } from '../lib/apiClient';
import type {
    AcabamentoVidro,
    CategoriaItemPreco,
    CorVidro,
    TabelaPreco,
    TipoVidro,
    UnidadeMedida,
} from '../types';

interface TabelaPrecoFormModalProps {
    item?: TabelaPreco;
    onClose: () => void;
    onSaved: () => void;
}

const categorias: { valor: CategoriaItemPreco; rotulo: string }[] = [
    { valor: 'VIDRO', rotulo: 'Vidro' },
    { valor: 'FERRAGEM', rotulo: 'Ferragem' },
    { valor: 'KIT', rotulo: 'Kit' },
    { valor: 'MAO_DE_OBRA', rotulo: 'Mão de obra' },
    { valor: 'DESLOCAMENTO', rotulo: 'Deslocamento' },
    { valor: 'OUTRO', rotulo: 'Outro' },
];

const tiposVidro: TipoVidro[] = ['COMUM', 'TEMPERADO', 'LAMINADO', 'ESPELHO'];
const cores: CorVidro[] = ['INCOLOR', 'FUME', 'VERDE', 'BRONZE'];
const acabamentos: AcabamentoVidro[] = ['JATEADO', 'ACIDATO', 'SERIGRAFADO', 'CANELADO'];
const unidades: UnidadeMedida[] = ['M2', 'ML', 'UN', 'KIT', 'HORA', 'KM'];

export function TabelaPrecoFormModal({ item, onClose, onSaved }: TabelaPrecoFormModalProps) {

    const queryClient = useQueryClient();
    const [erro, setErro] = useState<string | null>(null);

    const [categoria, setCategoria] = useState<CategoriaItemPreco>(item?.categoria ?? 'VIDRO');
    const [descricao, setDescricao] = useState(item?.descricao ?? '');
    const [tipoVidro, setTipoVidro] = useState<TipoVidro | ''>(item?.tipoVidro ?? '');
    const [espessuraMm, setEspessuraMm] = useState(item?.espessuraMm ? String(item.espessuraMm) : '');
    const [cor, setCor] = useState<CorVidro | ''>(item?.cor ?? '');
    const [acabamento, setAcabamento] = useState<AcabamentoVidro | ''>(item?.acabamento ?? '');
    const [unidade, setUnidade] = useState<UnidadeMedida>(item?.unidade ?? 'M2');
    const [custo, setCusto] = useState(item?.custo != null ? String(item.custo) : '');
    const [precoVenda, setPrecoVenda] = useState(item?.precoVenda != null ? String(item.precoVenda) : '');
    const [fornecedor, setFornecedor] = useState(item?.fornecedor ?? '');

    const ehVidro = categoria === 'VIDRO';

    const mutation = useMutation({
        mutationFn: () => {

            const payload = {
                categoria,
                descricao: descricao.trim(),
                tipoVidro: ehVidro && tipoVidro ? tipoVidro : undefined,
                espessuraMm: ehVidro && espessuraMm ? Number(espessuraMm) : undefined,
                cor: ehVidro && cor ? cor : undefined,
                acabamento: ehVidro && acabamento ? acabamento : undefined,
                unidade,
                custo: custo ? Number(custo.replace(',', '.')) : undefined,
                precoVenda: Number(precoVenda.replace(',', '.')),
                fornecedor: fornecedor.trim() || undefined,
                ativo: item?.ativo,
                origem: item?.origem,
                formulaOrigem: item?.formulaOrigem ?? undefined,
            };

            return item
                ? api.atualizarTabelaPreco(item.id, payload)
                : api.criarTabelaPreco(payload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['tabela-precos'] });
            onSaved();
        },
        onError: (excecao) =>
            setErro(
                excecao instanceof ApiRequestError ? excecao.message : 'Não foi possível salvar o item'
            ),
    });

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        setErro(null);
        mutation.mutate();
    }

    return (
        <Modal title={item ? 'Editar item da tabela' : 'Novo item da tabela'} onClose={onClose} largura="lg">
            <form onSubmit={aoSubmeter} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField label="Categoria">
                        <select
                            value={categoria}
                            onChange={(e) => setCategoria(e.target.value as CategoriaItemPreco)}
                            className={inputClass}
                        >
                            {categorias.map((c) => (
                                <option key={c.valor} value={c.valor}>{c.rotulo}</option>
                            ))}
                        </select>
                    </FormField>

                    <FormField label="Unidade">
                        <select value={unidade} onChange={(e) => setUnidade(e.target.value as UnidadeMedida)} className={inputClass}>
                            {unidades.map((u) => (
                                <option key={u} value={u}>{u}</option>
                            ))}
                        </select>
                    </FormField>
                </div>

                <FormField label="Descrição">
                    <input
                        type="text"
                        required
                        value={descricao}
                        onChange={(e) => setDescricao(e.target.value)}
                        className={inputClass}
                        placeholder="Ex.: Temperado 8mm incolor"
                    />
                </FormField>

                {ehVidro && (
                    <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                        <FormField label="Tipo">
                            <select value={tipoVidro} onChange={(e) => setTipoVidro(e.target.value as TipoVidro)} className={inputClass}>
                                <option value="">—</option>
                                {tiposVidro.map((t) => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </FormField>
                        <FormField label="Espessura (mm)">
                            <input
                                type="number"
                                value={espessuraMm}
                                onChange={(e) => setEspessuraMm(e.target.value)}
                                className={inputClass}
                            />
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
                )}

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <FormField label="Seu custo (opcional, R$)">
                        <input
                            type="text"
                            inputMode="decimal"
                            value={custo}
                            onChange={(e) => setCusto(e.target.value)}
                            className={inputClass}
                            placeholder="0,00"
                        />
                    </FormField>
                    <FormField label="Preço de venda (R$)">
                        <input
                            type="text"
                            inputMode="decimal"
                            required
                            value={precoVenda}
                            onChange={(e) => setPrecoVenda(e.target.value)}
                            className={inputClass}
                            placeholder="0,00"
                        />
                    </FormField>
                </div>

                <FormField label="Fornecedor (opcional)">
                    <input
                        type="text"
                        value={fornecedor}
                        onChange={(e) => setFornecedor(e.target.value)}
                        className={inputClass}
                    />
                </FormField>

                {erro && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}

                <div className="flex justify-end gap-2 pt-2">
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                    >
                        Cancelar
                    </button>
                    <button
                        type="submit"
                        disabled={mutation.isPending}
                        className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {mutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        {item ? 'Salvar' : 'Adicionar'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}
