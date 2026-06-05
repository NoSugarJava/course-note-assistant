/**
 * Create an EventSource for SSE progress streaming.
 * @param {string} url - SSE endpoint URL
 * @param {function} onProgress - callback({ slideSetId, progress, step, status, noteId, errorMessage })
 * @param {function} onComplete - callback(noteId)
 * @param {function} onError - callback(errorMessage)
 * @returns {EventSource}
 */
export function createProgressStream(url, { onProgress, onComplete, onError }) {
  const eventSource = new EventSource(url)

  eventSource.addEventListener('progress', (e) => {
    try {
      const data = JSON.parse(e.data)
      if (onProgress) onProgress(data)

      if (data.status === 'COMPLETED' && onComplete) {
        onComplete(data.noteId)
        eventSource.close()
      } else if (data.status === 'FAILED' && onError) {
        onError(data.errorMessage || 'Unknown error')
        eventSource.close()
      }
    } catch (err) {
      console.error('Failed to parse SSE event:', err)
    }
  })

  eventSource.onerror = () => {
    if (eventSource.readyState === EventSource.CLOSED) {
      return
    }
    if (onError) onError('Connection lost')
    eventSource.close()
  }

  return eventSource
}
