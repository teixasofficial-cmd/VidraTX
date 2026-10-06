import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';
import { AvisoWhatsappDesconectado } from './AvisoWhatsappDesconectado';

export function AppLayout() {

    const [menuAberto, setMenuAberto] = useState(false);

    return (
        <div className="min-h-screen bg-slate-50">
            <Sidebar open={menuAberto} onClose={() => setMenuAberto(false)} />

            <div className="flex flex-col lg:ml-64">
                <Topbar onMenuClick={() => setMenuAberto(true)} />
                <AvisoWhatsappDesconectado />
                <main className="flex-1 p-4 lg:p-8">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}
