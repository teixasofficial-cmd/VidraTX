import { describe, expect, it } from 'vitest';
import { formatarMoeda, formatarTelefone, lerValorDecimal } from './formato';
import { expiracaoDoToken } from './apiClient';

describe('formatarTelefone', () => {
    it('formata celular e fixo com ou sem o 55', () => {
        expect(formatarTelefone('5511987654321')).toBe('(11) 98765-4321');
        expect(formatarTelefone('11987654321')).toBe('(11) 98765-4321');
        expect(formatarTelefone('551133334444')).toBe('(11) 3333-4444');
    });

    it('deixa como veio o que não é telefone brasileiro', () => {
        expect(formatarTelefone('anon-12')).toBe('anon-12');
        expect(formatarTelefone(null)).toBe('');
    });
});

describe('lerValorDecimal', () => {
    it('lê o padrão brasileiro', () => {
        expect(lerValorDecimal('1.234,56')).toBe(1234.56);
        expect(lerValorDecimal('R$ 80')).toBe(80);
        expect(lerValorDecimal('abc')).toBeNull();
    });
});

describe('formatarMoeda', () => {
    it('usa real com vírgula', () => {
        expect(formatarMoeda(1559.9).replace(/\s/g, ' ')).toBe('R$ 1.559,90');
    });
});

describe('expiracaoDoToken', () => {
    it('lê o exp do JWT em milissegundos', () => {
        const payload = btoa(JSON.stringify({ exp: 1_900_000_000 })).replace(/=+$/, '');
        expect(expiracaoDoToken(`cabecalho.${payload}.assinatura`)).toBe(1_900_000_000_000);
    });

    it('devolve null para token ilegível', () => {
        expect(expiracaoDoToken('lixo')).toBeNull();
        expect(expiracaoDoToken(null)).toBeNull();
    });
});
