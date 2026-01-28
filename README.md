# 药物筛选平台

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)

基于 Spring Boot 的药物筛选平台后端系统，提供化合物数据管理、3D 分子可视化、综合评分算法等功能。

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [API 文档](docs/前端接口文档.md) • [贡献指南](CONTRIBUTING.md)

</div>

---

## 📖 目录

- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [功能特性](#-功能特性)
- [API 接口](#-api-接口)
- [文档](#-文档)
- [前端集成](#-前端集成)
- [开发工具](#-开发工具)
- [数据说明](#-数据说明)
- [故障排除](#️-故障排除)
- [贡献](#-贡献)
- [许可证](#-许可证)

---

## 🚀 技术栈

- **后端框架：** Spring Boot 3.2.0
- **数据库：** PostgreSQL 15
- **ORM：** Spring Data JPA + Hibernate
- **工具库：** Lombok
- **前端：** React Native + Expo + TypeScript

---

## 📁 项目结构

```
platform/
├── src/main/java/com/drugscreen/platform/
│   ├── controller/          # REST API 控制器
│   ├── service/             # 业务逻辑层
│   ├── repository/          # 数据访问层
│   ├── entity/              # 实体类
│   └── dto/                 # 数据传输对象
├── src/main/resources/
│   ├── 3ddata/pdb文件/      # 48个化合物 PDB 文件
│   ├── structures/          # CDK2 受体蛋白
│   ├── compounds.csv        # 化合物数据
│   └── application.properties
└── pom.xml
```

---

## ⚡ 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 15+

### 2. 数据库配置

创建数据库并执行初始化脚本：

```bash
# 创建数据库
createdb drug_screen_db

# 执行初始化脚本（包含表结构和48个化合物数据）
psql -U postgres -d drug_screen_db -f 数据库完整初始化脚本.sql
```

详细配置请查看 [数据库配置指南](docs/DATABASE_SETUP.md)。

### 3. 配置文件

编辑 `platform/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/drug_screen_db
spring.datasource.username=postgres
spring.datasource.password=123456
```

### 4. 启动应用

```bash
cd platform
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

---

## ✨ 功能特性

### 1. 用户认证

- JWT 令牌认证
- 用户注册/登录
- 手机号/邮箱登录
- 密码加密存储
- Token 自动过期
- 忘记密码（验证码重置）

### 2. 化合物管理

- 48 个天然产物数据
- 分页查询、搜索
- 按分类筛选

### 3. 3D 分子可视化

- 直接从文件读取 PDB 数据
- 支持 48 个化合物的 3D 结构
- CDK2 受体蛋白可视化

### 4. 综合评分算法

- 安全性评分（35%）
- 效能评分（45%）
- 成药性评分（20%）
- hERG 熔断机制

### 5. Top 10 排名

- 按结合亲和力排序
- 实时计算

---

## 🔌 API 接口

### 认证接口

```bash
# 用户注册
POST /api/auth/register

# 用户登录
POST /api/auth/login

# 检查手机号
GET /api/auth/check-phone/{phone}

# 检查邮箱
GET /api/auth/check-email/{email}

# 忘记密码 - 发送验证码
POST /api/auth/forgot-password/send-code

# 忘记密码 - 验证验证码
POST /api/auth/forgot-password/verify-code

# 忘记密码 - 重置密码
POST /api/auth/forgot-password/reset
```

### 化合物接口

```bash
# 获取化合物列表
GET /api/compounds?page=0&size=10&keyword=槲皮素

# 获取化合物详情
GET /api/compounds/{id}

# 按分类查询
GET /api/compounds/category/黄酮类

# Top 10 化合物
GET /api/compounds/top10
```

### 3D 可视化接口

```bash
# 获取 3D 数据
GET /api/visual/{compoundId}

# 获取结构数据
GET /api/compounds/{id}/structure
```

### 分析接口

```bash
# 计算综合评分
POST /api/analysis/{compoundId}/calculate
```

详细文档请查看 `前端接口文档.md`

---

## 📚 文档

| 文档 | 说明 |
|------|------|
| [认证接口文档](docs/认证接口文档.md) | 用户注册、登录 API |
| [忘记密码接口文档](docs/忘记密码接口文档.md) | 忘记密码功能 API |
| [前端接口文档](docs/前端接口文档.md) | 完整的 API 文档 |
| [数据库配置指南](docs/DATABASE_SETUP.md) | 数据库初始化和配置 |
| [3D可视化使用指南](docs/3D可视化使用指南.md) | 3D 功能使用说明 |
| [综合评分算法文档](docs/综合评分算法文档.md) | 评分算法详解 |
| [POSTMAN 快速开始](docs/POSTMAN_快速开始.md) | API 测试指南 |
| [Lombok 配置指南](docs/LOMBOK_SETUP.md) | IDE 配置说明 |

---

## 🎨 前端集成

### React Native 示例

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://10.0.2.2:8080', // Android 模拟器
});

// 获取化合物列表
const compounds = await api.get('/api/compounds');

// 获取 3D 数据
const visual3D = await api.get('/api/visual/1');
```

---

## 🧪 测试

### 测试脚本

项目提供了多个测试脚本，位于 `scripts/` 目录：

```bash
# Windows
scripts\test-auth-api.bat          # 测试认证接口
scripts\快速测试登录.bat            # 快速测试登录
scripts\测试忘记密码.bat            # 测试忘记密码

# Linux/Mac
chmod +x scripts/test-auth-api.sh
./scripts/test-auth-api.sh
```

### Postman 测试

导入 `药物筛选平台API.postman_collection.json` 到 Postman 进行测试。

---

## 🔧 开发工具

### IDE 配置

1. 安装 Lombok 插件
2. 启用注解处理器
3. 查看 [Lombok 配置指南](docs/LOMBOK_SETUP.md)

### API 测试

1. 导入 `药物筛选平台API.postman_collection.json`
2. 查看 [POSTMAN 快速开始](docs/POSTMAN_快速开始.md)

---

## 📊 数据说明

### 化合物数据

- **数量：** 48 个天然产物
- **来源：** `compounds.csv`
- **分类：** 黄酮类、生物碱类、蒽醌类等

### 3D 结构数据

- **格式：** PDB
- **位置：** `resources/3ddata/pdb文件/pdb文件/`
- **命名：** 根据英文名（小写）

---

## ⚠️ 注意事项

1. **端口占用：** 确保 8080 端口未被占用
2. **数据库连接：** 确保 PostgreSQL 服务运行
3. **文件编码：** PDB 文件使用 UTF-8 编码
4. **跨域配置：** 已启用 CORS，支持前端调用

---

## 🐛 故障排除

### 问题 1：启动失败

**检查：**
- PostgreSQL 是否运行
- 数据库是否创建
- 配置文件是否正确

### 问题 2：找不到 PDB 文件

**检查：**
- 文件是否存在于 `resources/3ddata/pdb文件/pdb文件/`
- 文件名是否与英文名匹配（小写）

### 问题 3：前端无法连接

**检查：**
- 后端是否启动
- 端口是否正确
- Android 模拟器使用 `10.0.2.2`

---

## 🤝 贡献

欢迎贡献代码！请查看 [贡献指南](CONTRIBUTING.md)。

### 贡献者

感谢所有为本项目做出贡献的开发者。

---

## 📝 版本历史

- **v3.0.0** (2026-01-28) - 添加忘记密码功能，修复 3D 文件名匹配问题，完成数据库字段对齐
- **v2.0.0** (2026-01-23) - 简化 3D 数据读取，直接从文件加载
- **v1.1.0** (2026-01-22) - 添加 3D 可视化和 Top 10 功能
- **v1.0.0** (2026-01-19) - 初始版本

---

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE)。

---

## 📞 联系方式

如有问题或建议，欢迎提交 [Issue](../../issues)。

---

<div align="center">

**当前版本：v3.0.0**  
**最后更新：2026-01-28**

Made with ❤️ by Drug Screening Platform Team

</div>
