# 药物筛选平台 - 后端接口文档 v2.0
## React Native + Expo 前端对接

---

## 📋 基本信息

| 项目 | 内容 |
|------|------|
| 基础 URL | `http://localhost:8080` |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 跨域支持 | 已启用 CORS |
| 认证方式 | JWT Token（部分接口需要） |

**开发环境配置：**
- Android 模拟器：`http://10.0.2.2:8080`
- iOS 模拟器：`http://localhost:8080`
- 真机测试：`http://你的电脑IP:8080`

---

## 📑 目录

1. [化合物管理接口](#化合物管理接口)
2. [分子对接接口](#分子对接接口)
3. [ADMET 预测接口](#admet-预测接口)
4. [3D 可视化接口](#3d-可视化接口)
5. [综合分析接口](#综合分析接口)
6. [用户认证接口](#用户认证接口)
7. [数据类型定义](#数据类型定义)
8. [错误码说明](#错误码说明)

---

## 化合物管理接口

### 1.1 获取化合物列表

**接口：** `GET /api/compounds`

**功能：** 分页获取化合物列表，支持关键词搜索

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | number | 否 | 0 | 页码（从0开始） |
| size | number | 否 | 10 | 每页数量 |
| keyword | string | 否 | - | 搜索关键词（支持中英文名） |

**请求示例：**
```bash
# 获取第一页，每页10条
GET /api/compounds?page=0&size=10

# 搜索包含"槲皮素"的化合物
GET /api/compounds?keyword=槲皮素

# 搜索英文名
GET /api/compounds?keyword=Quercetin
```

**成功响应（200）：**
```json
{
  "content": [
    {
      "id": 1,
      "name": "槲皮素",
      "englishName": "Quercetin",
      "smiles": "C1=CC(=C(C=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O)O)O",
      "molecularWeight": 302.24,
      "logP": 1.68,
      "category": "黄酮类",
      "description": "天然黄酮类化合物",
      "heavyAtomCount": 22,
      "hbd": 5,
      "hba": 7,
      "createdAt": "2026-01-28T10:00:00"
    }
  ],
  "totalElements": 48,
  "totalPages": 5,
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  }
}
```

---

### 1.2 获取化合物详情

**接口：** `GET /api/compounds/{id}`

**功能：** 根据ID获取化合物详细信息

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | number | 是 | 化合物ID |

**请求示例：**
```bash
GET /api/compounds/1
```

**成功响应（200）：**
```json
{
  "id": 1,
  "name": "槲皮素",
  "englishName": "Quercetin",
  "smiles": "C1=CC(=C(C=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O)O)O",
  "molecularWeight": 302.24,
  "logP": 1.68,
  "category": "黄酮类",
  "description": "天然黄酮类化合物，具有抗氧化、抗炎等多种生物活性",
  "rawPdbqtContent": "REMARK  Name = Quercetin\nROOT\nATOM...",
  "heavyAtomCount": 22,
  "hbd": 5,
  "hba": 7,
  "createdAt": "2026-01-28T10:00:00"
}
```

**失败响应（404）：**
```
HTTP 404 Not Found
```

---

### 1.3 获取 Top 10 化合物

**接口：** `GET /api/compounds/top10`

**功能：** 获取结合亲和力最好的前10个化合物

**请求示例：**
```bash
GET /api/compounds/top10
```

**成功响应（200）：**
```json
[
  {
    "id": 5,
    "name": "槲皮素",
    "englishName": "Quercetin",
    "affinity": -11.2,
    "rank": 1,
    "category": "黄酮类"
  },
  {
    "id": 12,
    "name": "染料木素",
    "englishName": "Genistein",
    "affinity": -10.8,
    "rank": 2,
    "category": "异黄酮类"
  }
]
```

---

### 1.4 按分类获取化合物

**接口：** `GET /api/compounds/category/{categoryName}`

**功能：** 获取指定分类下的所有化合物

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryName | string | 是 | 分类名称（如：黄酮类、生物碱类） |

**请求示例：**
```bash
GET /api/compounds/category/黄酮类
```

**成功响应（200）：**
```json
[
  {
    "id": 1,
    "name": "槲皮素",
    "englishName": "Quercetin",
    "category": "黄酮类",
    "molecularWeight": 302.24,
    "logP": 1.68
  }
]
```

---

## 分子对接接口

### 2.1 提交对接任务

**接口：** `POST /api/docking/submit` 或 `GET /api/docking/submit`

**功能：** 提交分子对接任务（异步执行）

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compoundId | number | 是 | 化合物ID |

**请求示例：**
```bash
POST /api/docking/submit?compoundId=1
# 或
GET /api/docking/submit?compoundId=1
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "任务已提交，正在执行",
  "data": "PENDING"
}
```

**失败响应（400）：**
```json
{
  "success": false,
  "message": "提交失败: 化合物不存在",
  "data": null
}
```

**注意事项：**
- 对接任务是异步执行的，提交后立即返回
- 需要通过查询接口轮询获取结果
- 对接过程通常需要 5-30 秒

---

### 2.2 查询对接结果（按任务ID）

**接口：** `GET /api/docking/result/{taskId}`

**功能：** 根据任务ID查询对接结果

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskId | number | 是 | 对接任务ID |

**请求示例：**
```bash
GET /api/docking/result/1
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "id": 1,
    "compoundId": 1,
    "affinity": -9.5,
    "status": "completed",
    "dockedPdbqtContent": "MODEL 1\nREMARK VINA RESULT:    -9.5...",
    "similarityScore": 0.85,
    "updatedAt": "2026-01-28T20:30:15"
  }
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 对接任务ID |
| compoundId | number | 化合物ID |
| affinity | number | 结合亲和力 (kcal/mol)，**值越低越好** |
| status | string | 对接状态：`completed`（完成）、`pending`（进行中）、`failed`（失败） |
| dockedPdbqtContent | string | 对接后的配体 PDBQT 内容（用于 3D 可视化） |
| similarityScore | number | 相似度分数 (0.0-1.0) |
| updatedAt | string | 更新时间 |

---

### 2.3 查询对接结果（按化合物ID）

**接口：** `GET /api/docking/result/compound/{compoundId}`

**功能：** 根据化合物ID查询最新的对接结果

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compoundId | number | 是 | 化合物ID |

**请求示例：**
```bash
GET /api/docking/result/compound/1
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "id": 1,
    "compoundId": 1,
    "affinity": -9.5,
    "status": "completed",
    "dockedPdbqtContent": "MODEL 1\nREMARK VINA RESULT:    -9.5...",
    "similarityScore": 0.85,
    "updatedAt": "2026-01-28T20:30:15"
  }
}
```

**结果不存在响应（200）：**
```json
{
  "success": false,
  "message": "对接结果不存在",
  "data": null
}
```

---

## ADMET 预测接口

### 3.1 提交 ADMET 预测任务

**接口：** `POST /api/admet/predict` 或 `GET /api/admet/predict`

**功能：** 提交 ADMET 预测任务并立即返回结果（同步）

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compoundId | number | 是 | 化合物ID |

**请求示例：**
```bash
POST /api/admet/predict?compoundId=1
# 或
GET /api/admet/predict?compoundId=1
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "预测成功",
  "data": {
    "id": 1,
    "compoundId": 1,
    "hergToxicity": 0.15,
    "amesToxicity": 0,
    "liverToxicity": 0,
    "absorption": 0.75,
    "metabolism": 0.68,
    "updatedAt": "2026-01-28T20:35:20"
  }
}
```

**失败响应（400）：**
```json
{
  "success": false,
  "message": "预测失败: 化合物不存在",
  "data": null
}
```

**ADMET 五维指标详细说明：**

| 指标 | 字段名 | 类型 | 范围 | 说明 | 评价标准 |
|------|--------|------|------|------|----------|
| 心脏毒性 | hergToxicity | number | 0-1 | hERG 心脏毒性风险 | < 0.3 低风险<br>0.3-0.7 中等风险<br>> 0.7 高风险 |
| 致突变性 | amesToxicity | number | 0或1 | Ames 致突变性 | 0 = 阴性（安全）<br>1 = 阳性（有风险） |
| 肝毒性 | liverToxicity | number | 0或1 | 肝脏毒性 | 0 = 无毒性<br>1 = 有毒性 |
| 吸收性 | absorption | number | 0-1 | 药物吸收能力 | > 0.7 良好<br>0.5-0.7 中等<br>< 0.5 较差 |
| 代谢稳定性 | metabolism | number | 0-1 | 代谢稳定性 | > 0.6 良好<br>0.4-0.6 中等<br>< 0.4 较差 |

**五维雷达图数据处理：**

前三个指标（毒性类）需要反转，后两个指标（性能类）直接使用：

```javascript
const radarData = {
  labels: ['心脏安全性', '致突变性', '肝脏安全性', '吸收性', '代谢稳定性'],
  datasets: [{
    data: [
      (1 - admet.hergToxicity) * 100,      // 反转：值越低越好
      admet.amesToxicity === 0 ? 100 : 0,  // 反转：0=安全
      admet.liverToxicity === 0 ? 100 : 0, // 反转：0=安全
      admet.absorption * 100,              // 直接使用：值越高越好
      admet.metabolism * 100,              // 直接使用：值越高越好
    ]
  }]
};
```

---

### 3.2 查询 ADMET 结果（按化合物ID）

**接口：** `GET /api/admet/result/compound/{compoundId}`

**功能：** 根据化合物ID查询 ADMET 预测结果

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compoundId | number | 是 | 化合物ID |

**请求示例：**
```bash
GET /api/admet/result/compound/1
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "id": 1,
    "compoundId": 1,
    "hergToxicity": 0.15,
    "amesToxicity": 0,
    "liverToxicity": 0,
    "absorption": 0.75,
    "metabolism": 0.68,
    "updatedAt": "2026-01-28T20:35:20"
  }
}
```

---

### 3.3 查询 ADMET 结果（按结果ID）

**接口：** `GET /api/admet/result/{id}`

**功能：** 根据结果ID查询 ADMET 预测结果

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | number | 是 | ADMET 结果ID |

**请求示例：**
```bash
GET /api/admet/result/1
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "id": 1,
    "compoundId": 1,
    "hergToxicity": 0.15,
    "amesToxicity": 0,
    "liverToxicity": 0,
    "absorption": 0.75,
    "metabolism": 0.68,
    "updatedAt": "2026-01-28T20:35:20"
  }
}
```

---

## 3D 可视化接口

### 4.1 获取化合物 3D 结构

**接口：** `GET /api/compounds/{id}/structure`

**功能：** 获取化合物的 3D 结构数据（PDB 和 PDBQT 格式）

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | number | 是 | 化合物ID |

**请求示例：**
```bash
GET /api/compounds/1/structure
```

**成功响应（200）：**
```json
{
  "compoundId": 1,
  "compoundName": "槲皮素",
  "receptorPdb": "HEADER    PROTEIN KINASE...\nATOM      1  N   ILE A  16...",
  "ligandPdbqt": "REMARK  Name = Quercetin\nATOM      1  C   UNL     1..."
}
```

---

### 4.2 获取 3D 可视化数据（对接后）

**接口：** `GET /api/visual/{compoundId}`

**功能：** 获取用于 3D 分子查看器的完整数据（蛋白 + 对接后的配体）

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compoundId | number | 是 | 化合物ID |

**请求示例：**
```bash
GET /api/visual/1
```

**成功响应（200）：**
```json
{
  "proteinPdb": "HEADER    PROTEIN KINASE...\nATOM      1  N   ILE A  16...",
  "dockedLigandPdbqt": "MODEL 1\nREMARK VINA RESULT:    -9.5...",
  "bindingEnergy": -9.5
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| proteinPdb | string | 蛋白质 PDB 内容（1e9h - CDK2） |
| dockedLigandPdbqt | string | 对接后的配体 PDBQT 内容 |
| bindingEnergy | number | 结合能 (kcal/mol)，值越低越好 |

---

## 综合分析接口

### 5.1 计算化合物评分

**接口：** `POST /api/analysis/{compoundId}/calculate`

**功能：** 分析化合物并计算综合评分

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compoundId | number | 是 | 化合物ID |

**请求示例：**
```bash
POST /api/analysis/1/calculate
```

**成功响应（200）：**
```json
{
  "compoundId": 1,
  "totalScore": 82.5,
  "affinityScore": 85.0,
  "admetScore": 80.0,
  "druglikenessScore": 82.5,
  "goodTags": ["亲和力优秀", "ADMET 性质良好", "分子量适中"],
  "badTags": [],
  "expertAdvice": "该化合物槲皮素综合评分优秀，建议优先进行实验验证。具有良好的成药潜力。"
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| compoundId | number | 化合物ID |
| totalScore | number | 总评分（0-100） |
| affinityScore | number | 亲和力评分 |
| admetScore | number | ADMET性质评分 |
| druglikenessScore | number | 类药性评分 |
| goodTags | string[] | 优点标签 |
| badTags | string[] | 缺点标签 |
| expertAdvice | string | 专家建议 |

---

## 用户认证接口

### 6.1 用户注册

**接口：** `POST /api/auth/register`

**功能：** 注册新用户

**请求体：**
```json
{
  "phone": "13800138000",
  "email": "user@example.com",
  "password": "password123",
  "purpose": "科研"
}
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "email": "user@example.com"
  }
}
```

---

### 6.2 用户登录

**接口：** `POST /api/auth/login`

**功能：** 用户登录，获取 JWT Token

**请求体：**
```json
{
  "phone": "13800138000",
  "password": "password123"
}
```

**成功响应（200）：**
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "phone": "13800138000",
      "email": "user@example.com"
    }
  }
}
```

