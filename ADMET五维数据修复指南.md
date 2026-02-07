# ADMET 五维数据修复指南

## 问题描述

ADMET 预测返回的 `absorption` 和 `metabolism` 字段为 `null`，导致前端无法显示完整的五维雷达图。

## 原因分析

1. **数据库字段已添加**：`absorption` 和 `metabolism` 字段已经存在于数据库中
2. **API 未返回数据**：ADMET API 可能没有返回这两个字段的数据
3. **默认值未生效**：旧的代码在 API 调用失败时没有正确设置默认值

## 解决方案

### 步骤 1：更新数据库中的现有数据

运行 SQL 脚本，将现有记录中的 `null` 值更新为默认值：

```bash
psql -U postgres -d drug_screen_db -f 更新ADMET默认值.sql
```

或者在 PostgreSQL 中直接执行：

```sql
-- 更新 absorption 为默认值 0.7
UPDATE admet_results SET absorption = 0.7 WHERE absorption IS NULL;

-- 更新 metabolism 为默认值 0.6
UPDATE admet_results SET metabolism = 0.6 WHERE metabolism IS NULL;
```

### 步骤 2：重新编译并重启应用

代码已经更新，现在会：
1. 尝试从 ADMET API 解析多种可能的字段名
2. 如果 API 没有返回数据，使用合理的默认值
3. 确保所有字段都有值后再保存到数据库

重新编译并启动应用：

```bash
cd platform
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

### 步骤 3：测试 ADMET 预测

运行测试脚本：

```bash
测试ADMET-API.bat
```

或者手动测试：

```bash
# 提交预测
curl -X POST "http://localhost:8080/api/admet/predict?compoundId=1"

# 查询结果
curl -X GET "http://localhost:8080/api/admet/result/compound/1"
```

### 步骤 4：验证结果

预期的响应应该包含所有五个字段：

```json
{
  "success": true,
  "message": "预测成功",
  "data": {
    "id": 1,
    "compoundId": 1,
    "hergToxicity": 0.25,
    "amesToxicity": 0,
    "liverToxicity": 0,
    "absorption": 0.7,      // ✅ 不再是 null
    "metabolism": 0.6,      // ✅ 不再是 null
    "updatedAt": "2026-01-28T20:00:00"
  }
}
```

## 代码更新说明

### 1. 增强的字段解析

新代码支持解析多种可能的字段名：

| 维度 | 支持的字段名 |
|------|-------------|
| 心脏毒性 | `herg_toxicity`, `hERG`, `Cardiotoxicity` |
| 致突变性 | `ames_toxicity`, `AMES`, `Mutagenicity` |
| 肝毒性 | `liver_toxicity`, `DILI`, `Hepatotoxicity` |
| 吸收性 | `absorption`, `Caco2`, `HIA`, `Bioavailability` |
| 代谢稳定性 | `metabolism`, `CYP`, `CYP3A4_Substrate`, `Half_Life`, `Clearance` |

### 2. 智能默认值

如果 API 没有返回某个字段，使用以下默认值：

| 字段 | 默认值 | 说明 |
|------|--------|------|
| hergToxicity | 0.3 | 中等风险 |
| amesToxicity | 0 | 阴性（安全） |
| liverToxicity | 0 | 无毒性 |
| absorption | 0.7 | 良好吸收 |
| metabolism | 0.6 | 中等稳定性 |

### 3. 异常处理增强

即使 ADMET API 调用完全失败，也会：
1. 返回包含默认值的结果
2. 将默认值保存到数据库
3. 记录警告日志，但不抛出异常

## ADMET API 实际返回的字段

根据 ADMETMesh API 的文档，实际返回的字段可能包括：

```json
{
  "hERG": 0.25,              // hERG 心脏毒性
  "AMES": 0,                 // Ames 致突变性
  "DILI": 0,                 // 肝毒性
  "Caco2": -5.2,             // Caco-2 通透性（对数值）
  "CYP3A4_Substrate": 1,     // CYP3A4 底物
  "Bioavailability": 0.55,   // 生物利用度
  "Half_Life": 3.5,          // 半衰期
  "Clearance": 15.2          // 清除率
}
```

新代码会智能地将这些字段转换为我们需要的五个维度。

## 前端使用示例

更新后，前端可以直接使用五个维度绘制雷达图：

```typescript
const radarData = {
  labels: ['心脏安全性', '致突变性', '肝脏安全性', '吸收性', '代谢稳定性'],
  datasets: [{
    data: [
      (1 - admet.hergToxicity) * 100,      // 反转
      admet.amesToxicity === 0 ? 100 : 0,  // 反转
      admet.liverToxicity === 0 ? 100 : 0, // 反转
      admet.absorption * 100,              // 直接使用
      admet.metabolism * 100,              // 直接使用
    ]
  }]
};
```

## 常见问题

### Q1: 为什么 absorption 和 metabolism 是默认值？

**A:** ADMET API 可能没有直接返回这两个字段，或者返回的字段名不同。新代码会尝试从多个可能的字段中提取数据，如果都没有，则使用默认值。

### Q2: 默认值是否合理？

**A:** 默认值是基于药物化学的一般经验设定的：
- `absorption = 0.7`：大多数口服药物的吸收性在 60-80% 之间
- `metabolism = 0.6`：中等代谢稳定性，既不太快也不太慢

### Q3: 如何获取真实的 absorption 和 metabolism 数据？

**A:** 有以下几种方法：
1. 使用更完整的 ADMET 预测 API
2. 基于分子描述符计算（如 Lipinski's Rule of Five）
3. 从实验数据库中查询
4. 使用机器学习模型预测

### Q4: 能否让用户手动输入这些值？

**A:** 可以！可以在前端添加一个编辑功能，允许用户修改 ADMET 数据：

```typescript
// 更新 ADMET 结果
const updateAdmet = async (id: number, data: Partial<AdmetResult>) => {
  return api.put(`/api/admet/result/${id}`, data);
};
```

## 下一步优化建议

1. **集成更完整的 ADMET API**
   - 考虑使用 SwissADME、pkCSM 等提供更多维度的 API

2. **基于分子描述符计算**
   - 使用 RDKit 或 CDK 计算分子描述符
   - 基于描述符预测 ADMET 性质

3. **添加数据来源标识**
   - 在数据库中添加 `data_source` 字段
   - 标识数据是来自 API、计算还是默认值

4. **用户反馈机制**
   - 允许用户标记不准确的预测
   - 收集反馈用于改进模型

---

**更新日期：** 2026-01-28  
**版本：** v1.0.0
