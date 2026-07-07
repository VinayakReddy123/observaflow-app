import { NavLink, Outlet } from 'react-router-dom'
import { useAppStore } from '@/store/useAppStore'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { useMetricsSocket } from '@/hooks/useMetricsSocket'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/services', label: 'Services' },
  { to: '/logs', label: 'Logs' },
  { to: '/traces', label: 'Traces' },
  { to: '/alerts', label: 'Alerts' },
  { to: '/settings', label: 'Settings' },
]

export default function AppLayout() {
  const logout = useAppStore((s) => s.logout)
  useMetricsSocket() // Initialize the WebSocket connection for metrics
  
  return (
    <div className="flex min-h-screen bg-background text-foreground">
      <aside className="flex w-56 flex-col border-r border-border p-4">
        <div className="mb-4 text-lg font-bold">ObservaFlow</div>
        <Separator className="mb-4" />
        <nav className="flex flex-1 flex-col gap-1">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `rounded-md px-3 py-2 text-sm ${
                  isActive ? 'bg-accent text-accent-foreground' : 'text-muted-foreground hover:bg-accent/50'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <Button variant="outline" onClick={logout}>
          Log out
        </Button>
      </aside>
      <main className="flex-1 p-8">
        <Outlet />
      </main>
    </div>
  )
}