---

### 6.3 健康检查

**接口：** `GET /api/auth/health`

**功能：** 检查后端服务是否正常运行

**请求示例：**
```bash
GET /api/auth/health
```

**成功响应（200）：**
```json
{
  "status": "UP",
  "timestamp": "2026-01-28T20:00:00"
}
```

---

## 数据类型定义

### TypeScript 类型定义

```typescript
// ========== 基础类型 ==========

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
}

// ========== 化合物相关 ==========

export interface Compound {
  id: number;
  name: string;
  englishName: string;
  smiles: string;
  molecularWeight: number;
  logP: number;
  category: string;
  description: string;
  rawPdbqtContent?: string;
  heavyAtomCount: number;
  hbd: number;
  hba: number;
  createdAt: string;
}

export interface RankedCompound {
  id: number;
  name: string;
  englishName: string;
  affinity: number;
  rank: number;
  category: string;
}

// ========== 对接相关 ==========

export interface DockingResult {
  id: number;
  compoundId: number;
  affinity: number;              // 结合亲和力 (kcal/mol)
  status: string;                // completed, pending, failed
  dockedPdbqtContent: string;    // 对接后的 PDBQT 内容
  similarityScore: number;       // 相似度分数 (0.0-1.0)
  updatedAt: string;
}

// ========== ADMET 相关 ==========

export interface AdmetResult {
  id: number;
  compoundId: number;
  hergToxicity: number;          // hERG 心脏毒性 (0-1)
  amesToxicity: number;          // Ames 致突变性 (0 或 1)
  liverToxicity: number;         // 肝毒性 (0 或 1)
  absorption: number;            // 吸收性 (0-1)
  metabolism: number;            // 代谢稳定性 (0-1)
  updatedAt: string;
}

// ========== 3D 可视化相关 ==========

export interface StructureData {
  compoundId: number;
  compoundName: string;
  receptorPdb: string;
  ligandPdbqt: string;
}

export interface Visual3DData {
  proteinPdb: string;
  dockedLigandPdbqt: string;
  bindingEnergy: number;
}

// ========== 分析相关 ==========

export interface ScoringResult {
  compoundId: number;
  totalScore: number;
  affinityScore: number;
  admetScore: number;
  druglikenessScore: number;
  goodTags: string[];
  badTags: string[];
  expertAdvice: string;
}

// ========== 用户相关 ==========

export interface User {
  id: number;
  phone: string;
  email: string;
  purpose?: string;
  isActive: boolean;
  createdAt: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}
```

