import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      const message = data?.error || `Request failed with status ${status}`
      error.userMessage = message
    } else if (error.request) {
      error.userMessage = 'Network error. Please check your connection.'
    } else {
      error.userMessage = error.message
    }
    return Promise.reject(error)
  }
)

export default api
