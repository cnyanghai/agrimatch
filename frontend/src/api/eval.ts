import { http, type Result } from './http'

export interface EvalCreateRequest {
  dealId: number
  requirementId?: number
  supplyId?: number
  toCompanyId: number
  stars: number
  comment?: string
  imagesJson?: string
}

export interface EvalResponse {
  id: number
  dealId?: number
  requirementId?: number
  supplyId?: number
  fromUserId: number
  toCompanyId: number
  fromUserName?: string
  fromNickName?: string
  toCompanyName?: string
  stars: number
  comment?: string
  imagesJson?: string
  createTime?: string
}

export async function createEval(req: EvalCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/evals', req)
  return data
}

export async function listCompanyEvals(companyId: number) {
  const { data } = await http.get<Result<EvalResponse[]>>(`/api/evals/company/${companyId}`)
  return data
}


