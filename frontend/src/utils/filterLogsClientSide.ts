import type { TelemetryLog } from "@/types/logs";

interface ClientFilters {
    serviceId? : string;
    from? : number;
    to? : number;
}

export function filterLogsClientSide(logs: TelemetryLog[], filters: ClientFilters): TelemetryLog[] {
    return logs.filter(log =>{
        if(filters.serviceId && log.serviceId !== filters.serviceId) {
            return false;
        }
        if(filters.from != undefined && log.timestamp < filters.from) {
            return false;
        }
        if(filters.to != undefined && log.timestamp > filters.to) {
            return false;
        }
        return true;
    })
}