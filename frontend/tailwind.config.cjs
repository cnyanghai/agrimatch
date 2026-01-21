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
        sm: '0.5rem',
        md: '0.75rem',
        lg: '1rem',
        xl: '1.25rem',
        '2xl': '1.5rem',
        '3xl': '1.75rem',
        full: '9999px',
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        md: '0 2px 4px 0 rgba(0, 0, 0, 0.05)',
        lg: '0 4px 6px -1px rgba(0, 0, 0, 0.07)',
        xl: '0 10px 15px -3px rgba(0, 0, 0, 0.08)',
        '2xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
        brand: '0 2px 8px rgba(132, 187, 159, 0.15)', // 若竹色阴影
      },
      fontSize: {
        '3xl': '1.875rem',
        '4xl': '2.25rem',
      },
      spacing: {
        '16': '4rem',
        '18': '4.5rem',
        '20': '5rem',
        '24': '6rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out',
        'slide-up': 'slideUp 0.2s ease-out',
        'scale-in': 'scaleIn 0.15s ease-out',
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
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
      },
    },
  },
  plugins: []
}
