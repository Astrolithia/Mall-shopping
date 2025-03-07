import { MedusaRequest, MedusaResponse } from "@medusajs/framework/http";

export async function GET(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    console.log('正在获取系列详情，ID:', id);
    console.log('请求路径:', req.url);

    // 如果是编辑页面的请求，重定向到编辑处理函数
    if (req.url.includes('/edit')) {
      return GET_EDIT(req, res);
    }

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

    // 返回标准的详情页响应格式
    res.json({
      collection: formattedCollection
    });

  } catch (error) {
    console.error('获取系列详情失败:', error);
    // 返回符合 Medusa Admin UI 期望的错误格式
    res.status(404).json({
      type: "not_found",
      message: error.message,
      code: "collection.not_found"
    });
  }
}

export async function PUT(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    // 如果是编辑页面的请求，重定向到编辑处理函数
    if (req.url.includes('/edit')) {
      return PUT_EDIT(req, res);
    }

    const id = req.params.id;
    const updateData = req.body;

    console.log('=== 开始处理更新系列请求 ===');
    console.log('系列ID:', id);
    console.log('更新数据:', updateData);
    console.log('请求路径:', req.url);

    // 构造发送到后端的数据
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

    // 格式化返回数据以匹配 Medusa Admin UI 的期望格式
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

    // 返回标准的响应格式
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

// 添加 POST 方法来处理编辑保存请求
export async function POST(
  req: MedusaRequest, 
  res: MedusaResponse
) {
  try {
    const id = req.params.id;
    const updateData = req.body;

    console.log('=== 开始处理 POST 更新系列请求 ===');
    console.log('系列ID:', id);
    console.log('更新数据:', updateData);
    console.log('请求路径:', req.url);

    // 构造发送到后端的数据
    const collectionRequest = {
      title: updateData.title,
      handle: updateData.handle,
      description: updateData.description,
      metadata: updateData.metadata || {}
    };

    const response = await fetch(`http://localhost:8080/api/collections/${id}`, {
      method: 'PUT', // 注意这里仍然使用 PUT 请求后端
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(collectionRequest)
    });

    if (!response.ok) {
      throw new Error('更新系列失败');
    }

    const updatedCollection = await response.json();

    // 格式化返回数据以匹配 Medusa Admin UI 的期望格式
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

    // 返回标准的响应格式
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