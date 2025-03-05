import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    // 添加详细的请求信息日志
    console.log('=== 开始处理商品详情请求 ===');
    console.log('完整URL:', req.url);
    console.log('查询参数:', req.query);

    // 从路径中获取ID
    const pathParts = req.path?.split('/') || req.url?.split('/')
    const id = pathParts[pathParts.length - 1].split('?')[0]

    const response = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    const product = await response.json()
    console.log('后端返回的原始数据:', product);

    // 修改返回格式以完全匹配 Medusa Admin UI 的期望
    const formattedProduct = {
      product: {  // 注意这里包装了一层 product
        id: product.id,
        title: product.title,
        description: product.description,
        handle: product.handle,
        thumbnail: product.thumbnail,
        status: product.status?.toLowerCase() || 'draft',
        variants: [{
          id: `${product.id}_default`,
          title: "Default Variant",
          sku: product.handle,
          ean: null,
          upc: null,
          barcode: null,
          prices: [{
            id: `price_${product.id}`,
            currency_code: "usd",
            amount: 0,
            variant_id: `${product.id}_default`
          }],
          options: [],
          inventory_quantity: 0,
          manage_inventory: true
        }],
        collection_id: null,
        collection: null,
        sales_channels: [{
          id: "default",
          name: "Default Sales Channel",
          description: null,
          is_disabled: false
        }],
        weight: product.weight,
        length: product.length,
        height: product.height,
        width: product.width,
        origin_country: product.originCountry,
        material: product.material,
        subtitle: product.subtitle,
        discountable: product.discountable,
        is_giftcard: product.isGiftcard,
        metadata: product.metadata || {},
        type: {
          id: "default",
          value: "default",
          name: "Default Type"
        },
        images: product.thumbnail ? [{
          url: product.thumbnail,
          id: `image_${product.id}`,
          created_at: product.createdAt || new Date().toISOString(),
          updated_at: product.updatedAt || new Date().toISOString()
        }] : [],
        options: [{
          id: `option_${product.id}`,
          title: "Size",
          product_id: product.id,
          created_at: product.createdAt || new Date().toISOString(),
          updated_at: product.updatedAt || new Date().toISOString(),
          values: []
        }],
        tags: [],
        created_at: product.createdAt || new Date().toISOString(),
        updated_at: product.updatedAt || new Date().toISOString(),
        profile_id: null,
        profile: null,
        categories: [],
        external_id: null,
        handle_exists: false,
        status_exists: false
      }
    }

    console.log('发送到前端的格式化数据:', formattedProduct);
    res.json(formattedProduct)

  } catch (error) {
    console.error('处理商品详情请求时发生错误:', error);
    res.status(500).json({
      message: "获取商品详情失败",
      error: error.message
    })
  }
}

export async function PUT(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    // 修改获取ID的方式，与GET方法保持一致
    const segments = req.url.split('/')
    const id = segments[segments.length - 1]
    const updateData = req.body

    console.log('Updating product with id:', id);
    console.log('Update data:', updateData);

    // 转换数据格式以匹配后端期望的格式
    const productRequest = {
      title: updateData.title,
      handle: updateData.handle,
      description: updateData.description,
      thumbnail: updateData.thumbnail,
      isGiftcard: updateData.is_giftcard,
      discountable: updateData.discountable,
      subtitle: updateData.subtitle,
      weight: updateData.weight,
      length: updateData.length,
      height: updateData.height,
      width: updateData.width,
      originCountry: updateData.origin_country,
      material: updateData.material,
      metadata: updateData.metadata
    }

    // 调用Java后端API更新商品
    const response = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productRequest)
    })

    if (!response.ok) {
      throw new Error('更新商品失败')
    }

    const updatedProduct = await response.json()

    // 转换返回的数据格式以匹配Medusa Admin的期望格式
    const formattedProduct = {
      id: updatedProduct.id,
      title: updatedProduct.title,
      description: updatedProduct.description,
      handle: updatedProduct.handle,
      thumbnail: updatedProduct.thumbnail,
      status: updatedProduct.status?.toLowerCase() || 'draft',
      variants: [{
        id: `${updatedProduct.id}_default`,
        title: "Default Variant",
        sku: updatedProduct.handle,
        ean: null,
        upc: null,
        barcode: null,
        prices: [{
          id: `price_${updatedProduct.id}`,
          currency_code: "usd",
          amount: 0,
          variant_id: `${updatedProduct.id}_default`
        }],
        options: [],
        inventory_quantity: 0,
        manage_inventory: true
      }],
      collection_id: null,
      collection: null,
      sales_channels: [{
        id: "default",
        name: "Default Sales Channel",
        description: null,
        is_disabled: false
      }],
      weight: updatedProduct.weight,
      length: updatedProduct.length,
      height: updatedProduct.height,
      width: updatedProduct.width,
      origin_country: updatedProduct.originCountry,
      material: updatedProduct.material,
      subtitle: updatedProduct.subtitle,
      discountable: updatedProduct.discountable,
      is_giftcard: updatedProduct.isGiftcard,
      metadata: updatedProduct.metadata,
      type: {
        id: "default",
        value: "default",
        name: "Default Type"
      },
      images: updatedProduct.thumbnail ? [{
        url: updatedProduct.thumbnail,
        id: `image_${updatedProduct.id}`,
        created_at: updatedProduct.createdAt || new Date().toISOString(),
        updated_at: updatedProduct.updatedAt || new Date().toISOString()
      }] : [],
      options: [{
        id: `option_${updatedProduct.id}`,
        title: "Size",
        product_id: updatedProduct.id,
        created_at: updatedProduct.createdAt || new Date().toISOString(),
        updated_at: updatedProduct.updatedAt || new Date().toISOString(),
        values: []
      }],
      tags: [],
      created_at: updatedProduct.createdAt || new Date().toISOString(),
      updated_at: updatedProduct.updatedAt || new Date().toISOString(),
      profile_id: null,
      profile: null,
      categories: [],
      external_id: null,
      handle_exists: false,
      status_exists: false
    }

    res.json(formattedProduct)

  } catch (error) {
    console.error('Error updating product:', error)
    res.status(500).json({
      message: "更新商品失败",
      error: error.message
    })
  }
} 