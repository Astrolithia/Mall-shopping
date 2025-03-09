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
    console.log('=== 开始处理创建商品请求 ===');
    console.log('请求体:', req.body);

    // 从请求体中获取商品数据
    const {
      title,
      subtitle,
      description,
      handle,
      status = 'draft',
      thumbnail,
      images = [],
      weight,
      length,
      height,
      width,
      origin_country,
      material,
      is_giftcard = false,
      discountable = true,
      options = [],
      variants = [],
      metadata = {}
    } = req.body;

    // 构造发送到后端的数据
    const productRequest = {
      title,
      subtitle,
      description,
      handle,
      status: status.toUpperCase(), // 转换为大写以匹配后端枚举
      thumbnail,
      isGiftcard: is_giftcard,
      discountable,
      weight: parseFloat(weight) || 0,
      length: parseFloat(length) || 0,
      height: parseFloat(height) || 0,
      width: parseFloat(width) || 0,
      originCountry: origin_country,
      material,
      metadata,
      // 如果有图片,使用第一张作为缩略图
      thumbnail: images.length > 0 ? images[0].url : thumbnail
    };

    console.log('发送到后端的数据:', productRequest);

    // 调用后端 API 创建商品
    const response = await fetch('http://localhost:8080/api/products', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productRequest)
    });

    if (!response.ok) {
      throw new Error('创建商品失败');
    }

    const createdProduct = await response.json();

    // 构造返回给前端的数据格式
    const formattedProduct = {
      product: {
        id: createdProduct.id,
        title: createdProduct.title,
        description: createdProduct.description,
        handle: createdProduct.handle,
        thumbnail: createdProduct.thumbnail,
        status: createdProduct.status?.toLowerCase() || 'draft',
        variants: variants.map(variant => ({
          id: `${createdProduct.id}_${variant.sku || 'default'}`,
          title: variant.title,
          sku: variant.sku,
          prices: variant.prices || [{
            currency_code: "usd",
            amount: 0
          }],
          options: variant.options || [],
          inventory_quantity: variant.inventory_quantity || 0,
          manage_inventory: true
        })),
        options: options.map((option, index) => ({
          id: `option_${createdProduct.id}_${index}`,
          title: option.title,
          values: option.values || []
        })),
        images: images.map((image, index) => ({
          url: image.url,
          id: `image_${createdProduct.id}_${index}`
        })),
        collection_id: null,
        collection: null,
        sales_channels: [{
          id: "default",
          name: "Default Sales Channel"
        }],
        weight: createdProduct.weight,
        length: createdProduct.length,
        height: createdProduct.height,
        width: createdProduct.width,
        origin_country: createdProduct.originCountry,
        material: createdProduct.material,
        subtitle: createdProduct.subtitle,
        discountable: createdProduct.discountable,
        is_giftcard: createdProduct.isGiftcard,
        metadata: createdProduct.metadata,
        type: {
          id: "default",
          value: "default",
          name: "Default Type"
        },
        tags: [],
        created_at: createdProduct.createdAt || new Date().toISOString(),
        updated_at: createdProduct.updatedAt || new Date().toISOString()
      }
    };

    console.log('发送到前端的响应:', formattedProduct);
    res.status(201).json(formattedProduct);

  } catch (error) {
    console.error('创建商品时发生错误:', error);
    res.status(500).json({
      message: "创建商品失败",
      error: error.message
    });
  }

  
} 