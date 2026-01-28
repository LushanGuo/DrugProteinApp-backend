package com.drugscreen.platform.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 排名化合物数据传输对象
 * 用于显示 Top 10 化合物列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankedCompoundDTO {
    private Long id;              // 化合物 ID
    private String name;          // 化合物名称
    private String englishName;   // 英文名称
    private Double affinity;      // 亲和力 (kcal/mol)
    private Integer rank;         // 排名 (#1, #2, ...)
    private String category;      // 分类
}
