import { axiosInstance } from "@/api/axiosInstance"
import { useQuery } from "@tanstack/react-query"


export function useLogsQuery(level: string | null, search: string | null){

    const data = useQuery({
        queryKey: ['logs',level,search],
        queryFn : async () => {
            const response = await axiosInstance.get('/api/v1/query/logs',{params:{level,search}});
            return response.data
        }
    });
    return data;
}

