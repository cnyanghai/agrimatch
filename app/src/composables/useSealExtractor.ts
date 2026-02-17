/**
 * 印章图像提取 Composable（uni-app 版）
 * 从白底红章照片中提取红色印章部分，输出透明背景 PNG（base64）
 * 
 * H5 模式：使用 Canvas 2D API
 * APP-PLUS 模式：使用 plus.nativeObj.Bitmap（回退到 Canvas 2D）
 */

export interface SealExtractOptions {
  hueLow?: number       // 红色色相范围低端 (度数, 默认 335)
  hueHigh?: number      // 红色色相范围高端 (度数, 默认 25)
  minSaturation?: number // 最低饱和度 (0-100, 默认 25)
  minLightness?: number  // 最低亮度 (0-100, 默认 15)
  maxLightness?: number  // 最高亮度 (0-100, 默认 85)
  maxWidth?: number      // 输出最大宽度 (px, 默认 600)
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
  const isRedHue = h <= opts.hueHigh || h >= opts.hueLow
  return isRedHue && s >= opts.minSaturation && l >= opts.minLightness && l <= opts.maxLightness
}

/**
 * 从图片路径提取红色印章（H5 模式 - Canvas 2D）
 * @param imagePath 图片本地路径或 data URL
 * @param options 提取参数
 * @returns 透明背景的 PNG Data URL
 */
export function extractSealFromPath(imagePath: string, options?: SealExtractOptions): Promise<string> {
  const opts = { ...DEFAULT_OPTIONS, ...options }

  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      try {
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
        let minX = w, minY = h, maxX = 0, maxY = 0
        let hasRed = false

        for (let y = 0; y < h; y++) {
          for (let x = 0; x < w; x++) {
            const idx = (y * w + x) * 4
            const r = data[idx]!, g = data[idx + 1]!, b = data[idx + 2]!
            if (isRedPixel(r, g, b, opts)) {
              hasRed = true
              if (x < minX) minX = x
              if (x > maxX) maxX = x
              if (y < minY) minY = y
              if (y > maxY) maxY = y
            } else {
              data[idx + 3] = 0 // 非红色设为透明
            }
          }
        }

        if (!hasRed) {
          reject(new Error('未检测到红色印章区域，请上传清晰的白底红章照片'))
          return
        }

        ctx.putImageData(imageData, 0, 0)

        // 裁剪到印章边界
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

        resolve(cropCanvas.toDataURL('image/png'))
      } catch (e: any) {
        reject(new Error(e?.message || '印章提取处理失败'))
      }
    }
    img.onerror = () => reject(new Error('图片加载失败'))
    img.src = imagePath
  })
}

/**
 * 将 base64 Data URL 转为临时文件路径（用于上传）
 * H5 模式下直接可用，APP-PLUS 需要写入临时目录
 */
export function dataUrlToTempFile(dataUrl: string): Promise<string> {
  return new Promise((resolve) => {
    // #ifdef H5
    // H5 直接返回 data URL（可被 uploadFile 接受）
    resolve(dataUrl)
    // #endif
    // #ifdef APP-PLUS
    // APP-PLUS: base64 写入临时文件
    const base64Data = dataUrl.split(',')[1] || ''
    const tempPath = `_doc/seal_${Date.now()}.png`
    plus.io.resolveLocalFileSystemURL('_doc/', (dirEntry: any) => {
      dirEntry.getFile(`seal_${Date.now()}.png`, { create: true }, (fileEntry: any) => {
        fileEntry.createWriter((writer: any) => {
          writer.onwrite = () => resolve(fileEntry.toLocalURL())
          writer.onerror = () => resolve(dataUrl) // fallback
          writer.write(base64Data)
        }, () => resolve(dataUrl))
      }, () => resolve(dataUrl))
    }, () => resolve(dataUrl))
    // #endif
  })
}

export function useSealExtractor() {
  return {
    extractSealFromPath,
    dataUrlToTempFile,
  }
}
