@echo off
chcp 65001 >nul
echo ========================================
echo ADMET 五维数据修复脚本
echo ========================================
echo.

echo 步骤 1: 更新数据库中的 NULL 值
echo ----------------------------------------
echo 请在 PostgreSQL 中手动执行以下 SQL：
echo.
echo UPDATE admet_results SET absorption = 0.7 WHERE absorption IS NULL;
echo UPDATE admet_results SET metabolism = 0.6 WHERE metabolism IS NULL;
echo.
echo 或者使用 pgAdmin 或 DBeaver 执行 更新ADMET默认值.sql 文件
echo.
pause
echo.

echo 步骤 2: 重新编译应用
echo ----------------------------------------
cd platform
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！
    pause
    exit /b 1
)
echo 编译成功！
echo.

echo 步骤 3: 启动应用（在新窗口中）
echo ----------------------------------------
echo 正在启动应用...
start "Drug Screen Platform" cmd /k "mvnw.cmd spring-boot:run"
echo 应用正在启动，请等待 10 秒...
timeout /t 10 /nobreak
echo.

echo 步骤 4: 测试 ADMET 预测
echo ----------------------------------------
cd ..
echo.
echo 测试化合物 ID=1 的 ADMET 预测：
curl -X POST "http://localhost:8080/api/admet/predict?compoundId=1" -H "Content-Type: application/json"
echo.
echo.

echo 查询化合物 ID=1 的 ADMET 结果：
curl -X GET "http://localhost:8080/api/admet/result/compound/1"
echo.
echo.

echo ========================================
echo 修复完成！
echo ========================================
echo.
echo 请检查上面的 JSON 响应中：
echo - absorption 字段是否有值（不是 null）
echo - metabolism 字段是否有值（不是 null）
echo.
pause
