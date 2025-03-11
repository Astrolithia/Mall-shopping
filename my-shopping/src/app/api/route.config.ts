import { NextConfig } from 'next'

const config: NextConfig = {
  async rewrites() {
    return [
      {
        source: '/admin/product-categories/:id',
        destination: '/api/admin/product-categories/:id',
        has: [
          {
            type: 'query',
            key: 'fields',
          }
        ]
      },
      {
        source: '/admin/product-categories/:id',
        destination: '/api/admin/product-categories/:id',
      },
      {
        source: '/app/categories/:id/edit',
        destination: '/api/admin/product-categories/:id/edit',
      },
      {
        source: '/admin/product-categories',
        destination: '/api/admin/product-categories/create',
      },
      {
        source: '/admin/:path*',
        destination: '/api/not-found',
        has: [
          {
            type: 'query',
            key: 'fields',
            value: '(?!.*product-categories).*'
          }
        ]
      }
    ]
  },
}

export default config 