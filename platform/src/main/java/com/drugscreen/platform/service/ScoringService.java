package com.drugscreen.platform.service;

import com.drugscreen.platform.dto.ScoringResultDTO;
import com.drugscreen.platform.entity.AdmetResult;
import com.drugscreen.platform.entity.AnalysisReport;
import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.entity.DockingResult;
import com.drugscreen.platform.repository.AdmetResultRepository;
import com.drugscreen.platform.repository.AnalysisReportRepository;
import com.drugscreen.platform.repository.CompoundRepository;
import com.drugscreen.platform.repository.DockingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final CompoundRepository compoundRepository;
    private final AnalysisReportRepository reportRepository;
    private final DockingResultRepository dockingResultRepository;
    private final AdmetResultRepository admetResultRepository;

    @Transactional
    public ScoringResultDTO analyze(Long compoundId) {
        // 1. 获取基础数据
        Compound compound = compoundRepository.findById(compoundId)
                .orElseThrow(() -> new RuntimeException("化合物不存在"));

        // 2. 从数据库读取对接结果
        DockingResult dockingResult = dockingResultRepository.findTopByCompoundIdOrderByAffinityAsc(compoundId)
                .orElseThrow(() -> new RuntimeException("未找到对接结果"));

        // 3. 从数据库读取 ADMET 结果
        AdmetResult admetResult = admetResultRepository.findByCompoundId(compoundId)
                .orElseThrow(() -> new RuntimeException("未找到ADMET结果"));

        // 获取真实数据
        double mockAffinity = dockingResult.getAffinity() != null ? dockingResult.getAffinity() : -7.0;
        double mockSimilarity = 0.65; // 暂时使用默认值，后续可以从数据库读取

        // ADMET 数据
        double mockHergProb = admetResult.getHergToxicity() != null ? admetResult.getHergToxicity() : 0.1;
        boolean mockAmes = admetResult.getAmesToxicity() != null && admetResult.getAmesToxicity() == 1;
        boolean mockLiver = admetResult.getLiverToxicity() != null && admetResult.getLiverToxicity() == 1;

        // 分子属性
        int hbd = compound.getHbd() != null ? compound.getHbd() : 3;
        int hba = compound.getHba() != null ? compound.getHba() : 6;
        int heavyAtoms = compound.getHeavyAtomCount() != null ? compound.getHeavyAtomCount() : 
                         (int) (compound.getMolecularWeight() / 12);

        // --- 开始评分逻辑 (The Master Plan) ---
        ScoringResultDTO result = new ScoringResultDTO();
        result.setCompoundId(compoundId);
        List<String> tags = new ArrayList<>();
        StringBuilder advice = new StringBuilder();

        double totalScore = 0.0;

        // =====================================================
        // 模块二：ADMET 安全性 (Safety) —— 权重 35% (优先检查熔断)
        // =====================================================
        double safetyScore = 0.0;

        // A. hERG 心脏毒性 (15分 + 熔断机制)
        if (mockHergProb > 0.7) {
            // --- 触发熔断 ---
            result.setVetoed(true);
            result.setTotalScore(0.0);
            tags.add("⛔ hERG高危熔断");
            advice.append("❌ 严重警告：该分子 hERG 心脏毒性预测概率过高 (>0.7)，存在致死性心律失常风险。根据安全一票否决制，系统已自动终止该分子的开发流程。\n");

            // 即使熔断，也保存记录，但分数为0
            saveReport(compoundId, 0.0, 0.0, 0.0, 0.0, tags, advice.toString());
            result.setAdviceTags(tags);
            result.setExpertAdvice(advice.toString());
            return result; // 直接返回
        } else if (mockHergProb < 0.3) {
            safetyScore += 15.0;
            tags.add("心脏安全性佳");
        } else {
            safetyScore += 5.0; // 中等风险
            tags.add("hERG风险中等");
        }

        // B. AMES 致突变性 (10分)
        if (!mockAmes) {
            safetyScore += 10.0;
        } else {
            tags.add("致突变风险");
            advice.append("⚠️ 安全性：AMES 测试呈阳性，存在潜在致癌/致突变风险。\n");
        }

        // C. 肝毒性 (10分)
        if (!mockLiver) {
            safetyScore += 10.0;
        } else {
            tags.add("肝毒性风险");
            advice.append("⚠️ 安全性：预测存在肝损伤 (DILI) 风险。\n");
        }

        advice.append(String.format("🛡️ 安全性得分：%.1f / 35.0\n", safetyScore));
        totalScore += safetyScore;


        // =====================================================
        // 模块一：生物活性与效能 (Potency) —— 权重 45%
        // =====================================================
        double potencyScore = 0.0;

        // A. 分子对接亲和力 (30分) - 线性插值
        // 范围: -10 (满分) 到 -6 (0分)
        double affScore = 0.0;
        if (mockAffinity <= -10.0) {
            affScore = 30.0;
            tags.add("🌟 极强结合");
        } else if (mockAffinity > -6.0) {
            affScore = 0.0;
            tags.add("结合力弱");
        } else {
            // 线性插值公式: Score = 30 * (x - (-6)) / (-10 - (-6))
            affScore = 30.0 * (mockAffinity - (-6.0)) / (-4.0);
        }
        potencyScore += affScore;

        // B. 结构相似性 (10分)
        if (mockSimilarity >= 0.7) {
            potencyScore += 10.0;
            tags.add("骨架成熟"); // 类似 Palbociclib
        } else if (mockSimilarity >= 0.5) {
            potencyScore += 5.0;
        } else {
            potencyScore += 2.0;
            tags.add("💡 骨架新颖"); // 鼓励创新
        }

        // C. 配体效率 LE (5分)
        double le = -mockAffinity / heavyAtoms;
        if (le >= 0.3) {
            potencyScore += 5.0;
            tags.add("高配体效率");
        }

        advice.append(String.format("🎯 效能得分：%.1f / 45.0 (Affinity: %.1f kcal/mol)\n", potencyScore, mockAffinity));
        totalScore += potencyScore;


        // =====================================================
        // 模块三：理化性质与成药性 (Drug-Likeness) —— 权重 20%
        // =====================================================
        double drugLikenessScore = 0.0;

        // A. LogP (10分)
        double logP = compound.getLogP();
        if (logP >= 0 && logP <= 3) {
            drugLikenessScore += 10.0;
        } else if ((logP > 3 && logP <= 4) || (logP >= -1 && logP < 0)) {
            drugLikenessScore += 6.0;
        } else {
            tags.add("LogP不佳");
        }

        // B. 分子量 MW (5分)
        double mw = compound.getMolecularWeight();
        if (mw >= 300 && mw <= 500) {
            drugLikenessScore += 5.0;
        } else if ((mw >= 250 && mw < 300) || (mw > 500 && mw <= 550)) {
            drugLikenessScore += 3.0;
        }

        // C. 氢键 (5分)
        if (hbd <= 5 && hba <= 10) {
            drugLikenessScore += 5.0;
        } else {
            drugLikenessScore += 2.0;
        }

        advice.append(String.format("💊 成药性得分：%.1f / 20.0\n", drugLikenessScore));
        totalScore += drugLikenessScore;

        // --- 最终汇总 ---
        // 保留一位小数
        totalScore = Math.round(totalScore * 10.0) / 10.0;

        result.setTotalScore(totalScore);
        result.setPotencyScore(potencyScore);
        result.setSafetyScore(safetyScore);
        result.setDruglikenessScore(drugLikenessScore);
        result.setVetoed(false);
        result.setAdviceTags(tags);
        result.setExpertAdvice(advice.toString());

        // 保存到数据库
        saveReport(compoundId, totalScore, potencyScore, safetyScore, drugLikenessScore, tags, advice.toString());

        return result;
    }

    private void saveReport(Long compoundId, Double total, Double potency, Double safety, Double drug, List<String> tags, String advice) {
        AnalysisReport report = new AnalysisReport();
        report.setCompoundId(compoundId);
        report.setTotalScore(total);
        report.setPotencyScore(potency);
        report.setSafetyScore(safety);
        report.setDruglikenessScore(drug);
        report.setIsVetoed(total == 0.0); // 如果总分为0，说明被熔断了
        report.setAdviceTags(tags);
        report.setExpertAdvice(advice);
        reportRepository.save(report);
    }
}
