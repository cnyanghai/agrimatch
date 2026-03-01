export function isMobileBrowser(): boolean {
  return /Android|iPhone|iPad|iPod/i.test(navigator.userAgent)
}

export function isAndroid(): boolean {
  return /Android/i.test(navigator.userAgent)
}

export function isIos(): boolean {
  return /iPhone|iPad|iPod/i.test(navigator.userAgent)
}

export function isStandalone(): boolean {
  return window.matchMedia('(display-mode: standalone)').matches
    || (navigator as any).standalone === true
}
