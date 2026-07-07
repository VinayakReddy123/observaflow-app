import { useState } from 'react'
import { useGenerateApiKey } from '@/hooks/useGenerateApiKey'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export default function SettingsPage() {
  const [name, setName] = useState('')
  const [copied, setCopied] = useState(false)
  const generateKey = useGenerateApiKey()

  const handleCopy = async () => {
    if (generateKey.data) {
      await navigator.clipboard.writeText(generateKey.data)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold">Settings</h1>
      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle className="text-base">API Keys</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <p className="text-sm text-muted-foreground">
            Generate a key for a service to send telemetry with. It's shown once — copy it now.
          </p>
          <div className="flex gap-2">
            <Input placeholder="Key name (e.g. ingest-simulator)" value={name} onChange={(e) => setName(e.target.value)} />
            <Button onClick={() => generateKey.mutate(name)} disabled={generateKey.isPending || !name}>
              Generate New Key
            </Button>
          </div>
          {generateKey.data && (
            <div className="flex items-center gap-2 rounded-md border border-border bg-muted p-2 font-mono text-sm">
              <span className="flex-1 truncate">{generateKey.data}</span>
              <Button size="sm" variant="outline" onClick={handleCopy}>
                {copied ? 'Copied!' : 'Copy'}
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
