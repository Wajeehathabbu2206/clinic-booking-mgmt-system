import type { HTMLAttributes, ReactNode } from 'react'
import { cn } from '@/utils/cn'

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  title?: string
  actions?: ReactNode
  padded?: boolean
}

export default function Card({
  title,
  actions,
  padded = true,
  className,
  children,
  ...rest
}: CardProps) {
  return (
    <div
      className={cn('rounded-xl border border-gray-200 bg-white shadow-sm', className)}
      {...rest}
    >
      {(title || actions) && (
        <div className="flex items-center justify-between border-b border-gray-100 px-5 py-3">
          {title && <h3 className="text-base font-semibold text-gray-900">{title}</h3>}
          {actions}
        </div>
      )}
      <div className={cn(padded && 'p-5')}>{children}</div>
    </div>
  )
}