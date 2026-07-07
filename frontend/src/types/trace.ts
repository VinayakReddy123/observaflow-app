export interface TraceSpan {
  id: string
  serviceId: string
  tenantId: string
  timestamp: number
  payload: {
    traceId: string
    spanId: string
    duration: number
  }
}
