import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";
import { UpdateRequest } from "@/types/api";

export async function GET_EDIT(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    console.log('正在获取系列编辑详情，ID:', id);
    console.log('请求路径:', req.url);

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error(`系列不存在 (ID: ${id})`);
      }
      throw new Error(`获取系列详情失败: ${response.status}`);
    }

    const collection = await response.json();
    console.log('后端返回的系列数据:', collection);

    // 格式化返回数据以匹配 Medusa Admin UI 的期望格式
    const formattedCollection = {
      id: collection.id,
      title: collection.title,
      handle: collection.handle || '',
      description: collection.description || '',
      products: collection.products || [],
      created_at: collection.createdAt,
      updated_at: collection.updatedAt,
      deleted_at: null,
      metadata: collection.metadata || {}
    };

    console.log('发送到前端的响应:', { collection: formattedCollection });

    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('获取系列详情失败:', error);
    res.status(404).json({
      type: "not_found",
      message: error.message,
      code: "collection.not_found"
    });
  }
}

export async function PUT_EDIT(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const updateData = req.body;

    console.log('=== 开始处理更新系列请求 ===');
    console.log('系列ID:', id);
    console.log('更新数据:', updateData);
    console.log('请求路径:', req.url);

    const collectionRequest = {
      title: updateData.title,
      handle: updateData.handle,
      description: updateData.description,
      metadata: updateData.metadata || {}
    };

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(collectionRequest)
    });

    if (!response.ok) {
      throw new Error('更新系列失败');
    }

    const updatedCollection = await response.json();

    const formattedCollection = {
      id: updatedCollection.id,
      title: updatedCollection.title,
      handle: updatedCollection.handle || '',
      description: updatedCollection.description || '',
      products: updatedCollection.products || [],
      created_at: updatedCollection.createdAt,
      updated_at: updatedCollection.updatedAt,
      deleted_at: null,
      metadata: updatedCollection.metadata || {}
    };

    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('更新系列失败:', error);
    res.status(500).json({
      message: "更新系列失败",
      error: error.message
    });
  }
}

export async function POST(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const updateData = req.body as UpdateRequest;

    const collectionRequest = {
      title: updateData.title,
      handle: updateData.handle,
      description: updateData.description,
      metadata: updateData.metadata || {}
    };

    // ... 其余代码保持不变
  } catch (error) {
    console.error('处理请求时发生错误:', error)
    return res.status(500).json({
      message: "操作失败",
      error: error.message
    })
  }
}