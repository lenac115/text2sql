/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fff1f2',
          500: '#f43f5e',
          600: '#e11d48',
          700: '#be123c',
        },
      },
    },
  },
  plugins: [],
};