---

## 错误码说明

### HTTP 状态码

| 状态码 | 说明 | 示例场景 |
|--------|------|----------|
| 200 | 成功 | 请求成功处理 |
| 400 | 请求错误 | 参数错误、数据验证失败 |
| 401 | 未授权 | Token 无效或过期 |
| 404 | 资源不存在 | 化合物ID不存在 |
| 500 | 服务器错误 | 内部错误、数据库连接失败 |

### 业务错误码

所有接口返回的错误都包含在 `ApiResponse` 中：

```json
{
  "success": false,
  "message": "错误描述信息",
  "data": null
}
```

常见错误信息：

| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| "化合物不存在" | 提供的化合物ID无效 | 检查ID是否正确 |
| "对接结果不存在" | 该化合物还未进行对接 | 先提交对接任务 |
| "ADMET预测结果不存在" | 该化合物还未进行ADMET预测 | 先提交预测任务 |
| "任务已提交，正在执行" | 对接任务正在进行中 | 等待几秒后查询结果 |
| "预测失败" | ADMET API调用失败 | 检查网络连接，稍后重试 |

---

## 使用示例

### API 服务封装（TypeScript）

```typescript
import axios from 'axios';

const API_BASE_URL = 'http://10.0.2.2:8080';  // Android模拟器

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器（添加 Token）
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器（统一错误处理）
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期，跳转到登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const compoundAPI = {
  getList: (page = 0, size = 10, keyword = '') =>
    api.get('/api/compounds', { params: { page, size, keyword } }),
  
  getDetail: (id: number) =>
    api.get(`/api/compounds/${id}`),
  
  getTop10: () =>
    api.get('/api/compounds/top10'),
  
  getByCategory: (categoryName: string) =>
    api.get(`/api/compounds/category/${categoryName}`),
};

export const dockingAPI = {
  submit: (compoundId: number) =>
    api.post('/api/docking/submit', null, { params: { compoundId } }),
  
  getResultById: (taskId: number) =>
    api.get(`/api/docking/result/${taskId}`),
  
  getResultByCompoundId: (compoundId: number) =>
    api.get(`/api/docking/result/compound/${compoundId}`),
};

export const admetAPI = {
  predict: (compoundId: number) =>
    api.post('/api/admet/predict', null, { params: { compoundId } }),
  
  getResultByCompoundId: (compoundId: number) =>
    api.get(`/api/admet/result/compound/${compoundId}`),
};
```

