import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

// POST /admin/collections/:id/products/batch
export async function POST(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const productIds = req.body;

    console.log('=== 开始添加产品到系列 ===');
    console.log('系列ID:', id);
    console.log('产品IDs:', productIds);

    const response = await fetch(`http://localhost:8080/api/collections/${id}/products`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(productIds)
    });

    if (!response.ok) {
      throw new Error('添加产品到系列失败');
    }

    const updatedCollection = await response.json();
    
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
      message: "添加产品到系列失败",
      error: error.message
    });
  }
}

// DELETE /admin/collections/:id/products/batch
export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const productIds = req.body;

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