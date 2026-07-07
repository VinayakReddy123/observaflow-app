import { useState } from "react";


const STORAGE_KEY = 'observaflow-recent-traces';
const MAX_RECENT = 5;

export function useRecentTraces() {
    const [recentTraces, setRecentTraces] = useState<string[]>(()=>{
        const storedTraces = localStorage.getItem(STORAGE_KEY);
        return storedTraces ? JSON.parse(storedTraces) : [];
    })

    const addRecentTrace = (traceId: string) => {
        setRecentTraces(prev => {
            const updated = [traceId, ...prev.filter(id => id !== traceId)].slice(0, MAX_RECENT);
            localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
            return updated;
        });
    }

    return { recentTraces, addRecentTrace };
}