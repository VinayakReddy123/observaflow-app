import { useAppStore } from "@/store/useAppStore"
import { Navigate, Outlet } from "react-router-dom";

export default function  ProtectedRoute(){
    const isAuthenticated = useAppStore(s => s.isAuthenticated);
    if(isAuthenticated){
        return <Outlet />
    }
    return <Navigate to={'/login'} replace />
}