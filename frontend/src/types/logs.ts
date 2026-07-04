
export interface TelemetryLog {
  id: string
  serviceId: string
  tenantId: string
  timestamp: number
  payload: {
    level?: 'ERROR' | 'WARN' | 'INFO'
    message?: string
    [key: string]: unknown
  }
}
