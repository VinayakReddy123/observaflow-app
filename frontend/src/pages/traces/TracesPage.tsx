import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useTraceQuery } from '@/hooks/useTraceQuery'
import { useRecentTraces } from '@/hooks/useRecentTraces'
import { computeWaterfallLayout } from '@/utils/computeWaterfallLayout'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'

export default function TracesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const traceIdFromUrl = searchParams.get('traceId') ?? ''
  const [traceIdInput, setTraceIdInput] = useState(traceIdFromUrl)
  const { recentTraces, addRecentTrace } = useRecentTraces()

  const { data, isLoading, isError } = useTraceQuery(traceIdFromUrl)
  const waterfall = computeWaterfallLayout(data ?? [])

  const viewTrace = (id: string) => {
    setSearchParams({ traceId: id })
    if (id) addRecentTrace(id)
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold">Trace Viewer</h1>

      <div className="mb-4 flex gap-3">
        <Input
          placeholder="Paste a traceId..."
          value={traceIdInput}
          onChange={(e) => setTraceIdInput(e.target.value)}
          className="max-w-md"
        />
        <Button onClick={() => viewTrace(traceIdInput)}>View Trace</Button>
      </div>

      {recentTraces.length > 0 && (
        <div className="mb-4 flex flex-wrap items-center gap-2 text-sm">
          <span className="text-muted-foreground">Recent:</span>
          {recentTraces.map((id) => (
            <button
              key={id}
              onClick={() => {
                setTraceIdInput(id)
                viewTrace(id)
              }}
              className="rounded-full border border-border px-2 py-0.5 font-mono text-xs hover:bg-accent"
            >
              {id}
            </button>
          ))}
        </div>
      )}

      {isLoading && <Skeleton className="h-40 w-full" />}
      {isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          Could not load trace.
        </div>
      )}

      {!isLoading && !isError && traceIdFromUrl && waterfall.length === 0 && (
        <p className="text-muted-foreground">No spans found for that traceId.</p>
      )}

      {waterfall.length > 0 && (
        <div className="flex flex-col gap-2 rounded-lg border border-border p-4">
          {waterfall.map((span) => (
            <div key={span.payload.spanId} className="flex items-center gap-3">
              <span className="w-32 shrink-0 truncate text-sm text-muted-foreground">{span.serviceId}</span>
              <div className="relative h-6 flex-1 rounded bg-muted">
                <div
                  className="absolute h-full rounded bg-primary"
                  style={{ left: `${span.leftPercent}%`, width: `${Math.max(span.widthPercent, 1)}%` }}
                />
              </div>
              <span className="w-16 shrink-0 text-right text-xs text-muted-foreground">
                {span.payload.duration}ms
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
