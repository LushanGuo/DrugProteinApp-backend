package com.drugscreen.platform.config;

import com.drugscreen.platform.service.CsvImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(CsvImportService csvImportService) {
        return args -> {
            // 启动时自动从 CSV 导入 48 个化合物
            csvImportService.importCompoundsFromCsv();
        };
    }
}
