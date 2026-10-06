import { useState, type ReactNode } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, ArrowRight, CheckCircle2, Loader2 } from 'lucide-react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import { api } from '../lib/api';
import type { ModoPrecificacao, RegimeTributario, TabelaPrecoPayload } from '../types';

interface ConfiguracaoExpressaWizardProps {
    onClose: () => void;
    onConcluido: () => void;
}

export function ConfiguracaoExpressaWizard({ onClose, onConcluido }: ConfiguracaoExpressaWizardProps) {

    const queryClient = useQueryClient();
    const [passo, setPasso] = useState(1);
    const [erro, setErro] = useState<string | null>(null);

    const [cidadeUf, setCidadeUf] = useState('');
    const [modo, setModo] = useState<ModoPrecificacao>('VENDA');
    const [temperado8, setTemperado8] = useState('');
    const [temperado10, setTemperado10] = useState('');
    const [percentualCor, setPercentualCor] = useState('15');
    const [boxInstalado, setBoxInstalado] = useState('');
    const [espelho4, setEspelho4] = useState('');
    const [cobraVisita, setCobraVisita] = useState(false);
    const [valorVisita, setValorVisita] = useState('');
    const [regime, setRegime] = useState<RegimeTributario>('SIMPLES_NACIONAL');
    const [margemDesejada, setMargemDesejada] = useState('25');

    const TOTAL_PASSOS = 8;

    const concluirMutation = useMutation({
        mutationFn: async () => {

            const t8 = Number(temperado8.replace(',', '.'));
            const t10 = Number(temperado10.replace(',', '.'));
            const fatorCor = 1 + Number(percentualCor.replace(',', '.')) / 100;

            const itens: TabelaPrecoPayload[] = [
                {
                    categoria: 'VIDRO', descricao: 'Temperado 8mm incolor', tipoVidro: 'TEMPERADO',
                    espessuraMm: 8, cor: 'INCOLOR', unidade: 'M2', precoVenda: t8, origem: 'MANUAL',
                },
                {
                    categoria: 'VIDRO', descricao: 'Temperado 10mm incolor', tipoVidro: 'TEMPERADO',
                    espessuraMm: 10, cor: 'INCOLOR', unidade: 'M2', precoVenda: t10, origem: 'MANUAL',
                },
            ];

            (['FUME', 'VERDE', 'BRONZE'] as const).forEach((corItem) => {

                itens.push({
                    categoria: 'VIDRO', descricao: `Temperado 8mm ${corItem.toLowerCase()}`, tipoVidro: 'TEMPERADO',
                    espessuraMm: 8, cor: corItem, unidade: 'M2',
                    precoVenda: Math.round(t8 * fatorCor * 100) / 100,
                    origem: 'DERIVADO',
                    formulaOrigem: `Temperado 8mm incolor (R$ ${t8.toFixed(2)}) × ${percentualCor}% (cor)`,
                });

                itens.push({
                    categoria: 'VIDRO', descricao: `Temperado 10mm ${corItem.toLowerCase()}`, tipoVidro: 'TEMPERADO',
                    espessuraMm: 10, cor: corItem, unidade: 'M2',
                    precoVenda: Math.round(t10 * fatorCor * 100) / 100,
                    origem: 'DERIVADO',
                    formulaOrigem: `Temperado 10mm incolor (R$ ${t10.toFixed(2)}) × ${percentualCor}% (cor)`,
                });
            });

            if (espelho4) {

                itens.push({
                    categoria: 'VIDRO', descricao: 'Espelho 4mm', tipoVidro: 'ESPELHO',
                    espessuraMm: 4, cor: 'INCOLOR', unidade: 'M2',
                    precoVenda: Number(espelho4.replace(',', '.')), origem: 'MANUAL',
                });
            }

            if (boxInstalado) {

                const areaReferencia = 2.28;
                const custoVidroReferencia = areaReferencia * t8;
                const valorInstalado = Number(boxInstalado.replace(',', '.'));
                const kitMaoDeObra = Math.max(0, Math.round((valorInstalado - custoVidroReferencia) * 100) / 100);

                itens.push({
                    categoria: 'KIT',
                    descricao: 'Kit + mão de obra (box frontal, calibrado)',
                    unidade: 'KIT',
                    precoVenda: kitMaoDeObra,
                    origem: 'DERIVADO',
                    formulaOrigem:
                        `Box frontal 8mm incolor instalado (R$ ${valorInstalado.toFixed(2)}) − vidro `
                        + `2,28 m² × R$ ${t8.toFixed(2)} = R$ ${custoVidroReferencia.toFixed(2)}`,
                });
            }

            for (const item of itens) {
                await api.criarTabelaPreco(item);
            }

            await api.atualizarParametrosCalculo({
                modoPrecificacao: modo,
                multiploArredondamentoMm: 50,
                areaMinimaM2: 0.25,
                percentualPerdas: 3,
                percentualImpostos: regime === 'MEI' ? 6 : regime === 'SIMPLES_NACIONAL' ? 8 : 10,
                percentualComissao: 0,
                percentualMargemDesejada: Number(margemDesejada.replace(',', '.')),
                percentualMargemMinima: Math.max(5, Number(margemDesejada.replace(',', '.')) - 10),
                taxaCartaoParcelas: { 1: 3 },
                arredondamentoComercial: 'TERMINAR_90',
                variacaoPreOrcamentoMinPct: -10,
                variacaoPreOrcamentoMaxPct: 15,
                valorVisitaTecnica: cobraVisita && valorVisita ? Number(valorVisita.replace(',', '.')) : 0,
                regraDeslocamento: 'FIXO',
                valorDeslocamento: 0,
                validadePadraoDias: 7,
                tamanhoMaximoChapaLarguraMm: 3210,
                tamanhoMaximoChapaAlturaMm: 2250,
                toleranciaPrumoNivelMm: 5,
                regimeTributario: regime,
            });
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['tabela-precos'] });
            queryClient.invalidateQueries({ queryKey: ['parametros-calculo'] });
            onConcluido();
        },
        onError: () => setErro('Não foi possível gerar a tabela. Revise os valores e tente de novo.'),
    });

    function avancar() {
        setErro(null);
        setPasso((p) => Math.min(p + 1, TOTAL_PASSOS + 1));
    }

    function voltar() {
        setErro(null);
        setPasso((p) => Math.max(p - 1, 1));
    }

    const podeAvancarPasso3 = temperado8.trim() !== '';
    const podeAvancarPasso4 = temperado10.trim() !== '';

    return (
        <Modal title="Configuração Expressa" onClose={onClose} largura="md">
            <div className="mb-4 h-1.5 w-full rounded-full bg-slate-100">
                <div
                    className="h-1.5 rounded-full bg-indigo-600 transition-all"
                    style={{ width: `${(Math.min(passo, TOTAL_PASSOS) / TOTAL_PASSOS) * 100}%` }}
                />
            </div>

            {passo === 1 && (
                <Passo titulo="Cidade e UF" descricao="Só pra ter uma referência de mercado da sua região.">
                    <FormField label="Cidade / UF">
                        <input
                            type="text"
                            value={cidadeUf}
                            onChange={(e) => setCidadeUf(e.target.value)}
                            className={inputClass}
                            placeholder="Ex.: Campinas / SP"
                        />
                    </FormField>
                </Passo>
            )}

            {passo === 2 && (
                <Passo titulo="Custo ou preço de venda?" descricao="O sistema funciona dos dois jeitos.">
                    <div className="space-y-2">
                        <OpcaoRadio
                            selecionado={modo === 'VENDA'}
                            onClick={() => setModo('VENDA')}
                            titulo="Prefiro informar meu preço de venda"
                            descricao="O valor que você cobra do cliente."
                        />
                        <OpcaoRadio
                            selecionado={modo === 'CUSTO'}
                            onClick={() => setModo('CUSTO')}
                            titulo="Prefiro informar meu custo"
                            descricao="Quanto você paga ao fornecedor — o sistema sugere o preço de venda."
                        />
                    </div>
                </Passo>
            )}

            {passo === 3 && (
                <Passo titulo="Temperado 8mm incolor" descricao="Valor do m² que você cobra (ou custa, se escolheu custo).">
                    <FormField label="Valor por m² (R$)">
                        <input
                            type="text"
                            inputMode="decimal"
                            autoFocus
                            value={temperado8}
                            onChange={(e) => setTemperado8(e.target.value)}
                            className={inputClass}
                            placeholder="150,00"
                        />
                    </FormField>
                </Passo>
            )}

            {passo === 4 && (
                <Passo titulo="Temperado 10mm incolor" descricao="Valor do m².">
                    <FormField label="Valor por m² (R$)">
                        <input
                            type="text"
                            inputMode="decimal"
                            autoFocus
                            value={temperado10}
                            onChange={(e) => setTemperado10(e.target.value)}
                            className={inputClass}
                            placeholder="180,00"
                        />
                    </FormField>
                </Passo>
            )}

            {passo === 5 && (
                <Passo titulo="Vidro colorido" descricao="Quanto a mais você cobra por fumê, verde ou bronze?">
                    <FormField label="Percentual a mais (%)">
                        <input
                            type="text"
                            inputMode="decimal"
                            value={percentualCor}
                            onChange={(e) => setPercentualCor(e.target.value)}
                            className={inputClass}
                        />
                    </FormField>
                </Passo>
            )}

            {passo === 6 && (
                <Passo
                    titulo="Box frontal instalado"
                    descricao="8mm incolor, tamanho 1,20 × 1,90 — usado só pra calibrar kit e mão de obra. Deixe em branco se não quiser calibrar agora."
                >
                    <FormField label="Valor total instalado (R$)">
                        <input
                            type="text"
                            inputMode="decimal"
                            value={boxInstalado}
                            onChange={(e) => setBoxInstalado(e.target.value)}
                            className={inputClass}
                            placeholder="920,00"
                        />
                    </FormField>
                </Passo>
            )}

            {passo === 7 && (
                <Passo titulo="Espelho 4mm" descricao="Valor do m².">
                    <FormField label="Valor por m² (R$)">
                        <input
                            type="text"
                            inputMode="decimal"
                            value={espelho4}
                            onChange={(e) => setEspelho4(e.target.value)}
                            className={inputClass}
                            placeholder="90,00"
                        />
                    </FormField>
                </Passo>
            )}

            {passo === 8 && (
                <Passo titulo="Visita, regime e margem" descricao="Últimos ajustes pro motor de cálculo.">
                    <div className="space-y-4">
                        <label className="flex items-center gap-2 text-sm text-slate-700">
                            <input
                                type="checkbox"
                                checked={cobraVisita}
                                onChange={(e) => setCobraVisita(e.target.checked)}
                            />
                            Cobro visita técnica
                        </label>

                        {cobraVisita && (
                            <FormField label="Valor da visita (R$)">
                                <input
                                    type="text"
                                    inputMode="decimal"
                                    value={valorVisita}
                                    onChange={(e) => setValorVisita(e.target.value)}
                                    className={inputClass}
                                />
                            </FormField>
                        )}

                        <FormField label="Seu regime tributário">
                            <select
                                value={regime}
                                onChange={(e) => setRegime(e.target.value as RegimeTributario)}
                                className={inputClass}
                            >
                                <option value="MEI">MEI</option>
                                <option value="SIMPLES_NACIONAL">Simples Nacional</option>
                                <option value="OUTRO">Outro</option>
                            </select>
                        </FormField>

                        <FormField label="Margem de lucro desejada (%)">
                            <input
                                type="text"
                                inputMode="decimal"
                                value={margemDesejada}
                                onChange={(e) => setMargemDesejada(e.target.value)}
                                className={inputClass}
                            />
                            <span className="mt-1 block text-xs text-slate-400">
                                Margem é sobre o preço de venda; markup é sobre o custo — o sistema sempre calcula margem.
                            </span>
                        </FormField>
                    </div>
                </Passo>
            )}

            {passo === TOTAL_PASSOS + 1 && (
                <div className="flex flex-col items-center gap-3 py-6 text-center">
                    <CheckCircle2 className="h-10 w-10 text-emerald-500" />
                    <p className="text-base font-semibold text-slate-900">Tudo pronto pra gerar sua tabela</p>
                    <p className="text-sm text-slate-500">
                        Vamos criar os itens de vidro (incolor e colorido), espelho
                        {boxInstalado ? ', o kit calibrado' : ''} e ajustar os parâmetros de cálculo.
                        Você pode revisar e ajustar tudo depois.
                    </p>
                </div>
            )}

            {erro && <p className="mt-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{erro}</p>}

            <div className="mt-6 flex justify-between gap-2">
                <button
                    type="button"
                    onClick={passo === 1 ? onClose : voltar}
                    className="flex items-center gap-1.5 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50"
                >
                    <ArrowLeft className="h-4 w-4" />
                    {passo === 1 ? 'Cancelar' : 'Voltar'}
                </button>

                {passo <= TOTAL_PASSOS ? (
                    <button
                        type="button"
                        onClick={avancar}
                        disabled={(passo === 3 && !podeAvancarPasso3) || (passo === 4 && !podeAvancarPasso4)}
                        className="flex items-center gap-1.5 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        Próxima
                        <ArrowRight className="h-4 w-4" />
                    </button>
                ) : (
                    <button
                        type="button"
                        onClick={() => concluirMutation.mutate()}
                        disabled={concluirMutation.isPending}
                        className="flex items-center gap-1.5 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        {concluirMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        Gerar tabela
                    </button>
                )}
            </div>
        </Modal>
    );
}

function Passo({ titulo, descricao, children }: { titulo: string; descricao: string; children: ReactNode }) {
    return (
        <div className="space-y-4">
            <div>
                <h3 className="text-base font-semibold text-slate-900">{titulo}</h3>
                <p className="mt-0.5 text-sm text-slate-500">{descricao}</p>
            </div>
            {children}
        </div>
    );
}

function OpcaoRadio({
    selecionado,
    onClick,
    titulo,
    descricao,
}: {
    selecionado: boolean;
    onClick: () => void;
    titulo: string;
    descricao: string;
}) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={`w-full rounded-lg border p-3 text-left transition-colors ${
                selecionado ? 'border-indigo-500 bg-indigo-50' : 'border-slate-200 hover:bg-slate-50'
            }`}
        >
            <p className="text-sm font-medium text-slate-900">{titulo}</p>
            <p className="text-xs text-slate-500">{descricao}</p>
        </button>
    );
}
