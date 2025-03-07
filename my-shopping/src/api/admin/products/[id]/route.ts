import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    // 添加详细的请求信息日志
    console.log('=== 开始处理商品详情请求 ===');
    console.log('完整URL:', req.url);
    console.log('请求路径:', req.path);

    // 从路径中获取ID，处理可能带有 /edit 的情况
    const pathParts = req.path?.split('/') || req.url?.split('/')
    const idPart = pathParts[pathParts.length - 1].split('?')[0]
    const id = idPart === 'edit' ? pathParts[pathParts.length - 2] : idPart

    console.log('提取的商品ID:', id);

    const response = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      console.error(`获取商品失败，状态码: ${response.status}`);
      return res.status(404).json({
        message: `Product with id "${id}" not found`,
        code: "not_found"
      });
    }

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

// 添加 POST 方法处理更新请求
// my-shopping/src/api/admin/products/[id]/route.ts
import { MedusaRequest, MedusaResponse } from "@medusajs/medusa"

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id
    const updateData = req.body
    
    console.log('请求参数:', req.params)
    console.log('请求体:', req.body)

    // 确保有状态数据
    if (!updateData || !updateData.status) {
      throw new Error('缺少状态数据')
    }

    // 调用后端API
    const response = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: updateData.status.toUpperCase()
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`更新失败: ${errorText}`)
    }

    const updatedProduct = await response.json()
    
    // 返回响应
    res.status(200)
    return res.json({
      product: {
        ...updatedProduct,
        status: updatedProduct.status.toLowerCase()
      }
    })

  } catch (error) {
    console.error('更新错误:', error)
    res.status(500)
    return res.json({
      message: "更新失败",
      error: error.message
    })
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    // 从URL中获取商品ID
    const id = req.params.id
    
    // 调用后端删除API
    const response = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('删除商品失败')
    }

    // 返回成功响应，格式参考 Medusa 文档
    return res.status(200).json({
      id: id,
      object: "product",
      deleted: true
    })

  } catch (error) {
    console.error('删除商品时发生错误:', error)
    return res.status(500).json({
      message: "删除商品失败",
      error: error.message
    })
  }
}