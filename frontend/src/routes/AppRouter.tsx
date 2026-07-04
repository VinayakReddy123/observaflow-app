import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "@/pages/auth/LoginPage";
import RegisterPage from "@/pages/auth/RegisterPage";
import ProtectedRoute from "./ProtectedRoute";
import AppLayout from "@/components/layout/AppLayout";
import DashboardPage from "@/pages/dashboard/DashboardPage";
import LogsPage from "@/pages/logs/LogsPage";
import TracesPage from "@/pages/traces/TracesPage";
import ServicesPage from "@/pages/services/ServicesPage";
import AlertsPage from "@/pages/alerts/AlertsPage";
import SettingsPage from "@/pages/settings/SettingsPage";



export const AppRouter = () => (
    <BrowserRouter>
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>         
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/services" element={<ServicesPage />} />
                    <Route path="/logs" element={<LogsPage />} />
                    <Route path="/traces" element={<TracesPage />} />
                    <Route path="/alerts" element={<AlertsPage />} />
                    <Route path="/settings" element={<SettingsPage />} />
                    <Route path="*" element={<Navigate to="/dashboard" />} />
                </Route>
            </Route>
        </Routes>
    </BrowserRouter>
)