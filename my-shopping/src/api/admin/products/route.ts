import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    // 获取查询参数
    const { page = 0, size = 10, title, status } = req.query

    // 调用Java后端API
    const response = await fetch(`http://localhost:8080/api/products?page=${page}&size=${size}${title ? `&title=${title}` : ''}${status ? `&status=${status}` : ''}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error(`获取商品列表失败: ${response.status}`)
    }

    const data = await response.json()

    // 转换数据格式
    const formattedProducts = data.products.map(product => ({
      id: product.id,
      title: product.title,
      description: product.description,
      thumbnail: product.thumbnail,
      status: product.status?.toLowerCase() || 'draft',
      variants: [], 
      collection: "-",
      sales_channels: [{
        name: "Default Sales Channel"
      }]
    }))

    res.json({
      products: formattedProducts,
      count: formattedProducts.length,
      offset: Number(page) * Number(size),
      limit: Number(size)
    })

  } catch (error) {
    console.error('Error fetching products:', error)
    res.status(500).json({
      message: "获取商品列表失败",
      error: error.message
    })
  }
}

export async function POST(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    // 创建测试商品数据
    const testProduct = {
      title: "测试商品",
      handle: "test-product",
      description: "这是一个测试商品",
      thumbnail: "https://example.com/test.jpg",
      isGiftcard: false,
      discountable: true,
      subtitle: "测试副标题",
      weight: 1.0,
      length: 10.0,
      height: 10.0,
      width: 10.0,
      originCountry: "CN",
      material: "测试材料"
    }

    // 调用Java后端API创建商品
    const response = await fetch('http://localhost:8080/api/products', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(testProduct)
    })

    if (!response.ok) {
      throw new Error('创建商品失败')
    }

    const data = await response.json()
    res.json(data)

  } catch (error) {
    console.error('Error creating product:', error)
    res.status(500).json({
      message: "创建商品失败",
      error: error.message
    })
  }
} 