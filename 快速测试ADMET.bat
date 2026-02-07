@echo off
chcp 65001 >nul
echo ========================================
echo 快速测试 ADMET 五维数据
echo ========================================
echo.

echo 1. 检查应用是否运行
echo ----------------------------------------
curl -s -X GET "http://localhost:8080/api/auth/health" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 应用未运行！请先启动应用。
    echo 运行命令: cd platform ^&^& mvnw.cmd spring-boot:run
    pause
    exit /b 1
)
echo [成功] 应用正在运行
echo.

echo 2. 测试化合物 ID=1 的 ADMET 预测
echo ----------------------------------------
curl -X POST "http://localhost:8080/api/admet/predict?compoundId=1" -H "Content-Type: application/json"
echo.
echo.

echo 3. 查询化合物 ID=1 的 ADMET 结果
echo ----------------------------------------
curl -X GET "http://localhost:8080/api/admet/result/compound/1"
echo.
echo.

echo 4. 测试化合物 ID=2 的 ADMET 预测
echo ----------------------------------------
curl -X POST "http://localhost:8080/api/admet/predict?compoundId=2" -H "Content-Type: application/json"
echo.
echo.

echo 5. 查询化合物 ID=2 的 ADMET 结果
echo ----------------------------------------
curl -X GET "http://localhost:8080/api/admet/result/compound/2"
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 请检查上面的 JSON 响应：
echo.
echo ✓ 如果 absorption 和 metabolism 有值（不是 null），说明修复成功！
echo ✗ 如果仍然是 null，请查看应用日志了解详情
echo.
echo 五个维度应该是：
echo   - hergToxicity: 0-1 之间的数字
echo   - amesToxicity: 0 或 1
echo   - liverToxicity: 0 或 1
echo   - absorption: 0-1 之间的数字 (不应该是 null)
echo   - metabolism: 0-1 之间的数字 (不应该是 null)
echo.
pause
