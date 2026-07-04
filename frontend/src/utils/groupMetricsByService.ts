import type { ProcessedMetric } from "@/types/service";


export function groupMetricsByService(metrics: ProcessedMetric[]):Record<string,ProcessedMetric[]> {
    const groupedMetrics = metrics.reduce<Record<string, ProcessedMetric[]>>((acc,cur)=>{
        const serviceName = cur.serviceId || 'unknown';
        if(!acc[serviceName]){
            acc[serviceName] = [];
        }
        acc[serviceName].push(cur);
        return acc;
    },{});

    Object.values(groupedMetrics).forEach((serviceMetrics:ProcessedMetric[])=>{
        serviceMetrics.sort((a,b)=> a.windowStart - b.windowStart);
    });
    return groupedMetrics;
}