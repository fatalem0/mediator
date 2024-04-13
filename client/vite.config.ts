import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    createSvgIconsPlugin({
      iconDirs: [path.resolve(process.cwd(), 'public/icons')],
      symbolId: 'icon-[dir]-[name]',
      customDomId: "svg-sprite",
      svgoOptions: {
        plugins: [
          {
            name: "convertColors",
            params: {
              currentColor: true
            }
          }
        ]
      }
    }),
  ],
  css: {
    devSourcemap: true,
  }
})
