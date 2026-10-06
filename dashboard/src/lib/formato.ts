export function formatarDataHora(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short',
        timeStyle: 'short',
    }).format(new Date(iso));
}

export function formatarHora(iso: string): string {
    return new Intl.DateTimeFormat('pt-BR', { timeStyle: 'short' }).format(new Date(iso));
}

export function formatarMoeda(valor: number | null | undefined): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor ?? 0);
}

export function dataIsoLocal(data: Date): string {
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
}

export function paraInputDataHora(iso: string | null | undefined): string {
    return iso ? iso.slice(0, 16) : '';
}

export function lerValorDecimal(texto: string): number | null {

    let limpo = texto.replace(/R\$/g, '').replace(/\s/g, '');

    if (limpo.includes(',')) {
        limpo = limpo.replace(/\./g, '').replace(',', '.');
    }

    if (limpo === '') {
        return null;
    }

    const valor = Number(limpo);
    return Number.isFinite(valor) ? valor : null;
}

export function formatarDecimal(valor: number): string {
    return valor.toFixed(2).replace('.', ',');
}

export function formatarTelefone(telefone: string | null | undefined): string {

    if (!telefone) {
        return '';
    }

    let digitos = telefone.replace(/\D/g, '');

    if (digitos.startsWith('55') && (digitos.length === 12 || digitos.length === 13)) {
        digitos = digitos.slice(2);
    }

    if (digitos.length === 11) {
        return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7)}`;
    }

    if (digitos.length === 10) {
        return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 6)}-${digitos.slice(6)}`;
    }

    return telefone;
}
