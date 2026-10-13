import type { ReactNode } from 'react'
import { cn } from '@/utils/cn'

type BadgeVariant = 'green' | 'gray' | 'red' | 'blue' | 'yellow'

const styles: Record<BadgeVariant, string> = {
  green: 'bg-green-50 text-green-700',
  gray: 'bg-gray-100 text-gray-600',
  red: 'bg-red-50 text-red-700',
  blue: 'bg-primary-50 text-primary-700',
  yellow: 'bg-yellow-50 text-yellow-800',
}

interface BadgeProps {
  variant?: BadgeVariant
  children: ReactNode
  className?: string
}

export default function Badge({ variant = 'gray', children, className }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
        styles[variant],
        className,
      )}
    >
      {children}
    </span>
  )
}