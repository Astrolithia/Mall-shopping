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
export async function POST(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    console.log('=== 开始处理更新商品请求(POST) ===');
    console.log('完整URL:', req.url);
    console.log('请求路径:', req.path);
    console.log('请求体:', req.body);

    // 从URL中提取ID
    const pathParts = req.path?.split('/') || req.url?.split('/')
    const idPart = pathParts[pathParts.length - 1].split('?')[0]
    const id = idPart === 'edit' ? pathParts[pathParts.length - 2] : idPart

    console.log('提取的商品ID:', id);

    // 验证商品是否存在
    const checkResponse = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!checkResponse.ok) {
      console.error(`商品不存在，ID: ${id}`);
      return res.status(404).json({
        message: `Product with id "${id}" not found`,
        code: "not_found"
      });
    }

    const updateData = req.body;

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

    console.log('发送到后端的更新数据:', productRequest);

    // 调用后端API更新商品
    const response = await fetch(`http://localhost:8080/api/products/${id}`, {
      method: 'PUT',  // 后端仍然使用 PUT
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productRequest)
    })

    if (!response.ok) {
      console.error('更新失败，状态码:', response.status);
      throw new Error('更新商品失败')
    }

    const updatedProduct = await response.json()
    console.log('后端返回的更新数据:', updatedProduct);

    // 格式化返回数据以匹配 Medusa Admin UI 的期望
    const formattedProduct = {
      product: {
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
        metadata: updatedProduct.metadata || {},
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
    }

    console.log('发送到前端的格式化响应:', formattedProduct);
    res.json(formattedProduct)

  } catch (error) {
    console.error('更新商品时发生错误:', error);
    res.status(500).json({
      message: "更新商品失败",
      error: error.message
    })
  }
}

// 添加 PUT 方法的定义
export async function PUT(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    console.log('=== 开始处理更新商品请求(PUT) ===');
    console.log('完整URL:', req.url);
    console.log('请求路径:', req.path);
    console.log('请求体:', req.body);

    const pathParts = req.path?.split('/') || req.url?.split('/')
    const id = pathParts[pathParts.length - 1].split('?')[0]
    
    console.log('提取的商品ID:', id);

    const updateData = req.body;
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
    
    // 返回格式化的响应
    const formattedProduct = {
      product: {
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
        metadata: updatedProduct.metadata || {},
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
    }

    res.json(formattedProduct)

  } catch (error) {
    console.error('更新商品时发生错误:', error);
    res.status(500).json({
      message: "更新商品失败",
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