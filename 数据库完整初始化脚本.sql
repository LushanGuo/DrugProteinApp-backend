-- ============================================
-- 药物筛选平台 - 完整数据库初始化脚本
-- 版本：v3.0.0
-- 日期：2026-01-23
-- 说明：删除旧表、创建新表、插入48个化合物及模拟数据
-- ============================================

-- ============================================
-- 第一步：删除旧表
-- ============================================
DROP TABLE IF EXISTS analysis_reports CASCADE;
DROP TABLE IF EXISTS admet_results CASCADE;
DROP TABLE IF EXISTS docking_results CASCADE;
DROP TABLE IF EXISTS compounds CASCADE;
DROP TABLE IF EXISTS proteins CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- 第二步：创建表结构
-- ============================================

-- 1. 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    purpose VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.phone IS '手机号';
COMMENT ON COLUMN users.email IS '邮箱';
COMMENT ON COLUMN users.password IS '加密后的密码';
COMMENT ON COLUMN users.purpose IS '使用目的';
COMMENT ON COLUMN users.is_active IS '账户是否激活';

-- 2. 蛋白质表 (存受体 1e9h)
CREATE TABLE proteins (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    pdb_content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE proteins IS '蛋白质数据表';
COMMENT ON COLUMN proteins.name IS '蛋白名称，如 1e9h';
COMMENT ON COLUMN proteins.pdb_content IS 'PDB 文件内容';

-- 3. 化合物表 (存配体)
CREATE TABLE compounds (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    english_name VARCHAR(255),
    smiles TEXT NOT NULL,
    molecular_weight DOUBLE PRECISION,
    log_p DOUBLE PRECISION,
    category VARCHAR(100),
    description TEXT,
    raw_pdbqt_content TEXT,
    heavy_atom_count INT DEFAULT 0,
    hbd INT DEFAULT 0,
    hba INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_compounds_category ON compounds(category);
CREATE INDEX idx_compounds_english_name ON compounds(english_name);

COMMENT ON TABLE compounds IS '化合物数据表';
COMMENT ON COLUMN compounds.raw_pdbqt_content IS '原始 PDBQT 内容';
COMMENT ON COLUMN compounds.heavy_atom_count IS '重原子数（非氢原子）';
COMMENT ON COLUMN compounds.hbd IS '氢键供体数';
COMMENT ON COLUMN compounds.hba IS '氢键受体数';

-- 3. 对接结果表
CREATE TABLE docking_results (
    id BIGSERIAL PRIMARY KEY,
    compound_id BIGINT REFERENCES compounds(id) ON DELETE CASCADE,
    affinity DOUBLE PRECISION,
    status VARCHAR(50),
    docked_pdbqt_content TEXT,
    similarity_score DOUBLE PRECISION DEFAULT 0.0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_docking_compound ON docking_results(compound_id);
CREATE INDEX idx_docking_affinity ON docking_results(affinity);

COMMENT ON TABLE docking_results IS '分子对接结果表';
COMMENT ON COLUMN docking_results.affinity IS '结合亲和力 (kcal/mol)，值越低越好';

-- 4. ADMET 结果表
CREATE TABLE admet_results (
    id BIGSERIAL PRIMARY KEY,
    compound_id BIGINT REFERENCES compounds(id) ON DELETE CASCADE,
    herg_toxicity DOUBLE PRECISION,
    ames_toxicity INT,
    liver_toxicity INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admet_compound ON admet_results(compound_id);

COMMENT ON TABLE admet_results IS 'ADMET 性质预测结果表';
COMMENT ON COLUMN admet_results.herg_toxicity IS 'hERG 心脏毒性风险';
COMMENT ON COLUMN admet_results.ames_toxicity IS 'Ames 致突变性 (0=阴性, 1=阳性)';
COMMENT ON COLUMN admet_results.liver_toxicity IS '肝毒性 (0=无, 1=有)';

-- 5. 综合分析报告表
CREATE TABLE analysis_reports (
    id BIGSERIAL PRIMARY KEY,
    compound_id BIGINT REFERENCES compounds(id) ON DELETE CASCADE,
    total_score DOUBLE PRECISION,
    potency_score DOUBLE PRECISION,
    safety_score DOUBLE PRECISION,
    druglikeness_score DOUBLE PRECISION,
    is_vetoed BOOLEAN DEFAULT FALSE,
    advice_tags TEXT[],
    expert_advice TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_analysis_compound ON analysis_reports(compound_id);
CREATE INDEX idx_analysis_score ON analysis_reports(total_score DESC);

COMMENT ON TABLE analysis_reports IS '综合分析报告表';
COMMENT ON COLUMN analysis_reports.is_vetoed IS '是否被熔断机制否决';
COMMENT ON COLUMN analysis_reports.advice_tags IS '建议标签数组';

-- ============================================
-- 第三步：插入化合物数据（48个）
-- ============================================

INSERT INTO compounds (name, english_name, molecular_weight, log_p, category, smiles, heavy_atom_count, hbd, hba, description) VALUES
('金合欢素', 'Acacetin', 284.26, 2.1, '黄酮类', 'COC1=CC=C(C=C1)C2=CC(=O)C3=C(C=C(C=C3O2)O)O', 21, 2, 5, '天然黄酮类化合物，具有抗炎和抗氧化活性'),
('茜素', 'Alizarin', 240.21, 3.2, '醌类', 'C1=CC=C2C(=C1)C(=O)C3=C(C2=O)C(=C(C=C3)O)O', 18, 2, 4, '天然蒽醌类染料，具有抗菌活性'),
('芦荟大黄素', 'Aloe-emodin', 270.24, 1.8, '蒽醌类', 'C1=CC=C2C(=C1)C(=O)C3=C(C2=O)C(=CC=C3CO)O', 20, 3, 5, '芦荟中的活性成分，具有抗癌潜力'),
('穿心莲内酯', 'Andrographolide', 350.4, 2.2, '二萜内酯类', 'C[C@@]12CC[C@H]([C@H]([C@H]1CCC(=C)[C@@H]2C/C=C(/CO)\C3=CC(=O)OC3)O)O', 25, 3, 5, '穿心莲的主要活性成分，具有抗炎和免疫调节作用'),
('芹菜素', 'Apigenin', 270.24, 1.7, '黄酮类', 'C1=CC(=CC=C1C2=CC(=O)C3=C(C=C(C=C3O2)O)O)O', 20, 3, 5, '广泛存在于植物中的黄酮类化合物'),
('牛蒡子苷', 'Arctiin', 534.6, 1.8, '木脂素类', 'COC1=C(C=C(C=C1)C[C@H]2COC(=O)[C@@H]2CC3=CC(=C(C=C3)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)CO)O)O)O)OC)OC', 38, 6, 11, '牛蒡子中的木脂素苷类化合物'),
('青蒿素', 'Artemisinin', 282.33, 2.8, '倍半萜内酯类', 'C[C@@H]1CC[C@H]2[C@@H](C(=O)O[C@@H]3[C@H]2[C@@]1(OO3)C)C', 20, 0, 5, '抗疟疾药物，诺贝尔奖获奖成果'),
('细辛脂素', 'Asarinin', 354.4, 2.7, '木脂素类', 'C1[C@H]2[C@H](CO[C@@H]2C3=CC4=C(C=C3)OCO4)[C@@H](O1)C5=CC6=C(C=C5)OCO6', 26, 0, 6, '细辛中的木脂素类化合物'),
('黄芩素', 'Baicalein', 270.24, 1.7, '黄酮类', 'C1=CC=C(C=C1)C2=CC(=O)C3=C(O2)C(=C(C=C3O)O)O', 20, 3, 5, '黄芩的主要活性成分'),
('黄芩苷', 'Baicalin', 446.4, 1.1, '黄酮类', 'C1=CC=C(C=C1)C2=CC(=O)C3=C(O2)C(=C(C=C3O)O)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)C(=O)O)O)O)O', 32, 7, 11, '黄芩苷是黄芩素的糖苷形式'),
('小檗碱', 'Berberine', 336.4, 3.6, '生物碱类', 'COC1=C(C2=C[N+]3=C(C=C2C=C1)C4=CC5=C(C=C4CC3)OCO5)OC', 25, 0, 4, '黄连的主要活性成分，具有抗菌作用'),
('鹰嘴豆芽素A', 'Biochanin A', 284.26, 3.0, '异黄酮类', 'COC1=CC=C(C=C1)C2=COC3=CC(O)=CC(O)=C3C2=O', 21, 2, 5, '异黄酮类植物雌激素'),
('大黄酚', 'Chrysophanol', 254.24, 3.5, '蒽醌类', 'CC1=CC2=C(C(=C1)O)C(=O)C3=C(C2=O)C=CC=C3O', 19, 2, 4, '大黄中的蒽醌类化合物'),
('姜黄素', 'Curcumin', 368.4, 3.2, '多酚类', 'COC1=C(C=CC(=C1)/C=C/C(=O)CC(=O)/C=C/C2=CC(=C(C=C2)O)OC)O', 27, 2, 6, '姜黄的主要活性成分，具有抗炎和抗氧化作用'),
('大豆苷元', 'Daidzein', 254.24, 2.5, '异黄酮类', 'C1=CC(=CC=C1C2=COC3=C(C2=O)C=CC(=C3)O)O', 19, 2, 4, '大豆中的异黄酮类化合物'),
('大豆苷', 'Daidzin', 416.4, 0.7, '异黄酮类', 'C1=CC(=CC=C1C2=COC3=C(C2=O)C=CC(=C3)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)CO)O)O)O)O', 30, 6, 9, '大豆苷元的糖苷形式'),
('大黄素', 'Emodin', 270.24, 2.7, '蒽醌类', 'CC1=CC2=C(C(=C1)O)C(=O)C3=C(C2=O)C(=CC=C3O)O', 20, 3, 5, '大黄中的主要蒽醌类化合物'),
('七叶苷', 'Esculin', 340.28, -0.6, '香豆素类', 'C1=CC(=O)OC2=CC(=C(C=C21)O[C@H]3[C@@H]([C@H]([C@@H]([C@H](O3)CO)O)O)O)O', 24, 6, 9, '七叶树中的香豆素苷'),
('杜鹃素', 'Farrerol', 300.3, 3.1, '黄酮类', 'CC1=C(C=C2C(=C1O)C(=O)C[C@H](O2)C3=CC=C(C=C3)O)C', 22, 2, 4, '杜鹃花中的黄酮类化合物'),
('芒柄花素', 'Formononetin', 268.26, 2.8, '异黄酮类', 'COC1=CC=C(C=C1)C2=COC3=C(C2=O)C=CC(=C3)O', 20, 1, 4, '红车轴草中的异黄酮'),
('连翘苷', 'Forsythin', 534.6, 0.0, '木脂素类', 'COC1=C(C=C(C=C1)[C@H]2[C@H]3CO[C@H]([C@H]3CO2)C4=CC(=C(C=C4)O[C@H]5[C@@H]([C@H]([C@@H]([C@H](O5)CO)O)O)O)OC)OC', 38, 6, 11, '连翘中的木脂素苷'),
('橙皮苷', 'Hesperidin', 610.6, -1.1, '黄酮类', 'CC1[C@H]([C@@H]([C@H]([C@@H](O1)OC[C@@H]2[C@H]([C@@H]([C@H]([C@@H](O2)OC3=CC(=C(C=C3)OC)O)O)O)O)O)O)OC4=CC(=C5C(=C4)OC[C@H](C5=O)C6=CC(=C(C=C6)OC)O)O', 43, 8, 15, '柑橘类水果中的黄酮苷'),
('和厚朴酚', 'Honokiol', 266.3, 5.0, '木脂素类', 'C=CCC1=CC(=C(C=C1)O)C2=CC(=C(C=C2)O)CC=C', 20, 2, 2, '厚朴中的联苯类化合物'),
('金丝桃苷', 'Hyperoside', 464.4, 0.4, '黄酮类', 'C1=CC(=C(C=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)CO)O)O)O)O)O', 33, 8, 12, '金丝桃中的黄酮苷'),
('靛蓝', 'Indigo', 466.4, -2.8, '生物碱类', 'C1=CC=C2C(=C1)C(=O)C(=C3C4=CC=CC=C4NC3=O)N2', 20, 2, 2, '天然蓝色染料'),
('异金丝桃苷', 'Isohyperoside', 464.4, 0.4, '黄酮类', 'C1=CC(=C(C=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)CO)O)O)O)O)O', 33, 8, 12, '金丝桃苷的异构体'),
('山奈酚', 'Kaempferol', 286.24, 1.9, '黄酮类', 'C1=CC(=CC=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O)O', 21, 4, 6, '广泛存在的黄酮类化合物'),
('木犀草素', 'Luteolin', 286.24, 2.0, '黄酮类', 'C1=CC(=C(C=C1C2=CC(=O)C3=C(C=C(C=C3O2)O)O)O)O', 21, 4, 6, '木犀草中的黄酮类化合物'),
('厚朴酚', 'Magnolol', 266.3, 5.0, '木脂素类', 'C=CCC1=CC(=C(C=C1)C2=C(C=CC(=C2)CC=C)O)O', 20, 2, 2, '厚朴中的联苯类化合物'),
('杨梅素', 'Myricetin', 318.23, 1.2, '黄酮类', 'C1=C(C=C(C(=C1O)O)O)C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O', 23, 6, 8, '杨梅中的黄酮类化合物'),
('叶下珠脂素', 'Phyllanthin', 418.5, 4.2, '木脂素类', 'COC[C@@H](CC1=CC(=C(C=C1)OC)OC)[C@H](CC2=CC(=C(C=C2)OC)OC)COC', 30, 0, 8, '叶下珠中的木脂素'),
('大黄素甲醚', 'Physcion', 284.26, 3.0, '蒽醌类', 'CC1=CC2=C(C(=C1)O)C(=O)C3=C(C2=O)C(=CC(=C3)OC)O', 21, 2, 5, '大黄素的甲基化衍生物'),
('鬼臼毒素', 'Podophyllotoxin', 414.4, 2.0, '木脂素类', 'COC1=CC(=CC(=C1OC)OC)[C@H]2[C@@H]3[C@H](COC3=O)[C@H](C4=CC5=C(C=C4)OCO5)O', 30, 0, 8, '鬼臼中的木脂素，具有抗肿瘤活性'),
('白花前胡甲素', 'Praeruptorin A', 386.4, 3.1, '香豆素类', 'CC(C)C=C(C)C(=O)O[C@H]1[C@H](C2=C(C=CC(=O)O2)C3=C1C=CC(=O)O3)OC(=O)C', 27, 0, 8, '前胡中的香豆素类化合物'),
('补骨脂素', 'Psoralen', 186.16, 2.3, '香豆素类', 'C1=CC(=O)OC2=CC3=C(C=CO3)C=C21', 14, 0, 3, '补骨脂中的香豆素，用于治疗白癜风'),
('葛根素', 'Puerarin', 416.4, 0.0, '异黄酮类', 'C1=CC(=CC=C1C2=COC3=C(C2=O)C=C(C(=C3)O)[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)CO)O)O)O)O', 30, 7, 9, '葛根的主要活性成分'),
('羟基茜草素', 'Purpurin', 256.2, 2.9, '蒽醌类', 'C1=CC=C2C(=C1)C(=O)C3=C(C2=O)C(=C(C(=C3)O)O)O', 19, 3, 5, '茜草中的蒽醌类化合物'),
('槲皮素', 'Quercetin', 302.23, 1.5, '黄酮类', 'C1=CC(=C(C=C1C2=C(C(=O)C3=C(C=C(C=C3O2)O)O)O)O)O', 22, 5, 7, '广泛存在的黄酮类化合物，具有强抗氧化活性'),
('大黄酸', 'Rhein', 284.22, 2.2, '蒽醌类', 'C1=CC=C2C(=C1)C(=O)C3=C(C2=O)C(=CC(=C3)C(=O)O)O', 21, 3, 6, '大黄中的蒽醌类化合物'),
('芦丁', 'Rutin', 610.5, -1.3, '黄酮类', 'C[C@H]1[C@@H]([C@H]([C@H]([C@@H](O1)OC[C@@H]2[C@H]([C@@H]([C@H]([C@@H](O2)OC3=C(OC4=CC(=CC(=C4C3=O)O)O)C5=CC(=C(C=C5)O)O)O)O)O)O)O', 43, 10, 16, '槲皮素的芸香糖苷'),
('五味子素', 'Schisandrin', 432.5, 4.0, '木脂素类', 'C[C@H]1CC2=CC(=C(C(=C2C3=C(C(=C(C=C3C[C@]1(C)O)OC)OC)OC)OC)OC)OC', 31, 1, 7, '五味子中的木脂素'),
('五味子醇甲', 'Schisandrol A', 432.5, 4.0, '木脂素类', 'C[C@H]1CC2=CC(=C(C(=C2C3=C(C(=C(C=C3C[C@H]1C)OC)OC)OC)OC)OC)OC', 31, 0, 7, '五味子中的木脂素'),
('滨蒿内酯', 'Scoparone', 206.19, 1.9, '香豆素类', 'COC1=C(C=C2C(=C1)C=CC(=O)O2)OC', 15, 0, 4, '滨蒿中的香豆素'),
('东莨菪内酯', 'Scopoletin', 192.17, 1.5, '香豆素类', 'COC1=C(C=C2C(=C1)C=CC(=O)O2)O', 14, 1, 4, '东莨菪中的香豆素'),
('芝麻素', 'Sesamin', 354.4, 2.7, '木脂素类', 'C1[C@H]2[C@H](CO[C@@H]2C3=CC4=C(C=C3)OCO4)[C@@H](O1)C5=CC6=C(C=C5)OCO6', 26, 0, 6, '芝麻中的木脂素'),
('汉黄芩素', 'Wogonin', 284.26, 3.0, '黄酮类', 'COC1=C(C=C(C2=C1OC(=CC2=O)C3=CC=CC=C3)O)O', 21, 2, 5, '黄芩中的黄酮类化合物'),
('汉黄芩苷', 'Wogonoside', 460.4, 1.4, '黄酮类', 'COC1=C(C=C(C2=C1OC(=CC2=O)C3=CC=CC=C3)O)O[C@H]4[C@@H]([C@H]([C@@H]([C@H](O4)C(=O)O)O)O)O', 33, 6, 11, '汉黄芩素的糖苷形式'),
('花椒松', 'Xanthyletin', 228.24, 2.8, '香豆素类', 'CC1(C=CC2=C(O1)C=C3C=CC(=O)OC3=C2)C', 17, 0, 3, '花椒中的香豆素');


-- ============================================
-- 第四步：插入对接结果（48条，所有字段有值）
-- ============================================

INSERT INTO docking_results (compound_id, affinity, status, similarity_score) VALUES
(1, -9.2, 'completed', 0.85),
(2, -8.7, 'completed', 0.78),
(3, -10.1, 'completed', 0.92),
(4, -7.8, 'completed', 0.71),
(5, -9.5, 'completed', 0.88),
(6, -8.3, 'completed', 0.75),
(7, -11.2, 'completed', 0.95),
(8, -8.9, 'completed', 0.82),
(9, -9.8, 'completed', 0.89),
(10, -8.1, 'completed', 0.73),
(11, -10.5, 'completed', 0.91),
(12, -9.0, 'completed', 0.84),
(13, -8.5, 'completed', 0.77),
(14, -9.7, 'completed', 0.87),
(15, -8.8, 'completed', 0.80),
(16, -7.9, 'completed', 0.72),
(17, -9.3, 'completed', 0.86),
(18, -8.2, 'completed', 0.74),
(19, -9.1, 'completed', 0.83),
(20, -8.6, 'completed', 0.79),
(21, -7.5, 'completed', 0.68),
(22, -8.0, 'completed', 0.70),
(23, -9.9, 'completed', 0.90),
(24, -9.4, 'completed', 0.85),
(25, -7.2, 'completed', 0.65),
(26, -9.6, 'completed', 0.88),
(27, -10.3, 'completed', 0.93),
(28, -8.4, 'completed', 0.76),
(29, -9.2, 'completed', 0.84),
(30, -10.8, 'completed', 0.94),
(31, -8.7, 'completed', 0.81),
(32, -9.0, 'completed', 0.83),
(33, -7.6, 'completed', 0.69),
(34, -8.9, 'completed', 0.82),
(35, -7.3, 'completed', 0.67),
(36, -9.5, 'completed', 0.87),
(37, -10.2, 'completed', 0.91),
(38, -8.8, 'completed', 0.80),
(39, -9.1, 'completed', 0.84),
(40, -8.3, 'completed', 0.75),
(41, -9.7, 'completed', 0.89),
(42, -8.5, 'completed', 0.78),
(43, -8.1, 'completed', 0.73),
(44, -9.3, 'completed', 0.86),
(45, -9.8, 'completed', 0.90),
(46, -8.6, 'completed', 0.79),
(47, -9.4, 'completed', 0.85),
(48, -8.2, 'completed', 0.74);

-- ============================================
-- 第五步：插入 ADMET 结果（48条，所有字段有值）
-- ============================================

INSERT INTO admet_results (compound_id, herg_toxicity, ames_toxicity, liver_toxicity) VALUES
(1, 0.25, 0, 0),
(2, 0.42, 0, 0),
(3, 0.18, 0, 0),
(4, 0.55, 1, 0),
(5, 0.22, 0, 0),
(6, 0.38, 0, 0),
(7, 0.15, 0, 0),
(8, 0.31, 0, 0),
(9, 0.20, 0, 0),
(10, 0.45, 0, 1),
(11, 0.28, 0, 0),
(12, 0.33, 0, 0),
(13, 0.48, 0, 0),
(14, 0.19, 0, 0),
(15, 0.36, 0, 0),
(16, 0.52, 1, 0),
(17, 0.24, 0, 0),
(18, 0.41, 0, 0),
(19, 0.29, 0, 0),
(20, 0.35, 0, 0),
(21, 0.58, 1, 1),
(22, 0.47, 0, 0),
(23, 0.17, 0, 0),
(24, 0.21, 0, 0),
(25, 0.62, 1, 0),
(26, 0.23, 0, 0),
(27, 0.16, 0, 0),
(28, 0.39, 0, 0),
(29, 0.27, 0, 0),
(30, 0.14, 0, 0),
(31, 0.34, 0, 0),
(32, 0.30, 0, 0),
(33, 0.54, 1, 0),
(34, 0.26, 0, 0),
(35, 0.60, 1, 1),
(36, 0.19, 0, 0),
(37, 0.18, 0, 0),
(38, 0.37, 0, 0),
(39, 0.28, 0, 0),
(40, 0.43, 0, 0),
(41, 0.20, 0, 0),
(42, 0.32, 0, 0),
(43, 0.46, 0, 0),
(44, 0.25, 0, 0),
(45, 0.17, 0, 0),
(46, 0.40, 0, 0),
(47, 0.22, 0, 0),
(48, 0.44, 0, 0);

-- ============================================
-- 第六步：插入综合分析报告（48条，所有字段有值）
-- ============================================

INSERT INTO analysis_reports (compound_id, total_score, potency_score, safety_score, druglikeness_score, is_vetoed, advice_tags, expert_advice) VALUES
(1, 85.3, 88.5, 82.0, 85.5, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物金合欢素(Acacetin)综合评分优秀，建议优先进行实验验证。'),
(2, 78.2, 82.0, 74.5, 78.0, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物茜素(Alizarin)综合评分良好，建议进行进一步的实验验证。'),
(3, 91.5, 94.2, 89.0, 91.8, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物芦荟大黄素(Aloe-emodin)综合评分优秀，建议优先进行实验验证。'),
(4, 72.8, 75.5, 68.2, 74.5, FALSE, ARRAY['亲和力良好'], '该化合物穿心莲内酯(Andrographolide)综合评分良好，建议进行结构优化。'),
(5, 87.6, 90.3, 85.0, 87.8, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物芹菜素(Apigenin)综合评分优秀，建议优先进行实验验证。'),
(6, 79.4, 81.8, 77.0, 79.5, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物牛蒡子苷(Arctiin)综合评分良好，建议进行进一步的实验验证。'),
(7, 94.8, 97.5, 92.0, 95.2, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物青蒿素(Artemisinin)综合评分优秀，建议优先进行实验验证。'),
(8, 83.1, 85.8, 80.5, 83.2, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好'], '该化合物细辛脂素(Asarinin)综合评分优秀，建议优先进行实验验证。'),
(9, 89.2, 92.0, 86.5, 89.5, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物黄芩素(Baicalein)综合评分优秀，建议优先进行实验验证。'),
(10, 76.5, 79.2, 71.8, 78.5, FALSE, ARRAY['亲和力良好'], '该化合物黄芩苷(Baicalin)综合评分良好，建议进行进一步的实验验证。'),
(11, 90.8, 93.5, 88.0, 91.2, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物小檗碱(Berberine)综合评分优秀，建议优先进行实验验证。'),
(12, 84.5, 87.2, 81.8, 84.8, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好'], '该化合物鹰嘴豆芽素A(Biochanin A)综合评分优秀，建议优先进行实验验证。'),
(13, 80.3, 82.8, 77.8, 80.5, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物大黄酚(Chrysophanol)综合评分良好，建议进行进一步的实验验证。'),
(14, 88.7, 91.5, 86.0, 88.9, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物姜黄素(Curcumin)综合评分优秀，建议优先进行实验验证。'),
(15, 82.4, 85.0, 79.8, 82.6, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物大豆苷元(Daidzein)综合评分良好，建议进行进一步的实验验证。'),
(16, 74.1, 76.8, 69.5, 76.0, FALSE, ARRAY['亲和力良好'], '该化合物大豆苷(Daidzin)综合评分良好，建议进行结构优化。'),
(17, 86.9, 89.6, 84.2, 87.1, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物大黄素(Emodin)综合评分优秀，建议优先进行实验验证。'),
(18, 77.8, 80.5, 75.2, 77.9, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物七叶苷(Esculin)综合评分良好，建议进行进一步的实验验证。'),
(19, 84.2, 86.9, 81.5, 84.4, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好'], '该化合物杜鹃素(Farrerol)综合评分优秀，建议优先进行实验验证。'),
(20, 81.6, 84.2, 79.0, 81.8, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物芒柄花素(Formononetin)综合评分良好，建议进行进一步的实验验证。'),
(21, 68.5, 71.2, 62.8, 71.5, TRUE, ARRAY['需要优化'], '该化合物连翘苷(Forsythin)存在安全性风险，已被熔断机制否决。'),
(22, 75.3, 78.0, 72.6, 75.5, FALSE, ARRAY['亲和力良好'], '该化合物橙皮苷(Hesperidin)综合评分良好，建议进行进一步的实验验证。'),
(23, 90.5, 93.2, 87.8, 90.8, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物和厚朴酚(Honokiol)综合评分优秀，建议优先进行实验验证。'),
(24, 87.3, 90.0, 84.6, 87.5, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物金丝桃苷(Hyperoside)综合评分优秀，建议优先进行实验验证。'),
(25, 65.2, 68.0, 59.5, 68.2, TRUE, ARRAY['需要优化'], '该化合物靛蓝(Indigo)存在安全性风险，已被熔断机制否决。'),
(26, 88.1, 90.8, 85.4, 88.3, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物异金丝桃苷(Isohyperoside)综合评分优秀，建议优先进行实验验证。'),
(27, 92.7, 95.4, 90.0, 93.0, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物山奈酚(Kaempferol)综合评分优秀，建议优先进行实验验证。'),
(28, 80.9, 83.5, 78.3, 81.1, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物木犀草素(Luteolin)综合评分良好，建议进行进一步的实验验证。'),
(29, 85.7, 88.4, 83.0, 85.9, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物厚朴酚(Magnolol)综合评分优秀，建议优先进行实验验证。'),
(30, 93.5, 96.2, 90.8, 93.8, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物杨梅素(Myricetin)综合评分优秀，建议优先进行实验验证。'),
(31, 82.8, 85.4, 80.2, 83.0, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物叶下珠脂素(Phyllanthin)综合评分良好，建议进行进一步的实验验证。'),
(32, 84.6, 87.2, 82.0, 84.8, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好'], '该化合物大黄素甲醚(Physcion)综合评分优秀，建议优先进行实验验证。'),
(33, 71.4, 74.0, 66.8, 73.5, FALSE, ARRAY['亲和力良好'], '该化合物鬼臼毒素(Podophyllotoxin)综合评分良好，建议进行结构优化。'),
(34, 83.9, 86.5, 81.3, 84.1, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好'], '该化合物白花前胡甲素(Praeruptorin A)综合评分优秀，建议优先进行实验验证。'),
(35, 67.8, 70.5, 61.2, 71.0, TRUE, ARRAY['需要优化'], '该化合物补骨脂素(Psoralen)存在安全性风险，已被熔断机制否决。'),
(36, 87.9, 90.6, 85.2, 88.1, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物葛根素(Puerarin)综合评分优秀，建议优先进行实验验证。'),
(37, 91.3, 94.0, 88.6, 91.5, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物羟基茜草素(Purpurin)综合评分优秀，建议优先进行实验验证。'),
(38, 82.1, 84.7, 79.5, 82.3, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物槲皮素(Quercetin)综合评分良好，建议进行进一步的实验验证。'),
(39, 84.8, 87.4, 82.2, 85.0, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好'], '该化合物大黄酸(Rhein)综合评分优秀，建议优先进行实验验证。'),
(40, 79.7, 82.3, 77.1, 79.9, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物芦丁(Rutin)综合评分良好，建议进行进一步的实验验证。'),
(41, 89.4, 92.1, 86.7, 89.6, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物五味子素(Schisandrin)综合评分优秀，建议优先进行实验验证。'),
(42, 81.2, 83.8, 78.6, 81.4, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物五味子醇甲(Schisandrol A)综合评分良好，建议进行进一步的实验验证。'),
(43, 77.5, 80.1, 74.9, 77.7, FALSE, ARRAY['亲和力良好'], '该化合物滨蒿内酯(Scoparone)综合评分良好，建议进行进一步的实验验证。'),
(44, 86.2, 88.9, 83.5, 86.4, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物东莨菪内酯(Scopoletin)综合评分优秀，建议优先进行实验验证。'),
(45, 90.1, 92.8, 87.4, 90.3, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物芝麻素(Sesamin)综合评分优秀，建议优先进行实验验证。'),
(46, 80.6, 83.2, 78.0, 80.8, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物汉黄芩素(Wogonin)综合评分良好，建议进行进一步的实验验证。'),
(47, 87.5, 90.2, 84.8, 87.7, FALSE, ARRAY['亲和力优秀', 'ADMET性质良好', '成药性好'], '该化合物汉黄芩苷(Wogonoside)综合评分优秀，建议优先进行实验验证。'),
(48, 78.9, 81.5, 76.3, 79.1, FALSE, ARRAY['亲和力良好', '成药性较好'], '该化合物花椒松(Xanthyletin)综合评分良好，建议进行进一步的实验验证。');

-- ============================================
-- 第七步：重置自增序列
-- ============================================

SELECT setval('compounds_id_seq', (SELECT MAX(id) FROM compounds));
SELECT setval('docking_results_id_seq', (SELECT MAX(id) FROM docking_results));
SELECT setval('admet_results_id_seq', (SELECT MAX(id) FROM admet_results));
SELECT setval('analysis_reports_id_seq', (SELECT MAX(id) FROM analysis_reports));

-- ============================================
-- 第八步：验证数据
-- ============================================

SELECT '✅ 数据库初始化完成！' AS status;

SELECT 
    '化合物' AS 表名, COUNT(*) AS 记录数 FROM compounds
UNION ALL
SELECT '对接结果', COUNT(*) FROM docking_results
UNION ALL
SELECT 'ADMET结果', COUNT(*) FROM admet_results
UNION ALL
SELECT '分析报告', COUNT(*) FROM analysis_reports;

-- 查看 Top 10 化合物（按亲和力排序）
SELECT 
    ROW_NUMBER() OVER (ORDER BY dr.affinity ASC) AS 排名,
    c.id,
    c.name AS 中文名,
    c.english_name AS 英文名,
    dr.affinity AS 亲和力,
    ar.total_score AS 总分,
    ar.is_vetoed AS 是否熔断
FROM compounds c
JOIN docking_results dr ON c.id = dr.compound_id
JOIN analysis_reports ar ON c.id = ar.compound_id
ORDER BY dr.affinity ASC
LIMIT 10;

-- 验证所有字段都不为 null
SELECT 
    '检查 null 值' AS 检查项,
    COUNT(*) FILTER (WHERE total_score IS NULL) AS total_score_null,
    COUNT(*) FILTER (WHERE potency_score IS NULL) AS potency_score_null,
    COUNT(*) FILTER (WHERE safety_score IS NULL) AS safety_score_null,
    COUNT(*) FILTER (WHERE druglikeness_score IS NULL) AS druglikeness_score_null
FROM analysis_reports;
