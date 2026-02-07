# Git 提交指南

## 项目结构说明

```
New-Protein-Splicing/          # 根目录（Git 仓库）
├── platform/                  # Maven 项目（主要代码）
│   ├── pom.xml               # Maven 配置
│   ├── src/                  # 源代码
│   └── mvnw.cmd              # Maven wrapper
├── docking/                   # 分子对接文件
├── docs/                      # 项目文档
├── scripts/                   # 工具脚本
└── *.sql                      # 数据库脚本
```

**为什么要在 platform 里运行？**
- 根目录是项目容器，包含多个模块（代码、文档、脚本、数据）
- `platform/` 是 Maven 项目，包含 `pom.xml` 和源代码
- IDEA 需要打开 `platform/` 文件夹才能识别为 Maven 项目

## 清理步骤

### 1. 删除对接输出文件（保留输入文件）
```bash
# 删除所有 *_out.pdbqt 文件（这些是对接结果，可以重新生成）
cd docking
del *_out.pdbqt
cd ..
```

### 2. 删除临时测试文件
```bash
# 删除测试 token 文件
del token.json

# 可选：删除一些重复的测试脚本（保留主要的）
# 保留：启动应用.bat, 快速测试ADMET.bat, 测试综合评分.bat
# 删除：test-backend.bat, test-merged-apis.bat, check-platform-status.bat
```

### 3. 整理文档
建议保留的文档：
- ✅ `README.md` - 项目主文档
- ✅ `CONTRIBUTING.md` - 贡献指南
- ✅ `项目结构说明.md` - 项目结构
- ✅ `IDEA配置指南.md` - IDEA 配置
- ✅ `综合评分算法说明.md` - 算法文档
- ✅ `ADMET五维数据修复指南.md` - 修复指南
- ✅ `docs/API接口文档-v2.0.md` - API 文档

可以删除的文档（临时/重复）：
- ❌ `后端404问题诊断.md` - 临时问题诊断
- ❌ `修复IDEA-JDK配置.md` - 已整合到 IDEA配置指南
- ❌ `修复对接和可视化问题.md` - 临时问题
- ❌ `数据库字段对照修改说明.md` - 临时说明
- ❌ `模块合并说明.md` - 临时说明
- ❌ `项目清理说明.md` - 临时说明
- ❌ `综合评分更新总结.md` - 临时总结
- ❌ `综合评分快速指南.md` - 已整合到算法说明

### 4. 整理脚本
建议保留的脚本：
- ✅ `启动应用.bat` - 主启动脚本
- ✅ `快速测试ADMET.bat` - ADMET 测试
- ✅ `测试综合评分.bat` - 评分测试
- ✅ `修复Maven依赖.bat` - Maven 修复
- ✅ `运行ADMET修复.bat` - ADMET 修复

可以删除的脚本（重复/临时）：
- ❌ `启动应用-不用IDEA.bat` - 与主启动脚本重复
- ❌ `快速启动应用.bat` - 与主启动脚本重复
- ❌ `重启应用并测试.bat` - 临时脚本
- ❌ `测试综合评分-简化版.bat` - 重复
- ❌ `测试ADMET-API.bat` - 与快速测试重复
- ❌ `test-backend.bat` - 临时测试
- ❌ `test-merged-apis.bat` - 临时测试
- ❌ `check-platform-status.bat` - 临时检查

### 5. 整理数据库脚本
建议保留的脚本：
- ✅ `数据库完整初始化脚本.sql` - 完整初始化
- ✅ `插入模拟数据.sql` - 测试数据

可以删除的脚本（临时/重复）：
- ❌ `手动更新ADMET数据.sql` - 临时修复
- ❌ `更新ADMET五维数据.sql` - 临时更新
- ❌ `更新ADMET默认值.sql` - 临时更新
- ❌ `数据库迁移脚本-从旧表到新表.sql` - 已完成迁移
- ❌ `数据库重建脚本-删除旧表创建新表.sql` - 临时脚本
- ❌ `直接更新数据库.ps1` - 临时脚本

## Git 提交步骤

### 步骤 1：查看当前状态
```bash
git status
```

### 步骤 2：添加所有修改的文件
```bash
# 添加修改的代码文件
git add platform/

# 添加新的文档
git add docs/API接口文档-v2.0.md
git add ADMET五维数据修复指南.md
git add IDEA配置指南.md
git add 综合评分算法说明.md
git add 项目结构说明.md

# 添加主要脚本
git add 启动应用.bat
git add 快速测试ADMET.bat
git add 测试综合评分.bat
git add 修复Maven依赖.bat
git add 运行ADMET修复.bat

# 添加数据库脚本
git add 数据库完整初始化脚本.sql
git add 插入模拟数据.sql

# 添加 .gitignore 更新
git add .gitignore
```

### 步骤 3：删除旧文件
```bash
# Git 会自动检测到这些文件被删除
git add -u
```

### 步骤 4：提交
```bash
git commit -m "重构项目：整合模块、更新 ADMET 五维数据、完善综合评分算法

主要更新：
- 将项目整合到 platform 模块（Maven）
- 升级 Java 版本到 21
- 完善 ADMET 五维数据支持（hERG, Ames, Liver, Absorption, Metabolism）
- 更新综合评分算法使用真实 Vina 和 ADMET 数据
- 完善 API 接口文档
- 添加 IDEA 配置指南和项目结构说明
- 清理临时文件和重复脚本"
```

### 步骤 5：推送到远程仓库
```bash
git push origin main
```

## 快速清理命令（可选）

如果你想一键清理所有临时文件，运行：

```bash
# 删除对接输出
del docking\*_out.pdbqt

# 删除临时文档
del 后端404问题诊断.md
del 修复IDEA-JDK配置.md
del 修复对接和可视化问题.md
del 数据库字段对照修改说明.md
del 模块合并说明.md
del 项目清理说明.md
del 综合评分更新总结.md
del 综合评分快速指南.md
del 添加ADMET五维数据-完整步骤.md

# 删除临时脚本
del 启动应用-不用IDEA.bat
del 快速启动应用.bat
del 重启应用并测试.bat
del 测试综合评分-简化版.bat
del 测试ADMET-API.bat
del test-backend.bat
del test-merged-apis.bat
del check-platform-status.bat

# 删除临时数据库脚本
del 手动更新ADMET数据.sql
del 更新ADMET五维数据.sql
del 更新ADMET默认值.sql
del 数据库迁移脚本-从旧表到新表.sql
del 数据库重建脚本-删除旧表创建新表.sql
del 直接更新数据库.ps1

# 删除临时文件
del token.json
```

## 注意事项

1. **不要提交敏感信息**：
   - 数据库密码已在 `.gitignore` 中排除
   - `application-local.properties` 不会被提交

2. **不要提交编译产物**：
   - `target/` 目录已在 `.gitignore` 中排除
   - `.idea/` 目录已在 `.gitignore` 中排除

3. **保留重要文档**：
   - API 接口文档
   - 算法说明文档
   - 配置指南

4. **定期清理**：
   - 对接输出文件（可重新生成）
   - 临时测试脚本
   - 过时的文档

## 提交后验证

```bash
# 查看提交历史
git log --oneline -5

# 查看远程仓库状态
git remote -v

# 确认所有更改已推送
git status
```
