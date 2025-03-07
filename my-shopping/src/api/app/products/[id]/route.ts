// 导入并重新导出 admin 路由的处理函数
import { GET, POST, PUT } from '../../../admin/products/[id]/route'

// 添加调试日志
console.log('正在加载 app/products/[id] 路由');

// 重新导出方法
export { GET, POST, PUT } 