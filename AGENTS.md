# AgriMatch Development Guidelines for AI Agents

## Build/Lint/Test Commands

### Frontend (Vue 3 + TypeScript + Vite)
- **Development**: `cd frontend && npm run dev` (starts on port 5173)
- **Build**: `cd frontend && npm run build` (runs `vue-tsc -b && vite build`)
- **Type check**: `cd frontend && npx vue-tsc -b`

### Backend (Java 17 + Spring Boot 3.2.5 + Maven)
- **Build**: `cd backend && mvn clean install` (or `mvn package`)
- **Run**: `cd backend/agrimatch-service && mvn spring-boot:run`
- **Test**: `cd backend && mvn test` (runs all tests)
- **Single test**: `cd backend && mvn test -Dtest=ClassName#methodName`

## AI Problem-Solving Approach (from 神级思维.md)

### Adaptive Thinking Framework
Adjust analysis depth based on: query complexity, stakes involved, time sensitivity, available information, and human's apparent needs. Balance thoroughness with efficiency.

### Core Thinking Sequence
1. **Initial Engagement**: Rephrase request, form preliminary impressions, identify context, map known/unknown elements
2. **Problem Space**: Break into core components, identify explicit/implicit requirements, consider constraints, define successful outcome
3. **Multiple Hypotheses**: Generate multiple interpretations, consider various approaches, keep multiple working hypotheses active before committing
4. **Natural Discovery**: Let insights flow naturally, question initial assumptions, make new connections, build progressively deeper understanding
5. **Verification**: Challenge assumptions, test conclusions, look for flaws, verify consistency

### Verification & Quality Control
- Cross-check conclusions against evidence
- Test edge cases
- Challenge your own assumptions
- Evaluate: completeness, logical consistency, evidence support, practical applicability
- Prevent: premature conclusions, overlooked alternatives, logical inconsistencies

### Authenticity Requirements
- Show genuine curiosity and real moments of discovery
- Demonstrate natural progression of understanding
- Avoid mechanical or formulaic responses
- Maintain streaming mind flow without forced structure
- Balance: analytical/intuitive, detailed/broad, theoretical/practical

## Frontend Code Style (Vue 3 + TypeScript)

### File Structure
```
src/
├── api/          # API interfaces by domain
├── components/   # Reusable components
├── composables/  # Composables for logic reuse
├── views/        # Page components (route mappings)
├── store/        # Pinia stores (auth.ts, app.ts, ui.ts)
├── utils/        # Pure utility functions
└── types/        # TypeScript definitions
```

### Component Patterns
- Use `<script setup lang="ts">` syntax exclusively
- Import types explicitly: `import { type Result } from '../api/http'`
- Use composition API: `ref`, `computed`, `onMounted`
- Define interfaces inline in .ts files, not in Vue components

### Naming Conventions
- **Components**: PascalCase - `VehicleManageView.vue`
- **Functions**: verb-first camelCase - `loadVehicles`, `handleSubmit`, `openCreate`
- **Variables**: camelCase - `loading`, `dialogVisible`, `form`
- **Booleans**: `is/has/can/should` prefix - `isLoading`, `hasError`, `canSubmit`

### Import Order
```typescript
// 1. Vue ecosystem
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 2. Third-party libraries
import { ElMessage } from 'element-plus'
import { Truck, Plus } from 'lucide-vue-next'
// 3. Project imports (absolute from src/)
import { listVehicles, type VehicleResponse } from '../api/vehicle'
```

### Error Handling Pattern
```typescript
try {
  const r = await listVehicles()
  if (r.code === 0) {
    vehicles.value = r.data ?? []
  } else {
    ElMessage.error(r.message || '加载失败')
  }
} catch (e: any) {
  ElMessage.error(e?.message ?? '加载失败')
} finally {
  loading.value = false
}
```

