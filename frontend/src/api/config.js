import api from './index'

export function getConfig() {
  return api.get('/config').then((res) => res.data)
}

export function updateConfig(updates) {
  return api.put('/config', { updates }).then((res) => res.data)
}
