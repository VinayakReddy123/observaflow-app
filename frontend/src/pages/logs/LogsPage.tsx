import { useMemo, useRef, useState } from 'react'
import { useVirtualizer } from '@tanstack/react-virtual'
import { useLogsQuery } from '@/hooks/useLogsQuery'
import { useDebouncedValue } from '@/hooks/useDebouncedValue'
import { filterLogsClientSide } from '@/utils/filterLogsClientSide'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'

const LEVEL_STYLES: Record<string, string> = {
  ERROR: 'text-red-500',
  WARN: 'text-amber-500',
  INFO: 'text-muted-foreground',
}

export default function LogsPage() {
  const [level, setLevel] = useState<string>('ALL')
  const [searchInput, setSearchInput] = useState('')
  const [serviceId, setServiceId] = useState('')
  const [fromInput, setFromInput] = useState('')
  const [toInput, setToInput] = useState('')
 
  const debouncedSearch = useDebouncedValue(searchInput, 400)

  const { data, isLoading, isError } = useLogsQuery(level === 'ALL' ? null : level, debouncedSearch)

  const filteredLogs = useMemo(
    () => filterLogsClientSide(
      data ?? [],
      { 
        serviceId: serviceId || undefined ,
        from: fromInput ? new Date(fromInput).getTime() : undefined,
        to: toInput ? new Date(toInput).getTime() : undefined
      }
    ),
    [data, serviceId, fromInput, toInput]
  )

  const parentRef = useRef<HTMLDivElement>(null)
  const virtualizer = useVirtualizer({
    count: filteredLogs.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 32,
    overscan: 10,
  })

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold">Log Explorer</h1>

      <div className="mb-4 flex flex-wrap gap-3">
        <Select value={level} onValueChange={setLevel}>
          <SelectTrigger className="w-32">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All levels</SelectItem>
            <SelectItem value="ERROR">Error</SelectItem>
            <SelectItem value="WARN">Warn</SelectItem>
            <SelectItem value="INFO">Info</SelectItem>
          </SelectContent>
        </Select>
        <Input
          placeholder="Filter by serviceId"
          value={serviceId}
          onChange={(e) => setServiceId(e.target.value)}
          className="w-48"
        />

        <Input
          type="datetime-local"
          value={fromInput}
          onChange={(e) => setFromInput(e.target.value)}
          className="w-56"
        />
        <Input
          type="datetime-local"
          value={toInput}
          onChange={(e) => setToInput(e.target.value)}
          className="w-56"
        />

        <Input
          placeholder="Search log messages..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className="flex-1"
        />
      </div>

      {isLoading && (
        <div className="flex flex-col gap-1">
          {Array.from({ length: 10 }).map((_, i) => (
            <Skeleton key={i} className="h-8 w-full" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          Could not load logs.
        </div>
      )}

      {!isLoading && !isError && (
        <div ref={parentRef} className="h-[600px] overflow-auto rounded-lg border border-border font-mono text-sm">
          <div style={{ height: virtualizer.getTotalSize(), position: 'relative' }}>
            {virtualizer.getVirtualItems().map((row) => {
              const log = filteredLogs[row.index]
              return (
                <div
                  key={log.id}
                  className="absolute left-0 top-0 flex w-full gap-3 border-b border-border/50 px-3"
                  style={{ height: row.size, transform: `translateY(${row.start}px)` }}
                >
                  <span className="text-muted-foreground">{new Date(log.timestamp).toLocaleTimeString()}</span>
                  <span className={`w-14 font-semibold ${LEVEL_STYLES[log.payload.level ?? 'INFO']}`}>
                    {log.payload.level ?? 'INFO'}
                  </span>
                  <span className="text-muted-foreground">{log.serviceId}</span>
                  <span className="truncate">{log.payload.message ?? '(no message)'}</span>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
