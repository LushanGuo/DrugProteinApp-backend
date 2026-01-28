package com.drugscreen.platform.service;

import com.drugscreen.platform.entity.Compound;
import com.drugscreen.platform.repository.CompoundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final CompoundRepository compoundRepository;

    @Transactional
    public void importCompoundsFromCsv() {
        if (compoundRepository.count() > 0) {
            log.info("数据库中已存在数据，跳过 CSV 导入。");
            return;
        }

        log.info("开始从 compounds.csv 导入数据...");
        List<Compound> compounds = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource("compounds.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // 跳过标题行
                    isFirstLine = false;
                    continue;
                }

                // 简单的 CSV 解析 (假设没有包含逗号的字段)
                String[] values = line.split(",");
                if (values.length < 6) {
                    log.warn("跳过无效行: {}", line);
                    continue;
                }

                Compound c = new Compound();
                c.setName(values[0].trim());
                c.setEnglishName(values[1].trim());

                // 处理数值转换异常
                try {
                    c.setMolecularWeight(Double.parseDouble(values[2].trim()));
                    c.setLogP(Double.parseDouble(values[3].trim()));
                } catch (NumberFormatException e) {
                    log.error("数值格式错误: {}", values[0]);
                    continue;
                }

                c.setCategory(values[4].trim());
                c.setSmiles(values[5].trim());
                c.setDescription("天然产物库导入数据"); // 默认描述

                compounds.add(c);
            }

            compoundRepository.saveAll(compounds);
            log.info("成功导入 {} 个化合物。", compounds.size());

        } catch (Exception e) {
            log.error("CSV 导入失败: ", e);
            throw new RuntimeException("初始化数据失败");
        }
    }
}