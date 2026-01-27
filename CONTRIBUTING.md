# 贡献指南

感谢你对药物筛选平台项目的关注！

## 如何贡献

### 报告问题

如果你发现了 bug 或有功能建议：

1. 检查 [Issues](../../issues) 确保问题未被报告
2. 创建新 Issue，提供详细信息：
   - 问题描述
   - 复现步骤
   - 预期行为
   - 实际行为
   - 环境信息（操作系统、Java 版本等）

### 提交代码

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送到分支：`git push origin feature/your-feature`
5. 创建 Pull Request

### 代码规范

- 遵循 Java 编码规范
- 使用 Lombok 简化代码
- 添加必要的注释
- 确保代码通过编译
- 更新相关文档

### 提交信息规范

使用清晰的提交信息：

- `feat: 添加新功能`
- `fix: 修复 bug`
- `docs: 更新文档`
- `style: 代码格式调整`
- `refactor: 代码重构`
- `test: 添加测试`
- `chore: 构建/工具变动`

## 开发环境设置

1. 安装 JDK 17+
2. 安装 Maven 3.8+
3. 安装 PostgreSQL 15+
4. 配置 IDE（推荐 IntelliJ IDEA）
5. 安装 Lombok 插件

详见 [docs/LOMBOK_SETUP.md](docs/LOMBOK_SETUP.md)

## 测试

运行测试：

```bash
cd platform
mvn test
```

## 文档

更新文档时：

- 保持简洁明了
- 提供代码示例
- 更新 API 文档
- 检查链接有效性

## 问题讨论

有任何问题欢迎在 Issues 中讨论。

感谢你的贡献！
