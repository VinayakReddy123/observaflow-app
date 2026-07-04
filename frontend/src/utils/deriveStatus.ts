
export type ServiceStatus = 'healthy' | 'degraded' | 'critical' ;

export function deriveStatus(p99: number): ServiceStatus {
    if (p99 < 200) {
        return 'healthy';
    } else if (p99 < 500) {
        return 'degraded';
    } 
    return 'critical'; 
}