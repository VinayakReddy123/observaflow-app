import axios from "axios";
import { useAppStore } from "@/store/useAppStore";

export const axiosInstance = axios.create({});

axiosInstance.interceptors.request.use(
    (config)=>{
       const token = useAppStore.getState().token;
       if(token){
           config.headers.Authorization = `Bearer ` + token;
       }
       return config;
    },
    (error)=>{
        return Promise.reject(error);
    }
);

axiosInstance.interceptors.response.use(
    (response) => {
        return response;
    },
    (error)=>{
        if(error.response){
            switch(error.response.status){
                case 401:
                  console.log("Token Expired",error);
                  useAppStore.getState().logout();
                  globalThis.location.href = '/login';
                  break;
                case 403:
                  console.log("Forbidden to access resource",error);
                  break;
            }
        }
        return Promise.reject(error);
    }
)