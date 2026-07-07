import { axiosInstance } from "@/api/axiosInstance";
import { useQuery } from "@tanstack/react-query";


export function useTraceQuery(traceId: string) {
    return useQuery({
        queryKey: ['trace', traceId],
        queryFn: async () =>{
            const response = await axiosInstance.get('/api/v1/query/traces', { params : {traceId} } );
            return response.data;
        },
        enabled: traceId?.length > 0
    })
}