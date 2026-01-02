<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '../components/PageHeader.vue'
import { listRequirements, type RequirementResponse } from '../api/requirement'

const router = useRouter()
const loading = ref(false)

const requirements = ref<RequirementResponse[]>([])

// 分页相关
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 筛选条件
const filters = reactive({
  categoryName: '',
  status: undefined as number | undefined
})

async function loadRequirements() {
  loading.value = true
  try {
    const r = await listRequirements({
      categoryName: filters.categoryName || undefined,
      status: filters.status
    })
    if (r.code === 0) {
      requirements.value = r.data || []
      pagination.total = requirements.value.length
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载需求列表失败')
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pagination.page = 1
  loadRequirements()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadRequirements()
}

function getStatusText(status?: number) {
  switch (status) {
    case 0: return '发布中'
    case 1: return '部分成交'
    case 2: return '已下架'
    case 3: return '全部成交'
    default: return '未知'
  }
}

function getStatusType(status?: number) {
  switch (status) {
    case 0: return 'primary'
    case 1: return 'warning'
    case 2: return 'info'
    case 3: return 'success'
    default: return 'info'
  }
}

onMounted(() => {
  loadRequirements()
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <div class="max-w-7xl mx-auto p-4 md:p-6 space-y-6">
      <PageHeader title="已发布采购" subtitle="管理您已发布的采购需求列表">
        <template #right>
          <el-button class="!rounded-xl transition-all active:scale-95" :icon="Refresh" :loading="loading" @click="loadRequirements">
            刷新
          </el-button>
          <el-button class="!rounded-xl transition-all active:scale-95" @click="router.push('/requirements')">
            返回发布
          </el-button>
          <el-button
            type="primary"
            class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
            :icon="Plus"
            @click="router.push('/requirements')"
          >
            发布新需求
          </el-button>
        </template>
      </PageHeader>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex items-center justify-between gap-4">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">list</div>
            <div class="text-lg font-bold text-gray-900 mt-1">采购需求列表</div>
            <div class="text-sm text-gray-500 mt-1">支持按品类与状态快速筛选</div>
          </div>
          <el-tag type="info" effect="light" size="large" class="!rounded-full">
            共 {{ pagination.total }} 条
          </el-tag>
        </div>

        <div class="p-6">
          <div class="flex flex-wrap gap-4 mb-6 p-4 bg-gray-50 rounded-2xl border border-gray-100">
            <div class="flex flex-wrap gap-4 items-center">
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">品类</span>
                <el-input
                  v-model="filters.categoryName"
                  placeholder="搜索品类"
                  class="w-40"
                  @keyup.enter="handleFilter"
                />
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">状态</span>
                <el-select v-model="filters.status" class="w-40" clearable>
                  <el-option label="全部" :value="undefined" />
                  <el-option label="发布中" :value="0" />
                  <el-option label="部分成交" :value="1" />
                  <el-option label="已下架" :value="2" />
                  <el-option label="全部成交" :value="3" />
                </el-select>
              </div>
              <el-button
                type="primary"
                class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
                @click="handleFilter"
              >
                搜索
              </el-button>
            </div>
          </div>

          <div v-if="requirements.length === 0" class="text-center py-12 text-gray-500">
            暂无采购需求
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="req in requirements"
              :key="req.id"
              class="bg-white rounded-2xl border border-gray-100 p-5 hover:shadow-md transition-shadow"
            >
              <div class="flex justify-between items-start mb-3 gap-4">
                <div class="min-w-0">
                  <div class="flex items-center gap-2 mb-2">
                    <h3 class="text-lg font-bold text-gray-900 truncate">{{ req.categoryName }}</h3>
                    <el-tag :type="getStatusType(req.status)" size="small">
                      {{ getStatusText(req.status) }}
                    </el-tag>
                  </div>
                  <div class="text-sm text-gray-600">
                    数量: {{ req.quantity }} 吨 |
                    包装: {{ req.packaging }} |
                    付款: {{ req.paymentMethod }}
                  </div>
                </div>
                <div class="text-right shrink-0">
                  <div class="text-sm text-gray-500">
                    {{ req.createTime ? new Date(req.createTime).toLocaleDateString() : '' }}
                  </div>
                  <div v-if="req.remainingQuantity !== undefined" class="text-sm text-gray-500">
                    剩余: {{ req.remainingQuantity }} 吨
                  </div>
                </div>
              </div>

              <div v-if="req.paramsJson" class="mb-3">
                <div class="text-sm text-gray-600">
                  参数:
                  {{ (() => { try { return JSON.parse(req.paramsJson)?.custom ? Object.values(JSON.parse(req.paramsJson).custom).join(', ') : '无特殊要求' } catch { return '无特殊要求' } })() }}
                </div>
              </div>

              <div class="flex items-center justify-between text-sm text-gray-500 gap-4">
                <div class="truncate">
                  采购地: {{ req.purchaseAddress || '未指定' }}
                </div>
                <div class="shrink-0">
                  有效期至: {{ req.expireTime ? new Date(req.expireTime).toLocaleDateString() : '长期有效' }}
                </div>
              </div>
            </div>
          </div>

          <div class="mt-6 flex justify-center">
            <el-pagination
              v-if="pagination.total > 0"
              v-model:current-page="pagination.page"
              :page-size="pagination.size"
              :total="pagination.total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


