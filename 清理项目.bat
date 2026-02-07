@echo off
chcp 65001 >nul
echo ========================================
echo 清理项目临时文件
echo ========================================
echo.

echo [提示] 此脚本会删除临时文件，保留重要文件
echo 按任意键继续，或关闭窗口取消...
pause >nul
echo.

echo [步骤 1] 删除对接输出文件...
del /Q docking\*_out.pdbqt 2>nul
echo ✅ 已删除对接输出文件

echo.
echo [步骤 2] 删除临时文档...
del /Q 后端404问题诊断.md 2>nul
del /Q 修复IDEA-JDK配置.md 2>nul
del /Q 修复对接和可视化问题.md 2>nul
del /Q 数据库字段对照修改说明.md 2>nul
del /Q 模块合并说明.md 2>nul
del /Q 项目清理说明.md 2>nul
del /Q 综合评分更新总结.md 2>nul
del /Q 综合评分快速指南.md 2>nul
del /Q 添加ADMET五维数据-完整步骤.md 2>nul
echo ✅ 已删除临时文档

echo.
echo [步骤 3] 删除临时脚本...
del /Q 启动应用-不用IDEA.bat 2>nul
del /Q 快速启动应用.bat 2>nul
del /Q 重启应用并测试.bat 2>nul
del /Q 测试综合评分-简化版.bat 2>nul
del /Q 测试ADMET-API.bat 2>nul
del /Q test-backend.bat 2>nul
del /Q test-merged-apis.bat 2>nul
del /Q check-platform-status.bat 2>nul
echo ✅ 已删除临时脚本

echo.
echo [步骤 4] 删除临时数据库脚本...
del /Q 手动更新ADMET数据.sql 2>nul
del /Q 更新ADMET五维数据.sql 2>nul
del /Q 更新ADMET默认值.sql 2>nul
del /Q 数据库迁移脚本-从旧表到新表.sql 2>nul
del /Q 数据库重建脚本-删除旧表创建新表.sql 2>nul
del /Q 直接更新数据库.ps1 2>nul
echo ✅ 已删除临时数据库脚本

echo.
echo [步骤 5] 删除临时文件...
del /Q token.json 2>nul
echo ✅ 已删除临时文件

echo.
echo ========================================
echo 清理完成！
echo ========================================
echo.
echo 保留的重要文件：
echo   - README.md
echo   - IDEA配置指南.md
echo   - 综合评分算法说明.md
echo   - ADMET五维数据修复指南.md
echo   - docs/API接口文档-v2.0.md
echo   - 启动应用.bat
echo   - 快速测试ADMET.bat
echo   - 测试综合评分.bat
echo   - 数据库完整初始化脚本.sql
echo.
echo 下一步：运行 Git 提交
echo   1. git status
echo   2. git add .
echo   3. git commit -m "你的提交信息"
echo   4. git push
echo.
pause
