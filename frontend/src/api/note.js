import api from './index'

export function getNotes() {
  return api.get('/notes').then((res) => res.data)
}

export function getNote(id) {
  return api.get(`/notes/${id}`).then((res) => res.data)
}

export function downloadNoteUrl(id) {
  return `/api/notes/${id}/download`
}

export function deleteNote(id) {
  return api.delete(`/notes/${id}`)
}
