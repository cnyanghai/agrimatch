import { http, type Result } from './http'

export interface FileUploadResponse {
  fileId: string
  fileName: string
  fileUrl: string
  size: number
  mimeType: string
  fileType: 'IMAGE' | 'ATTACHMENT'
}

/**
 * 上传图片
 * @param file 图片文件
 * @param onProgress 上传进度回调 (0-100)
 */
export async function uploadImage(
  file: File,
  onProgress?: (percent: number) => void
): Promise<Result<FileUploadResponse>> {
  const formData = new FormData()
  formData.append('file', file)
  
  const res = await http.post<Result<FileUploadResponse>>('/api/files/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
  return res.data
}

/**
 * 上传附件
 * @param file 附件文件
 * @param onProgress 上传进度回调 (0-100)
 */
export async function uploadAttachment(
  file: File,
  onProgress?: (percent: number) => void
): Promise<Result<FileUploadResponse>> {
  const formData = new FormData()
  formData.append('file', file)
  
  const res = await http.post<Result<FileUploadResponse>>('/api/files/upload/attachment', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
  return res.data
}

/**
 * 获取文件下载/访问 URL
 * @param fileUrl 文件路径 (如 /uploads/chat/images/2025-01/xxx.jpg)
 */
export function getFileDownloadUrl(fileUrl: string): string {
  // 图片可以直接访问静态资源路径
  if (fileUrl.startsWith('/uploads/')) {
    return fileUrl
  }
  // 其他情况通过下载 API
  return `/api/files/download?path=${encodeURIComponent(fileUrl)}`
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 检查是否为图片类型
 */
export function isImageFile(file: File | string): boolean {
  if (typeof file === 'string') {
    // 通过扩展名判断
    const ext = file.toLowerCase().split('.').pop()
    return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext || '')
  }
  return file.type.startsWith('image/')
}

/**
 * 图片消息 payload 类型
 */
export interface ImagePayload {
  fileId: string
  fileName: string
  fileUrl: string
  size: number
  mimeType: string
  width?: number
  height?: number
}

/**
 * 附件消息 payload 类型
 */
export interface AttachmentPayload {
  fileId: string
  fileName: string
  fileUrl: string
  size: number
  mimeType: string
}

