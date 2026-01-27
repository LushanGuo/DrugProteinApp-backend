-- 创建 compounds 表（适配PostgreSQL，支持Excel数据直接导入）
CREATE TABLE compounds (
    -- compound_id：自增主键（数据库自动生成，无需Excel提供）
    compound_id SERIAL PRIMARY KEY,
    -- smiles：存储Excel中的SMILES字符串（长度500足够容纳所有化合物的SMILES）
    smiles VARCHAR(500) NOT NULL,
    -- compound_name：存储Excel中的中文名称（traditional_name），允许为空（避免Excel中可能的空值报错）
    compound_name VARCHAR(100),
    -- create_time：自动记录数据导入时间（可选，按图片表结构保留）
    create_time TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- 创建分子对接结果表 docking_results
CREATE TABLE docking_results (
    id SERIAL PRIMARY KEY,
    compound_id VARCHAR(50) NOT NULL,
    affinity NUMERIC(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    docking_log TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建ADMET预测结果表 admet_results
CREATE TABLE admet_results (
    id SERIAL PRIMARY KEY,
    compound_id INTEGER NOT NULL,
    absorption NUMERIC(10,2),
    distribution NUMERIC(10,2),
    metabolism NUMERIC(10,2),
    excretion NUMERIC(10,2),
    toxicity NUMERIC(10,2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);