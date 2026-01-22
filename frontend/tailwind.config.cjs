/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Design System Colors (Mapped to CSS Variables)
        primary: {
          DEFAULT: 'var(--primary)',
          hover: 'var(--primary-hover)',
          light: 'var(--primary-light)',
          lighter: 'var(--primary-lighter)',
        },
        secondary: {
          DEFAULT: 'var(--secondary)',
          hover: 'var(--secondary-hover)',
          light: 'var(--secondary-light)',
        },
        surface: {
          light: 'var(--surface-light)',
          dark: 'var(--surface-dark)',
          DEFAULT: 'var(--surface-light)', // Fallback
        },
        background: {
          light: 'var(--background-light)',
          dark: 'var(--background-dark)',
          DEFAULT: 'var(--background-light)',
        },
        text: {
          primary: {
            DEFAULT: 'var(--text-primary)',
            dark: 'var(--text-primary-dark)',
          },
          secondary: {
            DEFAULT: 'var(--text-secondary)',
            dark: 'var(--text-secondary-dark)',
          },
        },
        border: {
          light: 'var(--border-light)',
          dark: 'var(--border-dark)',
          DEFAULT: 'var(--border-light)',
        },
        // Semantic Colors
        success: 'var(--success)',
        warning: 'var(--warning)',
        error: 'var(--error)',
        info: 'var(--info)',

        // Legacy Support (Optional - can be removed if full migration is done)
        brand: {
           50: '#f0f7f4',
           100: '#d9ede4',
           200: '#b8dbc8',
           300: '#9ac9ab',
           400: '#7bb78e',
           500: 'var(--primary)', // Map main brand color to primary
           600: 'var(--primary-hover)',
           700: '#5a9074',
           800: '#4a765f',
           900: '#3a5c4a',
        },
      },
      fontFamily: {
        sans: ['"Noto Sans SC"', '"Inter"', 'system-ui', 'sans-serif'], // Default sans
        display: ['"Noto Sans SC"', '"Inter"', 'system-ui', 'sans-serif'],
        body: ['"Noto Sans SC"', '"Inter"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', '"Fira Code"', 'monospace'],
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
        sm: '0 1px 3px rgba(0, 0, 0, 0.1)',
        md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
        lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
        'modern': '0 4px 12px rgba(0, 0, 0, 0.05)',
        'modern-hover': '0 6px 16px rgba(0, 0, 0, 0.1)',
      },
      spacing: {
        '18': '4.5rem',
        '112': '28rem',
        '128': '32rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'scale-in': 'scaleIn 0.15s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.9)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
      },
    },
  },
  plugins: [],
}
