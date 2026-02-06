import { get, post } from '../utils/request'

export interface EvalCreateRequest {
  dealId: number
  toCompanyId: number
  stars: number
  comment?: string
  imagesJson?: string
}

export interface EvalResponse {
  id: number
  dealId: number
  fromUserId: number
  toCompanyId: number
  stars: number
  comment?: string
  imagesJson?: string
  createTime?: string
  fromUserName?: string
  fromNickName?: string
  fromCompanyName?: string
}

/** Submit an evaluation */
export function createEval(req: EvalCreateRequest) {
  return post<number>('/api/evals', req)
}

/** List evaluations for a company */
export function listCompanyEvals(companyId: number) {
  return get<EvalResponse[]>(`/api/evals/company/${companyId}`)
}
