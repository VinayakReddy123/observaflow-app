import type { LoginRequest, RegisterRequest, User } from "@/types/auth";
import type { StateCreator } from "zustand";
import type { AppState } from "./useAppStore";
import { axiosInstance } from "@/api/axiosInstance";

export interface AuthSlice {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    login: (data: LoginRequest) => Promise<void>;
    register: (data:RegisterRequest) => Promise<void>
    logout: ()=> void
}

export const createAuthSlice: StateCreator<AppState,[],[],AuthSlice> = (set)=>({
    user: null,
    token: null,
    isAuthenticated: false,
    login: async(data)=>{
        try{
            const response = await axiosInstance.post('/auth/login',data);  
            const tenantId = JSON.parse(atob(response.data.split(".")[1])).tenantId;
            set({
                user: {
                    email: data.email,
                    tenantId
                },
                token: response.data,
                isAuthenticated: true
            })
        }catch(error){
            console.log("Error is",error);
            throw error;
        }
    },
    register: async(data)=>{
       try{
            const response = await axiosInstance.post('/auth/register',data);  
            set({
                user: {
                    email: data.email,
                    tenantId: data.tenantId,
                },
                token: response.data,
                isAuthenticated: true
            })
        }catch(error){
            console.log("Error is",error);
            throw error;
        }
    },
    logout: ()=>{
       set({
          user: null,
          token: null,
          isAuthenticated: false
       })
    }
})
