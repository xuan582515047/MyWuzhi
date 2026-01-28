//==========================> 服务器配置 <==========================
/**
 * API 服务器配置
 * 注意：所有API调用已被替换为模拟数据，这些配置已被注释
 */
export const API_SERVER = {
  // 开发环境服务器地址
  DEV_HOST: '/api', // 使用相对路径，通过 Vite 代理转发
  // 生产环境服务器地址
  PROD_HOST: 'http://8.138.10.105:8081/api',
  // 根据环境获取完整服务器地址
  getBaseUrl: () => {
    return import.meta.env.DEV ?
        API_SERVER.DEV_HOST : API_SERVER.PROD_HOST;
  }
};

//==========================> 文件服务配置 <==========================
/**
 * 文件服务基础URL配置
 * 开发环境使用相对路径（空字符串），通过 Vite 代理转发
 * 生产环境使用完整的服务器地址
 */
export const FILE_BASE_URL = import.meta.env.DEV ?
    '' : 'http://8.138.10.105:8090';
