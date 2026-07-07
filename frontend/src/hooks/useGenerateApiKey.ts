import { axiosInstance } from "@/api/axiosInstance";
import { useAppStore } from "@/store/useAppStore";
import { useMutation } from "@tanstack/react-query";


export function useGenerateApiKey() {
    return useMutation({
        mutationFn: async (name: string) => {
            const tenantId = useAppStore.getState().user?.tenantId;
            if (!tenantId) {
                throw new Error("Tenant ID is not available");
            }
            const response = await axiosInstance.post('/auth/generate-api-key', { name , tenantId });
            return response.data;
        }
    })
}