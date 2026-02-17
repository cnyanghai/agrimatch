/**
 * Shared composable for Supply/Purchase Hall page filters.
 * Extracts common URL-parameter parsing, schema/category tree,
 * pagination, keyword search, and mobile sidebar state.
 */
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../api/productSchema'

export function useHallFilters() {
  const route = useRoute()
  const router = useRouter()

  // -- Search --
  const searchKeyword = ref('')
  const selectedCategory = ref<string | null>(null)

  // -- Pagination --
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  // -- Schema tree --
  const schemaTree = ref<ProductSchemaVO[]>([])
  const selectedSchemaCode = ref<string | null>(null)

  // -- Mobile sidebar --
  const mobileSidebarOpen = ref(false)

  // -- URL query helpers --
  const companyIdFilter = computed(() => {
    const raw = route.query.companyId
    const s = Array.isArray(raw) ? raw[0] : raw
    const n = s ? Number(s) : NaN
    return Number.isFinite(n) ? n : null
  })

  const schemaCodeFromRoute = computed((): string | null => {
    const raw = route.query.schemaCode
    return (Array.isArray(raw) ? raw[0] : raw) ?? null
  })

  const categoryNameFromRoute = computed((): string | null => {
    const raw = route.query.categoryName
    return (Array.isArray(raw) ? raw[0] : raw) ?? null
  })

  // -- Flatten category tree --
  function flattenCategories(nodes: CategoryNode[]): string[] {
    const result: string[] = []
    function traverse(list: CategoryNode[]) {
      for (const node of list) {
        if (node.children && node.children.length > 0) {
          traverse(node.children)
        } else {
          result.push(node.name)
        }
      }
    }
    traverse(nodes)
    return result
  }

  // -- Load schema tree --
  async function loadSchemaTree() {
    try {
      const r = await getSchemaTree()
      if (r.code === 0 && r.data) {
        schemaTree.value = r.data
      }
    } catch {
      // silent
    }
  }

  // -- Find schema code by category name --
  function findSchemaCodeByCategory(categoryName: string): string {
    for (const schema of schemaTree.value) {
      const categories = flattenCategories(schema.categories)
      if (categories.includes(categoryName)) {
        return schema.schemaCode
      }
    }
    return 'feed'
  }

  // -- Pagination change --
  function handlePageChange(page: number) {
    currentPage.value = page
    window.scrollTo({ top: 400, behavior: 'smooth' })
  }

  // -- Initialize from URL on mount --
  function initFromRoute() {
    if (schemaCodeFromRoute.value) {
      selectedSchemaCode.value = schemaCodeFromRoute.value
    }
    if (categoryNameFromRoute.value) {
      selectedCategory.value = categoryNameFromRoute.value
    }
  }

  return {
    // State
    searchKeyword,
    selectedCategory,
    currentPage,
    pageSize,
    total,
    schemaTree,
    selectedSchemaCode,
    mobileSidebarOpen,

    // Computed
    companyIdFilter,
    schemaCodeFromRoute,
    categoryNameFromRoute,

    // Methods
    flattenCategories,
    loadSchemaTree,
    findSchemaCodeByCategory,
    handlePageChange,
    initFromRoute,
  }
}
