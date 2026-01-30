/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // 主品牌色 - 沃野绿（沃谷品牌 #2D6A4F）
        brand: {
          50: '#f0f7f4',
          100: '#daeee3',
          200: '#b5ddc6',
          300: '#82c49e',
          400: '#57b17f',
          500: '#389867',
          600: '#2D6A4F',  // 主品牌色
          700: '#1a4532',  // 导航栏主色
          800: '#113322',
          900: '#092015',
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
        // 强调色 - 赭石橙（用于价格、重要提示）
        accent: {
          50: '#fef3f0',
          100: '#fde0d6',
          200: '#fbc1ad',
          300: '#f89b80',
          400: '#E76F51',
          500: '#d4543a',
          600: '#b63f2a',
          700: '#952f1f',
          800: '#742318',
          900: '#571a12',
        },
        // 采购侧辅助色 - 麦穗金（沃谷品牌 #D4A373）
        autumn: {
          50: '#fdf8f3',
          100: '#f8edd9',
          200: '#f0d8b1',
          300: '#e6c08a',
          400: '#D4A373',
          500: '#c28a55',
          600: '#a87141',
          700: '#8a5a33',
          800: '#6c4528',
          900: '#4e311c',
        },
        // 兼容旧代码的 primary（映射到 brand）
        primary: {
          50: '#f0f7f4',
          100: '#daeee3',
          200: '#b5ddc6',
          300: '#82c49e',
          400: '#57b17f',
          500: '#389867',
          600: '#2D6A4F',
          700: '#1a4532',
          800: '#113322',
          900: '#092015',
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
        brand: '0 4px 14px 0 rgba(45, 106, 79, 0.25)',   // 沃野绿阴影
        action: '0 4px 14px 0 rgba(37, 99, 235, 0.25)',  // action 色阴影
        accent: '0 4px 14px 0 rgba(231, 111, 81, 0.25)', // 赭石橙阴影
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
