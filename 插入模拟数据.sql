-- ============================================
-- 插入模拟数据脚本
-- 版本：v2.0.0
-- 日期：2026-01-23
-- 说明：为 docking_results、admet_results、analysis_reports 表插入模拟数据
-- ============================================

-- ============================================
-- 1. 插入对接结果数据（模拟）
-- ============================================

-- 为所有 48 个化合物生成对接结果
INSERT INTO docking_results (compound_id, affinity, status, similarity_score)
SELECT 
    id,
    -- 生成 -7.0 到 -11.5 之间的亲和力（值越低越好）
    -7.0 - (RANDOM() * 4.5),
    'completed',
    -- 生成 0.6 到 0.95 之间的相似度分数
    0.6 + (RANDOM() * 0.35)
FROM compounds;

-- 验证
SELECT '对接结果插入完成，共 ' || COUNT(*) || ' 条记录' AS status FROM docking_results;

-- ============================================
-- 2. 插入 ADMET 结果数据（模拟）
-- ============================================

-- 为所有 48 个化合物生成 ADMET 数据
INSERT INTO admet_results (compound_id, herg_toxicity, ames_toxicity, liver_toxicity)
SELECT 
    id,
    -- hERG 毒性：0.1 到 0.9 之间（值越低越好）
    0.1 + (RANDOM() * 0.8),
    -- Ames 致突变性：0 或 1
    CASE WHEN RANDOM() > 0.7 THEN 1 ELSE 0 END,
    -- 肝毒性：0 或 1
    CASE WHEN RANDOM() > 0.8 THEN 1 ELSE 0 END
FROM compounds;

-- 验证
SELECT 'ADMET 结果插入完成，共 ' || COUNT(*) || ' 条记录' AS status FROM admet_results;

-- ============================================
-- 3. 插入综合分析报告数据（模拟）
-- ============================================

-- 为所有 48 个化合物生成综合分析报告
INSERT INTO analysis_reports (
    compound_id, 
    total_score, 
    potency_score, 
    safety_score, 
    druglikeness_score,
    is_vetoed,
    advice_tags,
    expert_advice
)
SELECT 
    c.id,
    -- 总分：60 到 95 之间
    60 + (RANDOM() * 35),
    -- 效能评分：65 到 95 之间
    65 + (RANDOM() * 30),
    -- 安全性评分：55 到 90 之间
    55 + (RANDOM() * 35),
    -- 成药性评分：60 到 95 之间
    60 + (RANDOM() * 35),
    -- 是否被熔断：10% 概率
    CASE WHEN RANDOM() > 0.9 THEN TRUE ELSE FALSE END,
    -- 建议标签
    CASE 
        WHEN RANDOM() > 0.7 THEN ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好']
        WHEN RANDOM() > 0.4 THEN ARRAY['亲和力良好', 'ADMET性质中等', '需要优化']
        ELSE ARRAY['亲和力一般', '需要进一步研究']
    END,
    -- 专家建议
    CASE 
        WHEN RANDOM() > 0.7 THEN '该化合物' || c.name || '综合评分优秀，建议优先进行实验验证。具有良好的成药潜力。'
        WHEN RANDOM() > 0.4 THEN '该化合物' || c.name || '综合评分良好，建议进行进一步的结构优化和实验验证。'
        ELSE '该化合物' || c.name || '综合评分一般，建议进行结构修饰以提高活性和成药性。'
    END
FROM compounds c;

-- 验证
SELECT '综合分析报告插入完成，共 ' || COUNT(*) || ' 条记录' AS status FROM analysis_reports;

-- ============================================
-- 4. 数据统计
-- ============================================

SELECT '=== 数据统计 ===' AS info;

SELECT 
    '化合物总数' AS item,
    COUNT(*) AS count
FROM compounds
UNION ALL
SELECT 
    '对接结果总数',
    COUNT(*)
FROM docking_results
UNION ALL
SELECT 
    'ADMET 结果总数',
    COUNT(*)
FROM admet_results
UNION ALL
SELECT 
    '分析报告总数',
    COUNT(*)
FROM analysis_reports;

-- ============================================
-- 5. Top 10 化合物（按亲和力排序）
-- ============================================

SELECT '=== Top 10 化合物（按亲和力排序）===' AS info;

SELECT 
    ROW_NUMBER() OVER (ORDER BY dr.affinity ASC) AS rank,
    c.id,
    c.name,
    c.english_name,
    c.category,
    ROUND(dr.affinity::numeric, 2) AS affinity,
    ROUND(ar.total_score::numeric, 2) AS total_score
FROM compounds c
JOIN docking_results dr ON c.id = dr.compound_id
JOIN analysis_reports ar ON c.id = ar.compound_id
ORDER BY dr.affinity ASC
LIMIT 10;

-- ============================================
-- 6. 按分类统计
-- ============================================

SELECT '=== 按分类统计 ===' AS info;

SELECT 
    category,
    COUNT(*) AS count,
    ROUND(AVG(dr.affinity)::numeric, 2) AS avg_affinity,
    ROUND(AVG(ar.total_score)::numeric, 2) AS avg_total_score
FROM compounds c
JOIN docking_results dr ON c.id = dr.compound_id
JOIN analysis_reports ar ON c.id = ar.compound_id
GROUP BY category
ORDER BY avg_affinity ASC;

-- ============================================
-- 完成
-- ============================================

SELECT '✅ 所有模拟数据插入完成！' AS status;
