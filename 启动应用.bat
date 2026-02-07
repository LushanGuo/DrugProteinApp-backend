@echo off
chcp 65001 >nul
echo ========================================
echo 启动药物筛选平台
echo ========================================
echo.

cd platform

echo 正在启动应用...
echo 端口: 8080
echo 健康检查: http://localhost:8080/api/auth/health
echo.

call mvnw.cmd spring-boot:run

pause
