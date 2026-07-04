import { create } from "zustand";
import { persist } from "zustand/middleware";
import { createAuthSlice, type AuthSlice } from "./authSlice";
import { createAlertSlice, type AlertSlice } from "./alertSlice";
import { createServiceSlice, type ServiceSlice } from "./serviceSlice";


export type AppState = AuthSlice & AlertSlice & ServiceSlice;

export const useAppStore = create<AppState>()(
    persist(
        (...a) => ({
            ...createAuthSlice(...a),
            ...createAlertSlice(...a),
            ...createServiceSlice(...a),
        }),
        {
            name: 'observaflow-auth',
        }
    )
)