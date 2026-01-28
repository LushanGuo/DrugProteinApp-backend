@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 药物筛选平台 - 认证接口测试脚本 (Windows)

set BASE_URL=http://localhost:8080

echo ==========================================
echo 药物筛选平台 - 认证接口测试
echo ==========================================
echo.

REM 1. 健康检查
echo 1. 测试健康检查接口...
curl -s -X GET "%BASE_URL%/api/auth/health"
echo.
echo.

REM 2. 注册用户
echo 2. 测试用户注册...
curl -s -X POST "%BASE_URL%/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\":\"13800138000\",\"email\":\"test@example.com\",\"password\":\"test123456\",\"purpose\":\"科研学习\"}"
echo.
echo.

REM 3. 检查手机号
echo 3. 测试检查手机号...
curl -s -X GET "%BASE_URL%/api/auth/check-phone/13800138000"
echo.
echo.

REM 4. 检查邮箱
echo 4. 测试检查邮箱...
curl -s -X GET "%BASE_URL%/api/auth/check-email/test@example.com"
echo.
echo.

REM 5. 登录
echo 5. 测试用户登录...
curl -s -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"phoneOrEmail\":\"test@example.com\",\"password\":\"test123456\"}"
echo.
echo.

REM 6. 访问化合物接口
echo 6. 访问化合物接口（无需 Token）...
curl -s -X GET "%BASE_URL%/api/compounds?page=0&size=5"
echo.
echo.

echo ==========================================
echo 测试完成！
echo ==========================================
pause
