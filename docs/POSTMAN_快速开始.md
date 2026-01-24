# Postman 快速开始指南 ⚡

## 🎯 最快的测试方法（2分钟）

### 方法 1：直接导入 Collection（推荐）

#### 步骤 1：打开 Postman
- 启动 Postman 应用

#### 步骤 2：导入 Collection
1. 点击左上角 **"Import"** 按钮
2. 点击 **"Upload Files"**
3. 选择项目根目录下的文件：
   ```
   药物筛选平台API.postman_collection.json
   ```
4. 点击 **"Import"**

#### 步骤 3：开始测试
1. 在左侧 Collections 中找到 **"药物筛选平台 API"**
2. 展开 **"化合物管理"** 文件夹
3. 点击任意请求（如：**"1. 获取化合物列表"**）
4. 点击右侧蓝色 **"Send"** 按钮
5. 查看下方的响应结果

✅ **完成！** 你已经成功测试了第一个 API！

---

### 方法 2：手动创建（5分钟）

如果你想学习如何手动创建请求：

#### 第 1 个测试：获取化合物列表

1. **点击** Postman 左上角 **"New"** → **"HTTP Request"**
2. **选择** 请求方法：`GET`
3. **输入** URL：
   ```
   http://localhost:8080/api/compounds
   ```
4. **点击** 蓝色 **"Send"** 按钮
5. **查看** 下方响应结果

**预期结果**：
```json
{
  "content": [
    {
      "id": 1,
      "name": "金合欢素",
      "englishName": "Acacetin",
      "molecularWeight": 284.26,
      ...
    }
  ],
  "totalElements": 48
}
```

---

## 📝 9 个测试用例

导入 Collection 后，你会看到以下 9 个预配置的测试：

| # | 测试名称 | URL | 说明 |
|---|---------|-----|------|
| 1 | 获取化合物列表 | `GET /api/compounds` | 默认分页 |
| 2 | 分页查询（每页5条） | `GET /api/compounds?page=0&size=5` | 自定义分页 |
| 3 | 分页查询（第2页） | `GET /api/compounds?page=1&size=10` | 翻页测试 |
| 4 | 搜索化合物（槲皮素） | `GET /api/compounds?keyword=槲皮素` | 中文搜索 |
| 5 | 搜索化合物（黄酮类） | `GET /api/compounds?keyword=黄酮` | 分类搜索 |
| 6 | 搜索化合物（Quercetin） | `GET /api/compounds?keyword=Quercetin` | 英文搜索 |
| 7 | 获取化合物详情（ID=1） | `GET /api/compounds/1` | 单个查询 |
| 8 | 获取化合物详情（ID=10） | `GET /api/compounds/10` | 单个查询 |
| 9 | 测试不存在的ID | `GET /api/compounds/999` | 错误处理 |

---

## 🎬 测试演示

### 测试 1：获取所有化合物
```
方法：GET
URL：http://localhost:8080/api/compounds
参数：无
预期：返回 48 个化合物（分页显示）
```

### 测试 2：搜索"槲皮素"
```
方法：GET
URL：http://localhost:8080/api/compounds
参数：
  - keyword: 槲皮素
预期：返回包含"槲皮素"的化合物
```

### 测试 3：获取详情
```
方法：GET
URL：http://localhost:8080/api/compounds/1
参数：无
预期：返回 ID=1 的化合物详细信息
```

---

## ⚠️ 常见问题

### 问题 1：显示 "Could not send request"
**原因**：应用没有启动
**解决**：
1. 打开 IntelliJ IDEA
2. 运行 `PlatformApplication`
3. 等待看到：`Tomcat started on port 8080`

### 问题 2：返回 404 Not Found
**原因**：URL 路径错误
**解决**：检查 URL 是否为：
```
http://localhost:8080/api/compounds
```
注意：
- ✅ 正确：`/api/compounds`
- ❌ 错误：`/compounds`
- ❌ 错误：`/api/compound`（少了 s）

### 问题 3：返回空数据
**原因**：数据库中没有数据
**解决**：
1. 查看应用启动日志
2. 确认看到：`成功导入 48 个化合物`

---

## 🎨 美化响应显示

### 在 Postman 中查看 JSON
1. 发送请求后，点击下方 **"Body"** 标签
2. 选择 **"Pretty"** 模式（默认）
3. 选择 **"JSON"** 格式

### 查看响应时间
- 在响应区域右上角可以看到：
  ```
  Status: 200 OK
  Time: 45 ms
  Size: 2.5 KB
  ```

---

## 📊 测试结果示例

### 成功的响应（200 OK）
```json
{
  "content": [
    {
      "id": 1,
      "name": "金合欢素",
      "englishName": "Acacetin",
      "smiles": "COC1=CC=C(C=C1)C2=CC(=O)C3=C(C=C(C=C3O2)O)O",
      "molecularWeight": 284.26,
      "logP": 2.1,
      "category": "黄酮类",
      "description": "天然产物库导入数据",
      "createdAt": "2026-01-19T19:07:01.617"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 48,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

### 失败的响应（404 Not Found）
```
Status: 404 Not Found
Body: (empty)
```

---

## 🚀 下一步

完成基础测试后，你可以：

1. **查看详细文档**：`POSTMAN_测试指南.md`
2. **学习高级功能**：环境变量、测试脚本、自动化测试
3. **导出 Collection**：分享给团队成员

---

## ✅ 测试清单

按顺序完成以下测试：

- [ ] 导入 Collection 到 Postman
- [ ] 测试：获取化合物列表
- [ ] 测试：分页查询（每页5条）
- [ ] 测试：搜索"槲皮素"
- [ ] 测试：搜索"黄酮"
- [ ] 测试：获取 ID=1 的化合物详情
- [ ] 测试：查询不存在的 ID（999）
- [ ] 验证所有响应状态码正确

---

## 💡 提示

- 使用 `Ctrl + Enter` 快捷键发送请求
- 使用 `Ctrl + S` 保存请求
- 双击请求名称可以重命名
- 右键点击请求可以复制、删除等操作

祝测试顺利！🎉
