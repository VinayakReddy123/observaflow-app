import type { AlertRule } from "@/types/alert";
import type { StateCreator } from "zustand";
import type { AppState } from "./useAppStore";

export interface AlertSlice {
    alerts: AlertRule[]
    setAlerts: (alerts: AlertRule[]) => void
}

export const createAlertSlice: StateCreator<AppState,[],[],AlertSlice> = (set)=>({
    alerts: [],
    setAlerts: (alerts)=>set({alerts})
})