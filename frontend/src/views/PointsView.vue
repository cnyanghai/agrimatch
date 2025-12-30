<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getPointsMe, listPointsTx, rechargePoints, redeemPoints, type PointsTxResponse } from '../api/points'
import PageHeader from '../components/PageHeader.vue'

const loading = ref(false)
const creating = ref(false)
const me = ref<{ pointsBalance: number; cnyBalance: number } | null>(null)
const txs = ref<PointsTxResponse[]>([])

const rechargeVal = ref<number>(100)
const redeemVal = ref<number>(50)

const canRecharge = computed(() => Number.isFinite(rechargeVal.value) && rechargeVal.value > 0)
const canRedeem = computed(() => Number.isFinite(redeemVal.value) && redeemVal.value > 0)

async function refresh() {
  loading.value = true
  try {
    const r1 = await getPointsMe()
    if (r1.code !== 0) throw new Error(r1.message)
    me.value = r1.data ?? null

    const r2 = await listPointsTx()
    if (r2.code !== 0) throw new Error(r2.message)
    txs.value = r2.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function onRecharge() {
  if (!canRecharge.value) return
  creating.value = true
  try {
    const r = await rechargePoints(rechargeVal.value)
    if (r.code !== 0) throw new Error(r.message)
    me.value = r.data ?? me.value
    ElMessage.success('充值成功')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '充值失败')
  } finally {
    creating.value = false
  }
}

async function onRedeem() {
  if (!canRedeem.value) return
  creating.value = true
  try {
    const r = await redeemPoints(redeemVal.value)
    if (r.code !== 0) throw new Error(r.message)
    me.value = r.data ?? me.value
    ElMessage.success('兑换成功')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '兑换失败')
  } finally {
    creating.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div class="space-y-4">
    <PageHeader title="积分" subtitle="余额、充值、兑换与流水记录">
      <template #right>
        <el-tag type="info">兑换比例：1 积分 = 1 元</el-tag>
      </template>
    </PageHeader>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="bg-white rounded-xl shadow-card border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-3">
          <div class="font-bold text-gray-800">我的余额</div>
          <el-button :loading="loading" @click="refresh">刷新</el-button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="p-4 rounded-xl border border-gray-100">
            <el-statistic title="积分余额" :value="me?.pointsBalance ?? 0" />
          </div>
          <div class="p-4 rounded-xl border border-gray-100">
            <el-statistic title="人民币余额" :value="me?.cnyBalance ?? 0" />
          </div>
        </div>

        <div class="mt-4 p-3 bg-gray-50 border border-gray-200 rounded-lg">
          <el-form inline>
            <el-form-item label="充值积分">
              <el-input-number v-model="rechargeVal" :min="1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :disabled="!canRecharge" :loading="creating" @click="onRecharge">充值</el-button>
            </el-form-item>

            <el-form-item label="兑换积分">
              <el-input-number v-model="redeemVal" :min="1" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :disabled="!canRedeem" :loading="creating" @click="onRedeem">兑换为人民币</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-card border border-gray-100 p-5">
        <div class="font-bold text-gray-800 mb-3">流水</div>
        <el-table :data="txs" v-loading="loading" style="width: 100%;">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="txType" label="类型" width="120" />
          <el-table-column prop="pointsDelta" label="积分变动" width="120" />
          <el-table-column prop="cnyDelta" label="人民币变动" width="140" />
          <el-table-column prop="remark" label="备注" min-width="160" />
          <el-table-column prop="createTime" label="时间" min-width="180" />
        </el-table>
      </div>
    </div>
  </div>
</template>


