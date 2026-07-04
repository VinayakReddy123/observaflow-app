import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { type  ProcessedMetric } from '@/types/service'
import { deriveStatus } from '@/utils/deriveStatus'

const STATUS_STYLES = {
  healthy: 'bg-emerald-500/15 text-emerald-500',
  degraded: 'bg-amber-500/15 text-amber-500',
  critical: 'bg-red-500/15 text-red-500',
}

interface MetricCardProps {
  serviceId: string
  history: ProcessedMetric[]
}

export function MetricCard({ serviceId, history }: MetricCardProps) {
  const latest = history[history.length - 1];
  const status = deriveStatus(latest.p99);

  const chartData = history.map((m) => ({
    time: new Date(m.windowStart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    p50: m.p50,
    p95: m.p95,
    p99: m.p99,
  }))

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-base">{serviceId}</CardTitle>
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[status]}`}>{status}</span>
      </CardHeader>
      <CardContent>
        <div className="mb-3 flex gap-4 text-sm text-muted-foreground">
          <span>p50: {latest.p50.toFixed(0)}ms</span>
          <span>p95: {latest.p95.toFixed(0)}ms</span>
          <span>p99: {latest.p99.toFixed(0)}ms</span>
        </div>
        <ResponsiveContainer width="100%" height={140}>
          <LineChart data={chartData}>
            <XAxis dataKey="time" tick={{ fontSize: 11 }} />
            <YAxis tick={{ fontSize: 11 }} />
            <Tooltip />
            <Line type="monotone" dataKey="p50" stroke="var(--chart-1)" dot={false} strokeWidth={2} />
            <Line type="monotone" dataKey="p95" stroke="var(--chart-2)" dot={false} strokeWidth={2} />
            <Line type="monotone" dataKey="p99" stroke="var(--chart-3)" dot={false} strokeWidth={2} />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
