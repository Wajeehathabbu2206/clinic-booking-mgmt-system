import { Badge } from '@/components/ui'
import type { ClinicStatus } from '@/types/clinic'

export default function ClinicStatusBadge({ status }: { status: ClinicStatus }) {
  return status === 'ACTIVE' ? (
    <Badge variant="green">Active</Badge>
  ) : (
    <Badge variant="gray">Inactive</Badge>
  )
}