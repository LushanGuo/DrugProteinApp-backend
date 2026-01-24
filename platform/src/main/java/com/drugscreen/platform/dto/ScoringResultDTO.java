package com.drugscreen.platform.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScoringResultDTO {
    private Long compoundId;

    // 核心分数
    private Double totalScore;          // 总分 (0-100)
    private Double potencyScore;        // 效能分 (Max 45)
    private Double safetyScore;         // 安全分 (Max 35)
    private Double druglikenessScore;   // 成药性 (Max 20)

    // 状态标识
    private Boolean vetoed;             // 是否触发熔断 (一票否决)

    // 文本报告
    private List<String> adviceTags;    // 建议标签 (混合了优缺点)
    private String expertAdvice;        // 详细报告
}
