@echo off
chcp 65001 >nul
echo ========================================
echo 测试综合评分算法（使用真实 Vina 和 ADMET 数据）
echo ========================================
echo.

echo [步骤 1] 检查应用是否运行...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 应用未运行，请先启动应用
    echo 运行命令: 启动应用.bat
    pause
    exit /b 1
)
echo ✅ 应用正在运行
echo.

echo [步骤 2] 测试综合评分 API（化合物 ID: 1）...
echo.
curl -X POST "http://localhost:8080/api/scoring/analyze/1" ^
     -H "Content-Type: application/json" ^
     -w "\n状态码: %%{http_code}\n"
echo.
echo.

echo [步骤 3] 查看评分详情...
echo 评分算法使用以下真实数据：
echo   - Vina 对接结果（affinity, similarity_score）
echo   - ADMET 五维预测（hERG, Ames, Liver, Absorption, Metabolism）
echo   - 化合物理化性质（MW, LogP, HBD, HBA）
echo.

echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 如果看到评分结果，说明算法正常工作。
echo 详细的评分规则请查看：综合评分算法说明.md
echo.
pause
