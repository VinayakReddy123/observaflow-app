import type { ProcessedMetric } from "@/types/service";
import type { StateCreator } from "zustand";
import type { AppState } from "./useAppStore";

export interface ServiceSlice {
    services: ProcessedMetric[]
    setServices: (services: ProcessedMetric[]) => void
}

export const createServiceSlice: StateCreator<AppState,[],[],ServiceSlice> = (set) =>({
   services : [],
   setServices: (services) => set({services})
})