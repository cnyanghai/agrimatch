import { http, type Result } from './http'

export interface RequirementTemplateCreateRequest {
  templateName: string
  templateJson: string
}

export interface RequirementTemplateResponse {
  id: number
  templateName: string
  templateJson: string
  createTime?: string
  updateTime?: string
}

export async function listMyRequirementTemplates() {
  const { data } = await http.get<Result<RequirementTemplateResponse[]>>('/api/requirement-templates')
  return data
}

export async function createRequirementTemplate(req: RequirementTemplateCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/requirement-templates', req)
  return data
}

export async function deleteRequirementTemplate(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/requirement-templates/${id}`)
  return data
}


