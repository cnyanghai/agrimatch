/**
 * 文件上传 API（uni-app 版）
 */
import { uploadFile } from '../utils/request'

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
 * @param filePath 本地图片路径
 */
export function uploadImage(filePath: string): Promise<FileUploadResponse> {
  return uploadFile(filePath, '/api/files/upload/image', 'file')
}

/**
 * 上传附件
 * @param filePath 本地文件路径
 */
export function uploadAttachment(filePath: string): Promise<FileUploadResponse> {
  return uploadFile(filePath, '/api/files/upload/attachment', 'file')
}
