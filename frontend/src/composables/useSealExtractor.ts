/**
 * 印章图像提取 Composable
 * 从白底红章照片中提取红色印章部分，输出透明背景 PNG
 */

export interface SealExtractOptions {
  /** 红色色相范围低端 (度数, 默认 335) */
  hueLow?: number
  /** 红色色相范围高端 (度数, 默认 25) */
  hueHigh?: number
  /** 最低饱和度 (0-100, 默认 25) */
  minSaturation?: number
  /** 最低亮度 (0-100, 默认 15) */
  minLightness?: number
  /** 最高亮度 (0-100, 默认 85) */
  maxLightness?: number
  /** 输出最大宽度 (px, 默认 600) */
  maxWidth?: number
}

const DEFAULT_OPTIONS: Required<SealExtractOptions> = {
  hueLow: 335,
  hueHigh: 25,
  minSaturation: 25,
  minLightness: 15,
  maxLightness: 85,
  maxWidth: 600,
}

/** RGB → HSL 转换 */
function rgbToHsl(r: number, g: number, b: number): [number, number, number] {
  r /= 255; g /= 255; b /= 255
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const l = (max + min) / 2
  if (max === min) return [0, 0, l * 100]

  const d = max - min
  const s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
  let h = 0
  if (max === r) h = ((g - b) / d + (g < b ? 6 : 0)) / 6
  else if (max === g) h = ((b - r) / d + 2) / 6
  else h = ((r - g) / d + 4) / 6
  return [h * 360, s * 100, l * 100]
}

/** 判断像素是否为红色 */
function isRedPixel(r: number, g: number, b: number, opts: Required<SealExtractOptions>): boolean {
  const [h, s, l] = rgbToHsl(r, g, b)
  // 红色在色相环两端: 0°~hueHigh 和 hueLow~360°
  const isRedHue = h <= opts.hueHigh || h >= opts.hueLow
  return isRedHue && s >= opts.minSaturation && l >= opts.minLightness && l <= opts.maxLightness
}

/** 加载图片文件到 HTMLImageElement */
function loadImage(file: File): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片加载失败'))
    }
    img.src = url
  })
}

/** 加载 Data URL 到 HTMLImageElement */
function loadDataUrlImage(dataUrl: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('图片加载失败'))
    img.src = dataUrl
  })
}

/**
 * 从图片文件中提取红色印章
 * @param file 图片文件 (jpg/png)
 * @param options 提取参数
 * @returns 透明背景的 PNG Data URL
 */
export async function extractSeal(file: File, options?: SealExtractOptions): Promise<string> {
  const opts = { ...DEFAULT_OPTIONS, ...options }
  const img = await loadImage(file)

  // 绘制到 Canvas
  const canvas = document.createElement('canvas')
  let w = img.naturalWidth
  let h = img.naturalHeight
  if (w > opts.maxWidth) {
    h = Math.round(h * (opts.maxWidth / w))
    w = opts.maxWidth
  }
  canvas.width = w
  canvas.height = h
  const ctx = canvas.getContext('2d')!
  ctx.drawImage(img, 0, 0, w, h)

  // 像素级处理
  const imageData = ctx.getImageData(0, 0, w, h)
  const data = imageData.data

  // 记录红色像素边界
  let minX = w, minY = h, maxX = 0, maxY = 0
  let hasRed = false

  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const idx = (y * w + x) * 4
      const r = data[idx]!, g = data[idx + 1]!, b = data[idx + 2]!

      if (isRedPixel(r, g, b, opts)) {
        // 保留红色像素，alpha 不变
        hasRed = true
        if (x < minX) minX = x
        if (x > maxX) maxX = x
        if (y < minY) minY = y
        if (y > maxY) maxY = y
      } else {
        // 非红色像素设为透明
        data[idx + 3] = 0
      }
    }
  }

  if (!hasRed) {
    throw new Error('未检测到红色印章区域，请上传清晰的白底红章照片')
  }

  ctx.putImageData(imageData, 0, 0)

  // 裁剪到印章边界 (留 10px padding)
  const pad = 10
  const cropX = Math.max(0, minX - pad)
  const cropY = Math.max(0, minY - pad)
  const cropW = Math.min(w, maxX + pad + 1) - cropX
  const cropH = Math.min(h, maxY + pad + 1) - cropY

  const cropCanvas = document.createElement('canvas')
  cropCanvas.width = cropW
  cropCanvas.height = cropH
  const cropCtx = cropCanvas.getContext('2d')!
  cropCtx.drawImage(canvas, cropX, cropY, cropW, cropH, 0, 0, cropW, cropH)

  return cropCanvas.toDataURL('image/png')
}

/**
 * 将 Data URL 转为 File 对象
 */
export function dataUrlToFile(dataUrl: string, filename = 'seal.png'): File {
  const arr = dataUrl.split(',')
  const mime = arr[0]!.match(/:(.*?);/)?.[1] || 'image/png'
  const bstr = atob(arr[1] || '')
  const u8arr = new Uint8Array(bstr.length)
  for (let i = 0; i < bstr.length; i++) u8arr[i] = bstr.charCodeAt(i)
  return new File([u8arr], filename, { type: mime })
}

/**
 * Composable 封装
 */
export function useSealExtractor() {
  return {
    extractSeal,
    dataUrlToFile,
    loadDataUrlImage,
  }
}
