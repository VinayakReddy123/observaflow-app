import { Component, type ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error: unknown) {
    console.error('Dashboard crashed:', error)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center gap-2 p-8 text-center">
          <p className="text-lg font-medium">Something went wrong rendering this page.</p>
          <p className="text-sm text-muted-foreground">Try refreshing — if it keeps happening, let Vinayak know.</p>
        </div>
      )
    }
    return this.props.children
  }
}
