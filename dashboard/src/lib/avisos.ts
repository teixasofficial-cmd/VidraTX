import { useEffect, useRef } from 'react';

export function useAvisoClientesEsperando(pendentes: number | undefined): void {

    const anterior = useRef<number | undefined>(undefined);

    useEffect(() => {

        if (pendentes === undefined) {
            return;
        }

        const tituloBase = document.title.replace(/^\(\d+\) /, '');
        document.title = pendentes > 0 ? `(${pendentes}) ${tituloBase}` : tituloBase;

        if (anterior.current !== undefined && pendentes > anterior.current) {
            tocarBipe();
            notificar(pendentes);
        }

        anterior.current = pendentes;
    }, [pendentes]);
}

export function podePedirNotificacao(): boolean {
    return typeof Notification !== 'undefined' && Notification.permission === 'default';
}

export function pedirNotificacao(): Promise<NotificationPermission | undefined> {
    return typeof Notification !== 'undefined' ? Notification.requestPermission() : Promise.resolve(undefined);
}

function notificar(pendentes: number): void {

    if (typeof Notification === 'undefined' || Notification.permission !== 'granted') {
        return;
    }

    new Notification('Cliente esperando resposta no WhatsApp', {
        body: pendentes === 1 ? '1 conversa esperando uma pessoa.' : `${pendentes} conversas esperando uma pessoa.`,
        tag: 'vidratx-atendimentos',
    });
}

function tocarBipe(): void {

    try {
        const contexto = new AudioContext();
        const oscilador = contexto.createOscillator();
        const volume = contexto.createGain();

        oscilador.frequency.value = 880;
        volume.gain.value = 0.08;
        oscilador.connect(volume);
        volume.connect(contexto.destination);
        oscilador.start();
        oscilador.stop(contexto.currentTime + 0.25);
        oscilador.onended = () => void contexto.close();
    } catch {
        return;
    }
}
