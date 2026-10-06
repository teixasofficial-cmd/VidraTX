import { useState, type FormEvent } from 'react';
import { Modal } from './Modal';
import { FormField, inputClass } from './FormField';
import type { MotivoPerda } from '../types';

const opcoes: { valor: MotivoPerda; rotulo: string }[] = [
    { valor: 'PRECO_ALTO', rotulo: 'Preço alto' },
    { valor: 'ESCOLHEU_CONCORRENTE', rotulo: 'Escolheu concorrente' },
    { valor: 'DESISTIU_DO_SERVICO', rotulo: 'Desistiu do serviço' },
    { valor: 'SEM_RETORNO_DO_CLIENTE', rotulo: 'Sem retorno do cliente' },
    { valor: 'OUTRO', rotulo: 'Outro' },
];

interface MotivoPerdaModalProps {
    onClose: () => void;
    onConfirmar: (motivo: MotivoPerda, motivoOutro?: string) => void;
    enviando?: boolean;
}

export function MotivoPerdaModal({ onClose, onConfirmar, enviando }: MotivoPerdaModalProps) {

    const [motivo, setMotivo] = useState<MotivoPerda>('PRECO_ALTO');
    const [motivoOutro, setMotivoOutro] = useState('');

    function aoSubmeter(evento: FormEvent) {
        evento.preventDefault();
        onConfirmar(motivo, motivo === 'OUTRO' ? motivoOutro.trim() : undefined);
    }

    return (
        <Modal title="Por que este orçamento foi perdido?" onClose={onClose} largura="sm">
            <form onSubmit={aoSubmeter} className="space-y-4">
                <FormField label="Motivo">
                    <select
                        value={motivo}
                        onChange={(e) => setMotivo(e.target.value as MotivoPerda)}
                        className={inputClass}
                    >
                        {opcoes.map((opcao) => (
                            <option key={opcao.valor} value={opcao.valor}>
                                {opcao.rotulo}
                            </option>
                        ))}
                    </select>
                </FormField>

                {motivo === 'OUTRO' && (
                    <FormField label="Descreva o motivo">
                        <input
                            type="text"
                            value={motivoOutro}
                            onChange={(e) => setMotivoOutro(e.target.value)}
                            className={inputClass}
                            maxLength={255}
                            required
                        />
                    </FormField>
                )}

                <p className="text-xs text-slate-400">
                    Usado só pra gerar o relatório de perdas — não é enviado ao cliente.
                </p>

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
                        disabled={enviando}
                        className="rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        Marcar como perdido
                    </button>
                </div>
            </form>
        </Modal>
    );
}
