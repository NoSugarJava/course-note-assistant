import api from './index'

export function uploadSlideSet(files, title) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  if (title) {
    formData.append('title', title)
  }
  return api
    .post('/slidesets', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    .then((res) => res.data)
}

export function getSlideSetStatus(id) {
  return api.get(`/slidesets/${id}`).then((res) => res.data)
}
