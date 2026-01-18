/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981', // 主色
          600: '#059669', // 主按钮
          700: '#047857',
          800: '#065f46',
          900: '#064e3b',
        },
        bg: {
          light: '#f6f8f6', // 柔和浅灰绿背景
          'light-hover': '#e8ebe8',
          dark: '#102210',
          'dark-hover': '#1a351a',
          white: '#ffffff',
          gray: '#f9fafb',
        },
        neutral: {
          50: '#fafafa',
          100: '#f5f5f5',
          200: '#e5e5e5', // 边框
          300: '#d4d4d4',
          400: '#a3a3a3', // 辅助文字
          500: '#737373',
          600: '#525252', // 正文文字
          700: '#404040',
          800: '#262626',
          900: '#171717',
        }
      },
      borderRadius: {
        sm: '0.5rem',   // 8px
        md: '0.75rem',  // 12px
        lg: '1rem',     // 16px
        xl: '1.25rem',  // 20px
        '2xl': '1.5rem', // 24px
        '3xl': '1.75rem', // 28px
        full: '9999px',
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05), 0 1px 3px 0 rgba(0, 0, 0, 0.1)',
        md: '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)',
        lg: '0 10px 15px -3px rgba(0, 0, 0, 0.07), 0 4px 6px -2px rgba(0, 0, 0, 0.04)',
        xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05)',
        '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        primary: '0 4px 14px rgba(16, 185, 129, 0.25)',
        glow: '0 0 20px rgba(16, 185, 129, 0.15)',
        inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
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
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
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
