import { defineStore } from 'pinia'
import { getNotes, getNote, deleteNote } from '@/api/note'

export const useNoteStore = defineStore('note', {
  state: () => ({
    notes: [],
    currentNote: null,
    loading: false,
    error: null
  }),

  actions: {
    async fetchNotes() {
      this.loading = true
      this.error = null
      try {
        this.notes = await getNotes()
      } catch (e) {
        this.error = e.userMessage || 'Failed to load notes'
      } finally {
        this.loading = false
      }
    },

    async fetchNote(id) {
      this.loading = true
      this.error = null
      try {
        this.currentNote = await getNote(id)
      } catch (e) {
        this.error = e.userMessage || 'Failed to load note'
      } finally {
        this.loading = false
      }
    },

    async removeNote(id) {
      try {
        await deleteNote(id)
        this.notes = this.notes.filter((n) => n.id !== id)
      } catch (e) {
        this.error = e.userMessage || 'Failed to delete note'
        throw e
      }
    },

    clearCurrentNote() {
      this.currentNote = null
    }
  }
})
