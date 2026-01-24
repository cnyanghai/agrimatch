# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AgriMatch (农汇通) is a B2B agricultural commodities trading platform connecting suppliers and buyers. It's a full-stack application with a Vue 3 frontend and Spring Boot backend.

## Build and Development Commands

### Frontend (Vue 3 + TypeScript + Vite)
```bash
cd frontend && npm run dev          # Development server (default: localhost:5173)
cd frontend && npx vue-tsc -b       # Type checking
cd frontend && npm run build        # Production build
```

### Backend (Java 17 + Spring Boot 3.2.5 + Maven)
```bash
cd backend && mvn clean install                    # Build all modules
cd backend/agrimatch-service && mvn spring-boot:run  # Run service (port 8080)
cd backend && mvn test                             # Run all tests
cd backend && mvn test -Dtest=ClassName#methodName # Run single test
```

## Architecture

### Frontend Structure (`frontend/src/`)
```
api/          # API client modules by business domain
components/   # Reusable components (cross-page)
composables/  # Vue composables (logic reuse)
views/        # Page components (route-mapped)
store/        # Pinia state management
utils/        # Pure utility functions
```

### Backend Structure (`backend/agrimatch-service/src/main/java/com/agrimatch/`)

Organized by domain modules, each containing its own controller/service/mapper/domain layers:
- `auth/` - Authentication (cookie-based session)
- `supply/` - Supply listings management
- `requirement/` - Purchase requirements
- `company/` - Company profiles
- `contract/` - Contract management
- `deal/` - Transaction records
- `post/` - Community posts/topics
- `chat/` - Real-time messaging
- `product/` - Product category tree
- `tag/` - Tag system
- `vehicle/` - Logistics vehicles
- `follow/` - User following system
- `points/` - Points/rewards system

### Database
MySQL database `nonghuitong`. Schema files located at `backend/agrimatch-service/src/main/resources/db/`. MyBatis mapper XMLs at `backend/agrimatch-service/src/main/resources/mapper/`.

## Key Technical Decisions

### API Response Format
All backend APIs return `Result<T>` wrapper with `code`, `message`, and `data` fields. Check `code === 0` for success.

### Authentication
Cookie-based session authentication. Frontend uses `/api/auth/me` to restore session state.

### Color System (Tailwind)
- `brand-*` (若竹色 Ruozhu Green): Primary brand color, supplier-side (#84BB9F)
- `autumn-*` (秋波蓝 Qiubo Blue): Secondary color, buyer-side (#A5CCDC)

### UI Conventions
- Rounded corners: `rounded-xl` (12px) for cards, `rounded-lg` (8px) for buttons/inputs
- Animation timing: 200-300ms entrance, 150-200ms exit
- Button feedback: `active:scale-95`

## Code Conventions

### Vue Components
- Always use `<script setup lang="ts">`
- Props via `defineProps<T>()` with TypeScript interfaces
- Emits via `defineEmits<T>()`
- Function naming: `handleXxx` for event handlers, `loadXxx` for data fetching

### Backend
- Controller layer: thin, validation and routing only
- Service layer: business logic with `@Transactional` for writes
- Use `@Valid` with DTO validation annotations
- Mapper methods: `selectByXxx`, `insert`, `updateById`, `deleteById`

### Error Handling
```typescript
try {
  await api()
  ElMessage.success('操作成功')
} catch (e: any) {
  console.error('Operation failed:', e)
  ElMessage.error(e.response?.data?.message || '操作失败')
}
```

## Important Files

- `.cursorrules` - Comprehensive development standards (design, code, legal, UI)
- `frontend/tailwind.config.cjs` - Custom color tokens and design system
- `frontend/src/router/index.ts` - Application routing
- `backend/pom.xml` - Maven parent with module definitions
