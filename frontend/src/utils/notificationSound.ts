/**
 * 通知提示音工具
 */

let audioContext: AudioContext | null = null
let isEnabled = true

/**
 * 初始化音频上下文（需要用户交互后调用）
 */
export function initAudioContext() {
  if (!audioContext) {
    audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
  }
  return audioContext
}

/**
 * 播放提示音
 * 使用 Web Audio API 生成简单的提示音
 */
export function playNotificationSound() {
  if (!isEnabled) return

  try {
    const ctx = initAudioContext()
    if (ctx.state === 'suspended') {
      ctx.resume()
    }

    // 创建振荡器（生成声音）
    const oscillator = ctx.createOscillator()
    const gainNode = ctx.createGain()

    oscillator.connect(gainNode)
    gainNode.connect(ctx.destination)

    // 设置声音参数 - 柔和的提示音
    oscillator.frequency.setValueAtTime(800, ctx.currentTime) // 频率 800Hz
    oscillator.type = 'sine' // 正弦波，声音柔和

    // 音量渐变（避免突兀）
    gainNode.gain.setValueAtTime(0, ctx.currentTime)
    gainNode.gain.linearRampToValueAtTime(0.3, ctx.currentTime + 0.05) // 快速升高
    gainNode.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.3) // 缓慢降低

    // 播放
    oscillator.start(ctx.currentTime)
    oscillator.stop(ctx.currentTime + 0.3)
  } catch (e) {
    console.warn('[NotificationSound] Failed to play sound:', e)
  }
}

/**
 * 播放双音提示（更明显的提示）
 */
export function playDoubleBeep() {
  if (!isEnabled) return

  try {
    const ctx = initAudioContext()
    if (ctx.state === 'suspended') {
      ctx.resume()
    }

    // 第一个音
    const osc1 = ctx.createOscillator()
    const gain1 = ctx.createGain()
    osc1.connect(gain1)
    gain1.connect(ctx.destination)
    osc1.frequency.setValueAtTime(880, ctx.currentTime)
    osc1.type = 'sine'
    gain1.gain.setValueAtTime(0, ctx.currentTime)
    gain1.gain.linearRampToValueAtTime(0.2, ctx.currentTime + 0.02)
    gain1.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.15)
    osc1.start(ctx.currentTime)
    osc1.stop(ctx.currentTime + 0.15)

    // 第二个音（延迟 0.15 秒）
    const osc2 = ctx.createOscillator()
    const gain2 = ctx.createGain()
    osc2.connect(gain2)
    gain2.connect(ctx.destination)
    osc2.frequency.setValueAtTime(1100, ctx.currentTime + 0.15)
    osc2.type = 'sine'
    gain2.gain.setValueAtTime(0, ctx.currentTime + 0.15)
    gain2.gain.linearRampToValueAtTime(0.2, ctx.currentTime + 0.17)
    gain2.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.35)
    osc2.start(ctx.currentTime + 0.15)
    osc2.stop(ctx.currentTime + 0.35)
  } catch (e) {
    console.warn('[NotificationSound] Failed to play double beep:', e)
  }
}

/**
 * 启用/禁用提示音
 */
export function setNotificationSoundEnabled(enabled: boolean) {
  isEnabled = enabled
}

/**
 * 获取提示音状态
 */
export function isNotificationSoundEnabled(): boolean {
  return isEnabled
}
