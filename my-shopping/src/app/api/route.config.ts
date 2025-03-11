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
      },
      {
        source: '/admin/inventory-items',
        destination: '/api/admin/inventory-items',
        has: [
          {
            type: 'query',
            key: 'offset'
          }
        ]
      },
      {
        source: '/admin/inventory-items/:id',
        destination: '/api/admin/inventory-items/:id'
      },
      {
        source: '/admin/inventory-items/:id',
        destination: '/api/admin/inventory-items/:id',
        has: [
          {
            type: 'method',
            key: 'POST'
          }
        ]
      },
      {
        source: '/admin/inventory-items/:id',
        destination: '/api/admin/inventory-items/:id',
        has: [
          {
            type: 'method',
            key: 'DELETE'
          }
        ]
      },
      {
        source: '/app/inventory',
        destination: '/api/admin/inventory-items',
        has: [
          {
            type: 'query',
            key: 'offset'
          }
        ]
      },
      {
        source: '/admin/inventory-items/:id/edit',
        destination: '/api/admin/inventory-items/:id/edit',
      },
      {
        source: '/app/inventory/:id/edit',
        destination: '/api/admin/inventory-items/:id/edit',
      },
      {
        source: '/admin/inventory-items/:id/attributes',
        destination: '/api/admin/inventory-items/:id/attributes',
      },
      {
        source: '/app/inventory/:id/attributes',
        destination: '/api/admin/inventory-items/:id/attributes',
      },
    ]
  },
}

export default config 