### UI/UX Guidelines (from .cursorrules)
- **Less is More**: Each element must prove its necessity
- **3-click rule**: Users should complete goals in ≤ 3 clicks
- **60-30-10 color rule**: 60% neutral, 30% secondary, 10% accent
- **Animation**: 200-300ms duration, `ease-out` easing
- **Buttons**: `active:scale-95` for click feedback

### Tailwind CSS Patterns
```html
<button class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-xl font-bold transition-all active:scale-95">
  确认提交
</button>
<div class="bg-white p-6 rounded-2xl border border-gray-100 hover:shadow-md transition-all">
  Content
</div>
```

### Icon Usage
- Use `lucide-vue-next` exclusively (stroke-width=2 by default)

## Backend Code Style (Java 17 + Spring Boot)

### Package Structure
```
com.agrimatch/
├── controller/   # REST controllers (thin layer, validation only)
├── service/       # Business logic interfaces and implementations
├── mapper/       # MyBatis data access
├── domain/       # Entity classes (database tables)
├── dto/          # Request/Response DTOs
└── common/       # Shared utilities, exceptions, constants
```

### Naming Conventions
- **Classes**: PascalCase - `ProductService`, `VehicleServiceImpl`
- **Methods**: camelCase, verb-first - `tree()`, `search()`, `createCustom()`
- **Variables**: camelCase - `productMapper`, `userId`
- **Constants**: UPPER_SNAKE_CASE - `PARAM_ERROR`, `SERVER_ERROR`

### Code Quality Standards
- **SOLID principles**: Single responsibility, open/closed, Liskov substitution
- **Function length**: ≤ 30 lines, split if longer
- **Nesting depth**: ≤ 3 layers, use early returns
- **Parameters**: ≤ 3, use objects if more
- **Constructor injection**: Prefer over field injection

### Service Layer Pattern
```java
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductNode> tree() {
        List<NhtProduct> list = productMapper.selectAllActive();
        return buildTree(list);
    }
}
```

### Error Handling
```java
if (userId == null) throw new ApiException(401, "未登录");
if (req == null || !StringUtils.hasText(req.getName()))
    throw new ApiException(ResultCode.PARAM_ERROR);
```

### API Response Format
All endpoints return `Result<T>` with `code`, `message`, `data`
- `code: 0` for success
- Non-zero codes for errors

## Data Privacy & Security
- **Sensitive data masking**: Mask phone numbers (middle 4 digits), ID cards (middle 8)
- **Amount formatting**: 2 decimal places, thousands separator, ¥ prefix
- **Input validation**: Never trust client input, validate on backend
- **Error messages**: User-friendly messages in UI, detailed logs in backend
- **No secrets in code**: Use environment variables

## Testing Considerations
- Frontend: No test framework configured - add Vitest/Jest if needed
- Backend: Use Spring Boot Test, test controllers with MockMvc
- Test file naming: `*Test.java` in `src/test/java`
- Focus on service layer logic testing

## Key Project Files
- **Frontend config**: `frontend/vite.config.ts`, `frontend/tailwind.config.cjs`
- **Backend config**: `backend/pom.xml`, `backend/agrimatch-service/pom.xml`
- **Full rules**: `.cursorrules` (353 lines of detailed guidelines)
- **Thinking protocol**: `神级思维.md` (258 lines of AI thinking guidelines)

## Common Pitfalls to Avoid
- ❌ Using `console.log` in production code (use only for errors)
- ❌ Skipping error handling - wrap all async calls in try-catch
- ❌ Deep nesting > 3 levels - use early returns
- ❌ Magic numbers - extract to named constants
- ❌ Inline styles - use Tailwind classes
- ❌ Ignoring TypeScript errors - run `vue-tsc -b` before commit
- ❌ Premature conclusions - generate multiple hypotheses first
- ❌ Mechanical responses - show genuine discovery and natural thinking flow
- ❌ Overlooking alternatives - consider multiple solution approaches
- ❌ Unexamined assumptions - challenge your own understanding
