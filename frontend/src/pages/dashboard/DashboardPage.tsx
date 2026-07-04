import { useServicesQuery } from '@/hooks/useServicesQuery'
import { groupMetricsByService } from '@/utils/groupMetricsByService'
import { MetricCard } from '@/components/dashboard/MetricCard'
import { ErrorBoundary } from '@/components/ErrorBoundary'
import { Skeleton } from '@/components/ui/skeleton'

function DashboardContent() {
  const { data, isLoading, isError, error } = useServicesQuery();

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <Skeleton key={i} className="h-56 w-full rounded-lg" />
        ))}
      </div>
    )
  }

  if (isError) {
    return (
      <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
        Could not load service metrics: {(error as Error).message}
      </div>
    )
  }

  const grouped = groupMetricsByService(data ?? [])
  const serviceIds = Object.keys(grouped)

  if (serviceIds.length === 0) {
    return <p className="text-muted-foreground">No service metrics reported yet.</p>
  }

  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      {serviceIds.map((serviceId) => (
        <MetricCard key={serviceId} serviceId={serviceId} history={grouped[serviceId]} />
      ))}
    </div>
  )
}

export default function DashboardPage() {
  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold">Dashboard</h1>
      <ErrorBoundary>
        <DashboardContent />
      </ErrorBoundary>
    </div>
  )
}
