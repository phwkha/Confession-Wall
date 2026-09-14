/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fff1f2',
          100: '#ffe4e6',
          200: '#fecdd3',
          300: '#fda4af',
          400: '#fb7185',
          500: '#f43f5e',
          600: '#e11d48',
          700: '#be123c',
          800: '#9f1239',
          900: '#881337',
          950: '#4c0519',
        },
        gothic: {
          950: '#020617',
          900: '#090d16',
          800: '#0f172a',
          ruby: '#e11d48',
          crimson: '#9f1239',
          blood: '#881337',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        'soft': '0 4px 20px -2px rgba(0, 0, 0, 0.4)',
        'card': '0 4px 20px -2px rgba(0, 0, 0, 0.5), 0 0 15px 1px rgba(225, 29, 72, 0.08)',
        'card-hover': '0 20px 30px -10px rgba(0, 0, 0, 0.7), 0 0 25px 2px rgba(225, 29, 72, 0.25)',
        'ruby-glow': '0 0 20px rgba(225, 29, 72, 0.35)',
        'ruby-glow-lg': '0 0 35px rgba(225, 29, 72, 0.5)',
      },
    },
  },
  plugins: [],
};
