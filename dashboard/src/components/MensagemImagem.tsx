import { useEffect, useState } from 'react';
import { ImageOff } from 'lucide-react';
import { api } from '../lib/api';

interface MensagemImagemProps {
    mensagemId: number;
    legenda: string | null;
}

export function MensagemImagem({ mensagemId, legenda }: MensagemImagemProps) {

    const [url, setUrl] = useState<string | null>(null);
    const [erro, setErro] = useState(false);

    useEffect(() => {

        let objectUrl: string | null = null;
        let cancelado = false;

        setUrl(null);
        setErro(false);

        api.mensagemMidia(mensagemId)
            .then((blob) => {

                if (cancelado) {
                    return;
                }

                objectUrl = URL.createObjectURL(blob);
                setUrl(objectUrl);
            })
            .catch(() => {

                if (!cancelado) {
                    setErro(true);
                }
            });

        return () => {

            cancelado = true;

            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    }, [mensagemId]);

    if (erro) {
        return (
            <div className="flex h-32 w-48 items-center justify-center rounded-lg bg-slate-100 text-slate-400">
                <ImageOff className="h-6 w-6" />
            </div>
        );
    }

    if (!url) {
        return <div className="h-32 w-48 animate-pulse rounded-lg bg-slate-200" />;
    }

    return (
        <a href={url} target="_blank" rel="noreferrer">
            <img
                src={url}
                alt={legenda ?? 'Foto enviada pelo cliente'}
                className="max-h-64 max-w-full rounded-lg object-cover"
            />
        </a>
    );
}
