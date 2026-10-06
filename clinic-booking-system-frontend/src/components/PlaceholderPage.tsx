import { Card } from '@/components/ui'

interface PlaceholderPageProps {
  title: string
  phase: string
  description?: string
}

export default function PlaceholderPage({ title, phase, description }: PlaceholderPageProps) {
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
      <Card>
        <p className="text-sm text-gray-600">
          {description ?? 'This page will be built in a later phase.'}
        </p>
        <p className="mt-2 inline-block rounded-full bg-primary-50 px-3 py-1 text-xs font-medium text-primary-700">
          Planned: {phase}
        </p>
      </Card>
    </div>
  )
}