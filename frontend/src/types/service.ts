export interface ProcessedMetric{
    id: string
    serviceId: string
    tenantId: string
    p50: number
    p95: number
    p99: number
    eventCount: number
    windowStart: number
    windowEnd: number
}