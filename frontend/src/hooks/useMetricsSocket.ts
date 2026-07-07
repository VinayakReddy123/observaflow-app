import { useAppStore } from "@/store/useAppStore";
import { useEffect, useRef } from "react";



const WS_BASE_URL = 'ws://localhost:8086/ws/metrics';
const BASE_RECONNECT_DELAY = 1000; // 1 second
const MAX_RECONNECT_DELAY = 30000; // 30 seconds


export function useMetricsSocket(){
    const tenantId = useAppStore((s)=>s.user?.tenantId);
    const addServiceMetric = useAppStore((s)=>s.addServiceMetric);
    
    const socketRef = useRef<WebSocket | null>(null);
    const reConnectAttemptRef = useRef(0);
    const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(()=>{
        if(!tenantId) return;
        let isUnmounted = false;

        function connect(){
            socketRef.current = new WebSocket(`${WS_BASE_URL}?tenantId=${tenantId}`);
            
            socketRef.current.onopen = () => {
                console.log('WebSocket connected');
                reConnectAttemptRef.current = 0; // Reset the reconnection attempts on successful connection
            }

            socketRef.current.onmessage = (event) => {
                const data = JSON.parse(event.data);
                addServiceMetric(data);
            }

            socketRef.current.onclose = () => {
                if(isUnmounted) return;
                console.log('WebSocket disconnected'); 
                // Attempt to reconnect with exponential backoff
                const delay = Math.min(BASE_RECONNECT_DELAY * Math.pow(2, reConnectAttemptRef.current), MAX_RECONNECT_DELAY);
                reConnectAttemptRef.current += 1;
                reconnectTimeoutRef.current = setTimeout(connect, delay);
            }
        }

        connect();

        return ()=>{
            isUnmounted = true;
            if(socketRef.current){
                socketRef.current.close();
            }
            if(reconnectTimeoutRef.current){
                clearTimeout(reconnectTimeoutRef.current);
            }
        }
    },[tenantId, addServiceMetric]);
}