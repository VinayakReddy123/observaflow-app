import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { axiosInstance } from '@/api/axiosInstance'
import { type AlertRule } from '@/types/alert'
import { type AlertRuleFormValues } from '@/pages/alerts/alertRuleSchema'

export function useAlertRulesQuery() {
  return useQuery({
    queryKey: ['alertRules'],
    queryFn: async () => {
        const response = await axiosInstance.get<AlertRule[]>('/alert/api/v1/rules');
        return response.data
    },
  })
}

export function useCreateAlertRule() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: AlertRuleFormValues) => axiosInstance.post('/alert/api/v1/rules', data),
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['alertRules'] });
    },
  })
}

export function useDeleteAlertRule() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => axiosInstance.delete(`/alert/api/v1/rules/${id}`),
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['alertRules'] });
    },
  })
}