### React Native 使用示例

```typescript
import { useState, useEffect } from 'react';
import { compoundAPI, dockingAPI, admetAPI } from './services/api';

// 化合物列表组件
function CompoundList() {
  const [compounds, setCompounds] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchCompounds();
  }, []);

  const fetchCompounds = async () => {
    setLoading(true);
    try {
      const res = await compoundAPI.getList(0, 10);
      setCompounds(res.data.content);
    } catch (error) {
      console.error('获取化合物列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    // 你的 UI 组件
  );
}

// 对接任务组件
function DockingTask({ compoundId }: { compoundId: number }) {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleDocking = async () => {
    setLoading(true);
    try {
      // 提交任务
      await dockingAPI.submit(compoundId);
      
      // 轮询查询结果
      const interval = setInterval(async () => {
        const res = await dockingAPI.getResultByCompoundId(compoundId);
        if (res.data.success && res.data.data?.status === 'completed') {
          setResult(res.data.data);
          setLoading(false);
          clearInterval(interval);
        }
      }, 2000);
      
      // 30秒后停止轮询
      setTimeout(() => clearInterval(interval), 30000);
    } catch (error) {
      console.error('对接失败:', error);
      setLoading(false);
    }
  };

  return (
    // 你的 UI 组件
  );
}

// ADMET 预测组件
function AdmetPrediction({ compoundId }: { compoundId: number }) {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handlePredict = async () => {
    setLoading(true);
    try {
      const res = await admetAPI.predict(compoundId);
      if (res.data.success) {
        setResult(res.data.data);
      }
    } catch (error) {
      console.error('预测失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 计算雷达图数据
  const getRadarData = () => {
    if (!result) return null;
    return {
      labels: ['心脏安全性', '致突变性', '肝脏安全性', '吸收性', '代谢稳定性'],
      datasets: [{
        data: [
          (1 - result.hergToxicity) * 100,
          result.amesToxicity === 0 ? 100 : 0,
          result.liverToxicity === 0 ? 100 : 0,
          result.absorption * 100,
          result.metabolism * 100,
        ]
      }]
    };
  };

  return (
    // 你的 UI 组件
  );
}
```

