/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // 主品牌色 - 森林绿（基于 stitch 森林绿主题 #1b5e20）
        brand: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#166534',  // 导航栏主色
          800: '#14532d',
          900: '#052e16',
        },
        // 行动色 - 蓝色（基于 stitch #1152d4）用于 CTA 按钮
        action: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',  // 主 CTA 按钮
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        // 强调色 - 橙色（用于价格、重要提示）
        accent: {
          50: '#fff7ed',
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
          800: '#9a3412',
          900: '#7c2d12',
        },
        // 采购侧辅助色 - 黛蓝（基于 stitch 黛蓝主题 #274472）
        autumn: {
          50: '#f0f4f8',
          100: '#d9e2ec',
          200: '#bcccdc',
          300: '#9fb3c8',
          400: '#829ab1',
          500: '#627d98',
          600: '#486581',
          700: '#334e68',  // 采购侧导航
          800: '#243b53',
          900: '#102a43',
        },
        // 兼容旧代码的 primary（映射到 brand）
        primary: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#166534',
          800: '#14532d',
          900: '#052e16',
        },
        // 背景色系（基于 stitch #f6f6f8）
        bg: {
          light: '#fafaf9',      // 温暖的浅灰
          'light-hover': '#f5f5f4',
          dark: '#18181b',
          'dark-hover': '#27272a',
          white: '#ffffff',
          gray: '#f4f4f5',
          muted: '#fafaf9',
        },
        // 中性色（基于 stitch 文字色 #111318）
        neutral: {
          50: '#fafafa',
          100: '#f4f4f5',
          200: '#e4e4e7',
          300: '#d4d4d8',
          400: '#a1a1aa',
          500: '#71717a',
          600: '#52525b',
          700: '#3f3f46',
          800: '#27272a',
          900: '#18181b',  // 主文字色
        },
        // 语义色
        success: {
          50: '#f0fdf4',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
        },
        warning: {
          50: '#fffbeb',
          500: '#f59e0b',
          600: '#d97706',
          700: '#b45309',
        },
        error: {
          50: '#fef2f2',
          500: '#ef4444',
          600: '#dc2626',
          700: '#b91c1c',
        },
      },
      borderRadius: {
        sm: '0.25rem',   // 4px
        md: '0.375rem',  // 6px
        lg: '0.5rem',    // 8px - 适用于输入框、按钮
        xl: '0.75rem',   // 12px - 适用于普通卡片
        '2xl': '1rem',   // 16px - 适用于大型容器
        '3xl': '1.25rem', // 20px - 适用于核心弹窗
        full: '9999px',
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        brand: '0 4px 14px 0 rgba(22, 163, 74, 0.25)',   // 更新为新 brand 色阴影
        action: '0 4px 14px 0 rgba(37, 99, 235, 0.25)',  // action 色阴影
        accent: '0 4px 14px 0 rgba(249, 115, 22, 0.25)', // accent 色阴影
        glass: 'inset 0 0 0 1px rgba(255, 255, 255, 0.1)',
      },
      fontFamily: {
        display: ['Inter', 'Noto Sans SC', 'sans-serif'],
        body: ['Inter', 'Noto Sans SC', 'sans-serif'],
      },
      fontSize: {
        '3xl': ['1.875rem', { lineHeight: '2.25rem', letterSpacing: '-0.02em' }],
        '4xl': ['2.25rem', { lineHeight: '2.5rem', letterSpacing: '-0.02em' }],
        '5xl': ['3rem', { lineHeight: '1', letterSpacing: '-0.02em' }],
      },
      spacing: {
        '16': '4rem',
        '18': '4.5rem',
        '20': '5rem',
        '24': '6rem',
        '28': '7rem',
        '32': '8rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.4s ease-out',
        'slide-down': 'slideDown 0.4s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'float': 'float 3s ease-in-out infinite',
        'ping-slow': 'ping 2s cubic-bezier(0, 0, 0.2, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        }
      },
    },
  },
  plugins: []
}
