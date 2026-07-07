import {z} from 'zod';

export const alertRuleSchema = z.object({
    serviceId: z.string().min(1,'Service ID is required'),
    webhookUrl: z.string().url('Must be valid Url'),
    threshold: z.coerce.number().min(1, 'Threshold must be greater than 0'),
    metricType: z.enum(['P50','P95','P99']),
    operator: z.enum(['GT','LT','GTE','LTE']),
    enabled: z.boolean(),
})

export type AlertRuleFormValues = z.infer<typeof alertRuleSchema>;