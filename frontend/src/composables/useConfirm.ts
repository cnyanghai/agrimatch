import { reactive } from 'vue'

export type ConfirmType = 'warning' | 'danger' | 'info'

export interface ConfirmOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  type?: ConfirmType
}

export interface PromptOptions {
  title: string
  message: string
  placeholder?: string
  inputValue?: string
}

interface DialogState {
  visible: boolean
  mode: 'confirm' | 'prompt'
  options: ConfirmOptions | PromptOptions
  resolve: ((value: boolean | string | null) => void) | null
}

// Global reactive dialog state – a single dialog at a time
export const dialogState = reactive<DialogState>({
  visible: false,
  mode: 'confirm',
  options: { title: '', message: '' },
  resolve: null,
})

function openConfirm(options: ConfirmOptions): Promise<boolean> {
  return new Promise((resolve) => {
    dialogState.mode = 'confirm'
    dialogState.options = options
    dialogState.resolve = resolve as (value: boolean | string | null) => void
    dialogState.visible = true
  })
}

function openPrompt(options: PromptOptions): Promise<string | null> {
  return new Promise((resolve) => {
    dialogState.mode = 'prompt'
    dialogState.options = options
    dialogState.resolve = resolve as (value: boolean | string | null) => void
    dialogState.visible = true
  })
}

/** composable for use inside Vue components */
export function useConfirm() {
  return {
    confirm: openConfirm,
    prompt: openPrompt,
  }
}

/** Global singletons for non-component contexts */
export const showConfirm = openConfirm
export const showPrompt = openPrompt
