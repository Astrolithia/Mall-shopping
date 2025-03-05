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