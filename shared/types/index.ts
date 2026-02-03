/**
 * Shared type definitions for the WoGu (沃谷) platform.
 * These types are used by both frontend and backend consumers.
 */

// ==================== Common API Response Wrappers ====================

/**
 * Standard API response wrapper.
 * All backend APIs return this shape. Check `code === 0` for success.
 */
export interface Result<T> {
  code: number
  message: string
  data?: T
}

/**
 * Paginated list response.
 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// ==================== Sort & Pagination Params ====================

/**
 * Common pagination query parameters.
 */
export interface PaginationParams {
  page?: number
  size?: number
}

/**
 * Common sort query parameters.
 */
export interface SortParams {
  orderBy?: string
  order?: 'asc' | 'desc' | string
}
