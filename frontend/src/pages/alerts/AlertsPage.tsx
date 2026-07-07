import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { alertRuleSchema, type AlertRuleFormValues } from './alertRuleSchema'

type AlertRuleFormInput = z.input<typeof alertRuleSchema>
import { useAlertRulesQuery, useCreateAlertRule, useDeleteAlertRule } from '@/hooks/useAlertRules'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'

export default function AlertsPage() {
  const { data: rules, isLoading } = useAlertRulesQuery()
  const createRule = useCreateAlertRule()
  const deleteRule = useDeleteAlertRule()

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    reset,
    formState: { errors },
  } = useForm<AlertRuleFormInput, any, AlertRuleFormValues>({
    resolver: zodResolver(alertRuleSchema),
    defaultValues: { metricType: 'P99', operator: 'GT', enabled: true },
  })

  const onSubmit = (values: AlertRuleFormValues) => {
    createRule.mutate(values, { onSuccess: () => reset() })
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold">Alert Rules</h1>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">New Rule</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <div>
              <Input placeholder="Service ID" {...register('serviceId')} />
              {errors.serviceId && <p className="text-xs text-destructive">{errors.serviceId.message}</p>}
            </div>
            <div>
              <Input placeholder="Webhook URL" {...register('webhookUrl')} />
              {errors.webhookUrl && <p className="text-xs text-destructive">{errors.webhookUrl.message}</p>}
            </div>
            <div>
              <Input type="number" placeholder="Threshold" {...register('threshold')} />
              {errors.threshold && <p className="text-xs text-destructive">{errors.threshold.message}</p>}
            </div>
            <Select value={watch('metricType')} onValueChange={(v) => setValue('metricType', v as AlertRuleFormValues['metricType'])}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="P50">P50</SelectItem>
                <SelectItem value="P95">P95</SelectItem>
                <SelectItem value="P99">P99</SelectItem>
              </SelectContent>
            </Select>
            <Select value={watch('operator')} onValueChange={(v) => setValue('operator', v as AlertRuleFormValues['operator'])}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="GT">&gt;</SelectItem>
                <SelectItem value="LT">&lt;</SelectItem>
                <SelectItem value="GTE">&gt;=</SelectItem>
                <SelectItem value="LTE">&lt;=</SelectItem>
              </SelectContent>
            </Select>
            <Button type="submit" disabled={createRule.isPending}>
              {createRule.isPending ? 'Creating...' : 'Create Rule'}
            </Button>
          </form>
        </CardContent>
      </Card>

      {isLoading && <Skeleton className="h-32 w-full" />}

      <div className="flex flex-col gap-2">
        {rules?.map((rule) => (
          <Card key={rule.id}>
            <CardContent className="flex items-center justify-between py-3">
              <span className="text-sm">
                {rule.serviceId}: {rule.metricType} {rule.operator} {rule.threshold}
              </span>
              <Button variant="outline" size="sm" onClick={() => deleteRule.mutate(rule.id)}>
                Delete
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