---

## 注意事项

### 通用注意事项

1. **页码从0开始**，不是从1开始
2. **搜索支持中文名和英文名**模糊匹配
3. **CORS已启用**，无需额外配置
4. **超时设置**：建议设置 30 秒超时

### 分子对接相关

1. **异步执行**：对接任务是异步的，需要轮询查询结果
2. **执行时间**：通常需要 5-30 秒
3. **状态检查**：通过 `status` 字段判断任务状态
4. **结果缓存**：对接结果会保存在数据库中

### ADMET 预测相关

1. **同步执行**：ADMET 预测是同步的，立即返回结果
2. **五维指标**：前三个需要反转，后两个直接使用
3. **结果缓存**：预测结果会保存，重复调用返回缓存

### 性能优化建议

1. **分页加载**：列表接口使用分页，避免一次加载过多数据
2. **结果缓存**：对接和ADMET结果会缓存，避免重复计算
3. **并发控制**：避免同时提交大量对接任务
4. **错误重试**：网络错误时实现指数退避重试

---

**文档版本：** v2.0.0  
**更新日期：** 2026-01-28  
**维护者：** 药物筛选平台开发团队

**更新日志：**
- v2.0.0 (2026-01-28): 完整重写文档，添加所有接口详细说明
- v1.2.0 (2026-01-28): 添加分子对接和ADMET预测接口
- v1.1.0 (2026-01-22): 添加Top10和分类查询接口
- v1.0.0 (2026-01-19): 初始版本
