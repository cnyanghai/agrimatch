/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          // 若竹色 (Ruozhu) - 主品牌色，供应侧
          50: '#f0f7f4',
          100: '#d9ede4',
          200: '#b8dbc8',
          300: '#9ac9ab',
          400: '#7bb78e',
          500: '#84BB9F', // 主色
          600: '#6da78b',
          700: '#5a9074',
          800: '#4a765f',
          900: '#3a5c4a',
        },
        autumn: {
          // 秋波蓝 (Qiubo Blue) - 采购侧/辅助色
          50: '#f0f7fa',
          100: '#d9ecf3',
          200: '#b8d9e8',
          300: '#9ac6dd',
          400: '#7bb3d2',
          500: '#A5CCDC', // 主色
          600: '#8fb8c9',
          700: '#7aa0b3',
          800: '#65889d',
          900: '#507087',
        },
        primary: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981',
          600: '#059669',
          700: '#047857',
          800: '#065f46',
          900: '#064e3b',
        },
        bg: {
          light: '#f6f8f6',
          'light-hover': '#e8ebe8',
          dark: '#102210',
          'dark-hover': '#1a351a',
          white: '#ffffff',
          gray: '#f9fafb',
        },
        neutral: {
          50: '#fafafa',
          100: '#f5f5f5',
          200: '#e5e5e5',
          300: '#d4d4d4',
          400: '#a3a3a3',
          500: '#737373',
          600: '#525252',
          700: '#404040',
          800: '#262626',
          900: '#171717',
        }
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
        brand: '0 4px 14px 0 rgba(132, 187, 159, 0.39)', // 增强的若竹色阴影
        autumn: '0 4px 14px 0 rgba(165, 204, 220, 0.39)', // 秋波蓝阴影
        glass: 'inset 0 0 0 1px rgba(255, 255, 255, 0.1)', // 玻璃质感边框阴影
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
