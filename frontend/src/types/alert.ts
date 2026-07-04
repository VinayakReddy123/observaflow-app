
export type MetricType = 'P50' | 'P95' | 'P99'
export type Operator = 'GT' | 'LT' | 'GTE' | 'LTE'


export interface AlertRule{
    id: string
    tenantId: string
    serviceId: string
    webhookUrl: string
    threshold: number
    enabled: boolean
    metricType: MetricType
    operator: Operator
}