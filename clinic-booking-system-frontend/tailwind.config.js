/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#effcf9',
          100: '#d7f5ee',
          500: '#16a68a',
          600: '#10856f',
          700: '#126b5b',
        },
      },
    },
  },
  plugins: [],
}
