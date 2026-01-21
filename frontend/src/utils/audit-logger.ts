export interface AuditLog {
  userId: number
  action: string
  resourceType: string
  resourceId: number
  details?: Record<string, any>
  ipAddress?: string
  userAgent?: string
  timestamp?: string
}

export class AuditLogger {
  private queue: AuditLog[] = []
  private flushInterval: number = 5000
  private maxQueueSize: number = 50
  private timer: number | null = null
  private apiEndpoint: string

  constructor(apiEndpoint: string) {
    this.apiEndpoint = apiEndpoint
  }

  start(): void {
    this.timer = window.setInterval(() => {
      this.flush()
    }, this.flushInterval)
  }

  stop(): void {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  }

  log(log: Omit<AuditLog, 'timestamp'>): void {
    const auditLog: AuditLog = {
      ...log,
      timestamp: new Date().toISOString(),
      ipAddress: this.getClientIp(),
      userAgent: navigator.userAgent
    }

    this.queue.push(auditLog)

    if (this.queue.length >= this.maxQueueSize) {
      this.flush()
    }
  }

  private async flush(): Promise<void> {
    if (this.queue.length === 0) return

    const logs = [...this.queue]
    this.queue = []

    try {
      await fetch(this.apiEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ logs }),
        credentials: 'include'
      })
    } catch (error) {
      console.error('[AuditLogger] Failed to flush logs:', error)
      this.queue.unshift(...logs)
    }
  }

  private getClientIp(): string {
    return localStorage.getItem('clientIp') || 'unknown'
  }
}

export const auditLogger = new AuditLogger('/api/audit/logs')
