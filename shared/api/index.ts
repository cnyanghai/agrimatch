/**
 * Shared API types and path constants.
 * Re-exports everything from all domain modules.
 */

// Common types
export type { Result, PageResult, PaginationParams, SortParams } from '../types'

// Auth & User
export type {
  LoginResponse,
  MeResponse,
  RegisterRequest,
  CaptchaResponse,
  UserCreateRequest,
  UserUpdateRequest,
  UserRoleUpdateRequest,
  UserResponse,
  UserBriefResponse,
  LoginLogResponse,
} from './auth'
export { AUTH_API, USER_API } from './auth'

// Supply
export type {
  BasisQuoteRequest,
  BasisQuoteResponse,
  SupplyCreateRequest,
  SupplyUpdateRequest,
  SupplyResponse,
  SupplyListParams,
  SupplyTemplateCreateRequest,
  SupplyTemplateResponse,
} from './supply'
export { SUPPLY_API, SUPPLY_TEMPLATE_API } from './supply'

// Requirement
export type {
  RequirementCreateRequest,
  RequirementUpdateRequest,
  RequirementResponse,
  RequirementListParams,
} from './requirement'
export { REQUIREMENT_API } from './requirement'

// Company
export type {
  CompanyType,
  CompanyCreateRequest,
  CompanyUpdateRequest,
  CompanyResponse,
  CompanyBriefResponse,
  CompanyCardResponse,
  CompanyProfileResponse,
} from './company'
export { COMPANY_API } from './company'

// Post
export type {
  PostType,
  PostCreateRequest,
  PostResponse,
  PostQuery,
  PostLikeToggleResponse,
  PostCommentResponse,
} from './post'
export { POST_API } from './post'

// Chat
export type {
  ChatMsgType,
  ChatQuoteStatus,
  ChatSubjectType,
  ChatPeerResponse,
  ChatMessageResponse,
  ChatConversationResponse,
  ChatConversationOpenRequest,
} from './chat'
export { CHAT_API } from './chat'

// Contract
export type {
  ContractStatus,
  ContractType,
  ProductParam,
  ContractResponse,
  ContractFromQuoteRequest,
  ContractFromNegotiationRequest,
  ContractCreateRequest,
  ContractUpdateRequest,
  ContractSignRequest,
  ContractQuery,
  SealResponse,
  SealCreateRequest,
  MilestoneResponse,
  MilestoneCreateRequest,
  MilestoneSubmitRequest,
  BankInfo,
  ContractStats,
  PartnerCompany,
} from './contract'
export { CONTRACT_API, SEAL_API, MILESTONE_API } from './contract'

// Product, Tag, Search
export type {
  ProductNode,
  ProductCreateRequest,
  ProductParamResponse,
  Tag,
  TagValue,
  SearchEntityType,
  UnifiedSearchResult,
  UnifiedSearchParams,
} from './product'
export { PRODUCT_API, TAG_API, SEARCH_API } from './product'

// Deal
export type {
  DealCreateRequest,
  DealResponse,
} from './deal'
export { DEAL_API } from './deal'

// Follow
export type {
  FollowedUser,
  FollowStats,
} from './follow'
export { FOLLOW_API } from './follow'

// Points
export type {
  PointsMeResponse,
  PointsTxResponse,
  JdRedeemResponse,
  JdRedeemDetailResponse,
  AdminJdRedeemResponse,
} from './points'
export { POINTS_API } from './points'
