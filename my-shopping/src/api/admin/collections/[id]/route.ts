import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('获取系列详情失败');
    }

    const collection = await response.json();

    // 格式化返回数据以匹配 Medusa Admin UI 的期望格式
    const formattedCollection = {
      id: collection.id,
      title: collection.title,
      handle: collection.handle || '',
      products: collection.products || [],
      created_at: collection.createdAt,
      updated_at: collection.updatedAt,
      deleted_at: null,
      metadata: collection.metadata ? JSON.parse(collection.metadata) : {}
    };

    // 返回标准的详情页响应格式
    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('获取系列详情失败:', error);
    res.status(500).json({
      message: "获取系列详情失败",
      error: error.message
    });
  }
}

export async function PUT(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const updateData = req.body;

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    });

    if (!response.ok) {
      throw new Error('更新系列失败');
    }

    const updatedCollection = await response.json();

    // 格式化返回数据
    const formattedCollection = {
      id: updatedCollection.id,
      title: updatedCollection.title,
      handle: updatedCollection.handle,
      products: updatedCollection.products || [],
      created_at: updatedCollection.createdAt,
      updated_at: updatedCollection.updatedAt,
      metadata: updatedCollection.metadata ? JSON.parse(updatedCollection.metadata) : {}
    };

    res.json(formattedCollection);

  } catch (error) {
    console.error('更新系列失败:', error);
    res.status(500).json({
      message: "更新系列失败",
      error: error.message
    });
  }
}

export async function DELETE(
  req: MedusaRequest,
  res: MedusaResponse
) {
  try {
    const id = req.params.id;

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('删除系列失败');
    }

    res.json({
      id,
      object: "collection",
      deleted: true
    });

  } catch (error) {
    console.error('删除系列失败:', error);
    res.status(500).json({
      message: "删除系列失败",
      error: error.message
    });
  }
} 