import { NextConfig } from 'next'

const config: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/admin/product-categories',
        destination: '/api/admin/product-categories',
      },
      {
        source: '/admin/product-categories/:path*',
        destination: '/api/admin/product-categories/:path*',
      },
      {
        source: '/admin/product-categories/category',
        destination: '/api/admin/product-categories',
      },
      {
        source: '/store/product-categories/:path*',
        destination: '/api/admin/product-categories/:path*',
      }
    ]
  },
}

export default config 