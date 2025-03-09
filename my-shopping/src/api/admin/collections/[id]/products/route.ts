import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    console.log('\n=== 开始获取系列中的产品 ===');
    console.log('系列ID:', id);
    console.log('请求URL:', req.url);

    const response = await fetch(`http://localhost:8080/api/collections/${id}/products`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('获取系列详情失败');
    }

    const collection = await response.json();
    console.log('后端返回的系列数据:', collection);

    // 格式化产品数据
    const formattedProducts = (collection.products || []).map(product => ({
      id: product.id,
      title: product.title,
      subtitle: product.subtitle || '',
      description: product.description || '',
      handle: product.handle || '',
      thumbnail: product.thumbnail || '',
      status: product.status || 'draft',
      collection_id: id,
      collection: {
        id: id,
        title: collection.title
      },
      variants: [],
      options: [],
      images: product.thumbnail ? [{ url: product.thumbnail }] : [],
      created_at: product.created_at,
      updated_at: product.updated_at,
      deleted_at: null,
      metadata: {},
      profile_id: null,
      weight: product.weight || null,
      length: product.length || null,
      height: product.height || null,
      width: product.width || null
    }));

    const responseData = {
      products: formattedProducts,
      count: formattedProducts.length,
      offset: 0,
      limit: 50
    };

    console.log('发送到前端的响应:', responseData);
    res.json(responseData);

  } catch (error) {
    console.error('获取系列产品失败:', error);
    res.status(500).json({
      type: "error",
      message: "获取系列产品失败",
      code: "products.list_error"
    });
  }
}

export async function POST(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const { add } = req.body as { add: string[] };
    
    // 将字符串ID转换为数字
    const productIds = add.map(id => parseInt(id, 10));

    console.log('\n=== 开始添加产品到系列 ===');
    console.log('系列ID:', id);
    console.log('要添加的产品IDs:', productIds);

    // 1. 先添加产品
    const addResponse = await fetch(`http://localhost:8080/api/collections/${id}/products`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productIds)
    });

    if (!addResponse.ok) {
      const errorText = await addResponse.text();
      console.error('后端返回错误:', errorText);
      throw new Error(`添加产品到系列失败: ${addResponse.status}`);
    }

    // 2. 然后获取更新后的系列详情（包含产品）
    const detailResponse = await fetch(`http://localhost:8080/api/collections/${id}?includeProducts=true`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!detailResponse.ok) {
      throw new Error('获取更新后的系列详情失败');
    }

    const updatedCollection = await detailResponse.json();
    console.log('后端返回的更新后的系列:', updatedCollection);
    
    // 格式化返回数据
    const formattedCollection = {
      id: updatedCollection.id,
      title: updatedCollection.title,
      handle: updatedCollection.handle || '',
      description: updatedCollection.description || '',
      created_at: updatedCollection.createdAt,
      updated_at: updatedCollection.updatedAt,
      deleted_at: null,
      metadata: updatedCollection.metadata || {},
      products: updatedCollection.products || []
    };

    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('添加产品到系列失败:', error);
    res.status(500).json({
      type: "error",
      message: error.message || "添加产品到系列失败",
      code: "collections.error"
    });
  }
}

// DELETE 方法也需要类似的修改
export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const { remove } = req.body as { remove: string[] };
    
    // 将字符串ID转换为数字
    const productIds = remove.map(id => parseInt(id, 10));

    console.log('=== 开始从系列移除产品 ===');
    console.log('系列ID:', id);
    console.log('产品IDs:', productIds);

    const response = await fetch(`http://localhost:8080/api/collections/${id}/products`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productIds)
    });

    if (!response.ok) {
      throw new Error('从系列移除产品失败');
    }

    const updatedCollection = await response.json();

    const formattedCollection = {
      id: updatedCollection.id,
      title: updatedCollection.title,
      handle: updatedCollection.handle || '',
      description: updatedCollection.description || '',
      created_at: updatedCollection.createdAt,
      updated_at: updatedCollection.updatedAt,
      deleted_at: null,
      metadata: updatedCollection.metadata || {},
      products: updatedCollection.products || []
    };

    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('从系列移除产品失败:', error);
    res.status(500).json({
      message: "从系列移除产品失败",
      error: error.message
    });
  }
} 