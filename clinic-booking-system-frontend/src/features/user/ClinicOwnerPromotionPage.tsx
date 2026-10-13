import { useEffect, useState } from 'react'
import { userApi } from '@/api/userApi'
import { Alert, Button, Card, EmptyState, Modal, Pagination, Spinner } from '@/components/ui'
import type { PagedResponse } from '@/types/api'
import type { UserPromotionCandidate } from '@/types/user'
import { getErrorMessage } from '@/utils/errors'
import { formatDate } from '@/utils/format'

const PAGE_SIZE = 10

export default function ClinicOwnerPromotionPage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PagedResponse<UserPromotionCandidate> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [selected, setSelected] = useState<UserPromotionCandidate | null>(null)
  const [promoting, setPromoting] = useState(false)
  const [actionError, setActionError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    userApi.getClinicOwnerPromotionCandidates({ page, size: PAGE_SIZE })
      .then((result) => {
        if (cancelled) return
        setData(result)
        setError('')
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => { cancelled = true }
  }, [page, reloadKey])

  const promote = async () => {
    if (!selected) return
    setPromoting(true)
    setActionError('')
    try {
      await userApi.promoteToClinicOwner(selected.id)
      setSuccess(`${selected.fullName} was promoted to clinic owner.`)
      setSelected(null)
      setReloadKey((key) => key + 1)
    } catch (err) {
      setActionError(getErrorMessage(err))
    } finally {
      setPromoting(false)
    }
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">Promote clinic owners</h1>
        <p className="mt-1 text-sm text-gray-600">
          Select a patient account to grant clinic owner access. The user must sign in again after promotion.
        </p>
      </div>

      {success && <Alert variant="success" onClose={() => setSuccess('')}>{success}</Alert>}
      {error && (
        <Alert variant="error" title="Could not load patient accounts">
          {error}
          <div className="mt-2"><Button size="sm" variant="secondary" onClick={() => setReloadKey((key) => key + 1)}>Try again</Button></div>
        </Alert>
      )}

      {loading && !data ? (
        <div className="flex justify-center py-16"><Spinner className="h-8 w-8 text-primary-600" /></div>
      ) : data && data.content.length === 0 ? (
        <EmptyState title="No patient accounts to promote" description="Newly registered patient accounts will appear here." />
      ) : data ? (
        <>
          <Card>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
                <thead className="text-xs uppercase tracking-wide text-gray-500">
                  <tr>
                    <th className="px-3 py-3 font-medium">Name</th>
                    <th className="px-3 py-3 font-medium">Email</th>
                    <th className="px-3 py-3 font-medium">Registered</th>
                    <th className="px-3 py-3 text-right font-medium">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {data.content.map((candidate) => (
                    <tr key={candidate.id}>
                      <td className="px-3 py-4 font-medium text-gray-900">{candidate.fullName}</td>
                      <td className="px-3 py-4 text-gray-600">{candidate.email}</td>
                      <td className="px-3 py-4 text-gray-600">{formatDate(candidate.createdAt)}</td>
                      <td className="px-3 py-4 text-right">
                        <Button size="sm" onClick={() => { setSelected(candidate); setActionError('') }}>Promote</Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
          <Pagination pageNumber={data.pageNumber} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : null}

      <Modal
        open={selected !== null}
        title="Promote to clinic owner?"
        onClose={() => promoting ? undefined : setSelected(null)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setSelected(null)} disabled={promoting}>Cancel</Button>
            <Button onClick={promote} loading={promoting}>Promote account</Button>
          </>
        }
      >
        <p>
          {selected?.fullName} ({selected?.email}) will receive clinic owner permissions. They can manage clinics they own or are assigned to.
        </p>
        {actionError && <Alert variant="error" className="mt-3">{actionError}</Alert>}
      </Modal>
    </div>
  )
}
