import { axiosInstance } from "@/api/axiosInstance";
import { useAppStore } from "@/store/useAppStore";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";




export function useServicesQuery(){
    const setServices = useAppStore(s => s.setServices);

    const query = useQuery({
        queryKey: ['services'],
        queryFn: async () => {
            const to = Date.now();
            const from = to - 60*60*1000;
            const response = await axiosInstance.get(`/api/v1/query/metrics`, { params: { from, to} });
            return response.data;
        }
    });

    useEffect(() => {
        if(query.data){
            setServices(query.data);
        }       
    }, [query.data]);

    return query;
}