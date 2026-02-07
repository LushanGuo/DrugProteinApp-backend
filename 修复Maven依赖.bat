@echo off
chcp 65001 >nul
echo ========================================
echo 修复 Maven 依赖
echo ========================================
echo.

cd platform

echo 1. 清理项目...
call mvnw.cmd clean
echo.

echo 2. 下载依赖...
call mvnw.cmd dependency:resolve
echo.

echo 3. 编译项目...
call mvnw.cmd compile
echo.

echo ========================================
echo 完成！
echo ========================================
echo.
echo 现在请在 IDEA 中：
echo 1. 右键点击 platform 文件夹
echo 2. 选择 Maven → Reload Project
echo 3. 或者点击右侧 Maven 面板的刷新按钮
echo.
pause
