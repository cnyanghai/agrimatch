/**
 * 合同法律条款常量
 * 与 Web 端 frontend/src/constants/contractTerms.ts 保持一致
 */

/** 违约责任条款 */
export const BREACH_OF_CONTRACT_TERMS = `5.1 卖方违约责任：
    (1) 卖方逾期交货的，每逾期一日，应向买方支付合同总金额 0.5% 的违约金；
    (2) 卖方交付的产品质量不符合约定标准的，买方有权要求退货或换货，卖方应承担由此产生的运费及损失；
    (3) 卖方拒绝履行合同的，应向买方支付合同总金额 10% 的违约金。

5.2 买方违约责任：
    (1) 买方逾期付款的，每逾期一日，应向卖方支付逾期金额 0.5% 的违约金；
    (2) 买方无正当理由拒收货物的，应向卖方支付合同总金额 10% 的违约金，并承担卖方因此产生的仓储、运输等损失。

5.3 违约金不足以弥补损失的，违约方应继续赔偿差额部分。`

/** 不可抗力条款 */
export const FORCE_MAJEURE_TERMS = `6.1 因地震、台风、洪水、战争、政府行为等不可抗力事件导致合同无法履行的，受影响方应在事件发生后 3 日内书面通知对方，并在 15 日内提供相关证明。

6.2 不可抗力事件持续超过 30 日的，任何一方有权书面通知对方解除合同，双方互不承担违约责任，但应就已履行部分进行结算。`

/** 争议解决条款 */
export const DISPUTE_RESOLUTION_TERMS = `7.1 本合同的签订、履行、解释及争议解决均适用中华人民共和国法律。

7.2 双方因本合同发生争议的，应友好协商解决；协商不成的，任何一方均可向合同签订地人民法院提起诉讼。`

/** 合同生效条款 */
export const CONTRACT_EFFECTIVE_TERMS = `8.1 本合同自双方电子签章完成之日起生效。

8.2 本合同一式两份（电子版），双方各执一份，具有同等法律效力。

8.3 本合同未尽事宜，双方可另行签订补充协议，补充协议与本合同具有同等法律效力。`

/** 法律提示文本 */
export const LEGAL_NOTICE = '本合同条款具有法律效力。签署前请仔细阅读所有条款，如有疑问请咨询专业法律人士。合同一经双方签署即生效，对双方均具有约束力。'

/** 条款区块定义 */
export interface ContractTermSection {
  /** 条款编号 */
  number: string
  /** 条款标题（中文） */
  titleCn: string
  /** 条款标题（英文） */
  titleEn: string
  /** 条款内容 */
  content: string
}

/** 完整的合同条款结构 */
export const CONTRACT_TERM_SECTIONS: ContractTermSection[] = [
  {
    number: '五',
    titleCn: '违约责任',
    titleEn: 'Breach of Contract',
    content: BREACH_OF_CONTRACT_TERMS,
  },
  {
    number: '六',
    titleCn: '不可抗力',
    titleEn: 'Force Majeure',
    content: FORCE_MAJEURE_TERMS,
  },
  {
    number: '七',
    titleCn: '争议解决',
    titleEn: 'Dispute Resolution',
    content: DISPUTE_RESOLUTION_TERMS,
  },
  {
    number: '八',
    titleCn: '合同生效',
    titleEn: 'Contract Effectiveness',
    content: CONTRACT_EFFECTIVE_TERMS,
  },
]

/**
 * 获取法律约束条款
 * @param signingPlace 合同签订地（用于争议解决条款）
 */
export function getLegalBindingTerms(signingPlace?: string): ContractTermSection[] {
  return CONTRACT_TERM_SECTIONS.map(section => {
    if (section.number === '七' && signingPlace) {
      return {
        ...section,
        content: section.content + `\n\n7.3 合同签订地：${signingPlace}`,
      }
    }
    return section
  })
}
