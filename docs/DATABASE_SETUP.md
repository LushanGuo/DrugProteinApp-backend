# 数据库配置指南

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE drug_screen_db;
```

### 2. 执行初始化脚本

按顺序执行以下脚本：

```bash
# 1. 创建表结构和导入 48 个化合物数据
psql -U postgres -d drug_screen_db -f 数据库完整初始化脚本.sql

# 2. 插入模拟数据（对接结果、ADMET 数据等）
psql -U postgres -d drug_screen_db -f 插入模拟数据.sql
```

## 数据库结构

### 表说明

| 表名 | 说明 | 记录数 |
|------|------|--------|
| `proteins` | 蛋白质数据（CDK2 受体） | 1 |
| `compounds` | 化合物数据 | 48 |
| `docking_results` | 对接结果 | 48 |
| `admet_results` | ADMET 预测结果 | 48 |
| `analysis_reports` | 综合分析报告 | 动态生成 |

### 化合物数据

包含 48 个天然产物：

- **黄酮类**：槲皮素、芦丁、山奈酚等
- **生物碱类**：小檗碱、咖啡因、茶碱等
- **蒽醌类**：大黄素、芦荟大黄素等
- **萜类**：青蒿素、紫杉醇等
- **其他**：姜黄素、白藜芦醇等

每个化合物包含：
- 基本信息（中文名、英文名、SMILES）
- 物理化学性质（分子量、LogP）
- 成药性参数（HBA、HBD、重原子数）
- 3D 结构（PDB 文件）

## 验证数据

### 检查化合物数据

```sql
-- 查看化合物总数
SELECT COUNT(*) FROM compounds;

-- 查看前 5 个化合物
SELECT id, name, english_name, category FROM compounds LIMIT 5;
```

### 检查对接结果

```sql
-- 查看对接结果
SELECT c.name, d.affinity, d.status 
FROM compounds c 
JOIN docking_results d ON c.id = d.compound_id 
ORDER BY d.affinity 
LIMIT 10;
```

### 检查 ADMET 数据

```sql
-- 查看 ADMET 数据
SELECT c.name, a.herg_toxicity, a.ames_toxicity 
FROM compounds c 
JOIN admet_results a ON c.id = a.compound_id 
LIMIT 10;
```

## 连接配置

在 `platform/src/main/resources/application.properties` 中配置：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/drug_screen_db
spring.datasource.username=postgres
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=update
```

## 故障排除

### 问题 1：连接失败

**错误信息：**
```
Connection refused
```

**解决方案：**
- 确保 PostgreSQL 服务正在运行
- 检查端口是否正确（默认 5432）
- 检查防火墙设置

### 问题 2：权限错误

**错误信息：**
```
permission denied for database
```

**解决方案：**
```sql
GRANT ALL PRIVILEGES ON DATABASE drug_screen_db TO postgres;
```

### 问题 3：表已存在

**错误信息：**
```
relation "compounds" already exists
```

**解决方案：**
```sql
-- 删除所有表（谨慎操作）
DROP TABLE IF EXISTS analysis_reports CASCADE;
DROP TABLE IF EXISTS admet_results CASCADE;
DROP TABLE IF EXISTS docking_results CASCADE;
DROP TABLE IF EXISTS compounds CASCADE;
DROP TABLE IF EXISTS proteins CASCADE;

-- 重新执行初始化脚本
```

## 数据备份

### 备份数据库

```bash
pg_dump -U postgres drug_screen_db > backup.sql
```

### 恢复数据库

```bash
psql -U postgres drug_screen_db < backup.sql
```

## 性能优化

### 创建索引

```sql
-- 化合物分类索引（已创建）
CREATE INDEX idx_compounds_category ON compounds(category);

-- 对接结果索引
CREATE INDEX idx_docking_compound ON docking_results(compound_id);
CREATE INDEX idx_docking_affinity ON docking_results(affinity);

-- ADMET 结果索引
CREATE INDEX idx_admet_compound ON admet_results(compound_id);
```

### 查询优化

使用分页查询避免一次性加载大量数据：

```sql
-- 分页查询
SELECT * FROM compounds 
ORDER BY id 
LIMIT 10 OFFSET 0;
```

## 相关文档

- [前端接口文档](前端接口文档.md)
- [3D 可视化使用指南](3D可视化使用指南.md)
- [综合评分算法文档](综合评分算法文档.md)
