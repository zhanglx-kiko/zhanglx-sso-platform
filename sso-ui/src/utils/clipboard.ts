export const copyText = async (text: string): Promise<boolean> => {
  const value = text.trim()
  if (!value) return false

  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(value)
    return true
  }

  const textarea = document.createElement('textarea')
  textarea.value = value
  textarea.setAttribute('readonly', 'true')
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()

  try {
    const copied = document.execCommand('copy')
    return copied
  } finally {
    document.body.removeChild(textarea)
  }
}
