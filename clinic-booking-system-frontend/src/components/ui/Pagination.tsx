import Button from './Button'

interface PaginationProps {
  pageNumber: number // 0-based, same as backend
  totalPages: number
  onPageChange: (pageNumber: number) => void
}

export default function Pagination({ pageNumber, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null

  return (
    <nav className="flex items-center justify-between pt-4" aria-label="Pagination">
      <Button
        variant="secondary"
        size="sm"
        disabled={pageNumber === 0}
        onClick={() => onPageChange(pageNumber - 1)}
      >
        Previous
      </Button>
      <span className="text-sm text-gray-600">
        Page {pageNumber + 1} of {totalPages}
      </span>
      <Button
        variant="secondary"
        size="sm"
        disabled={pageNumber >= totalPages - 1}
        onClick={() => onPageChange(pageNumber + 1)}
      >
        Next
      </Button>
    </nav>
  )
}