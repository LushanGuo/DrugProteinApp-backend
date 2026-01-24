@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set BASE_URL=http://192.168.3.254:8080

echo ==========================================
echo 登录问题快速测试
echo ==========================================
echo.

REM 1. 健康检查
echo [1/4] 测试后端健康检查...
curl -s -X GET "%BASE_URL%/api/auth/health"
echo.
echo.

REM 2. 注册测试用户
echo [2/4] 注册测试用户...
echo 手机号: 13800138000
echo 密码: 123456
curl -s -X POST "%BASE_URL%/api/auth/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\":\"13800138000\",\"email\":\"test@example.com\",\"password\":\"123456\",\"purpose\":\"测试\"}"
echo.
echo.

REM 3. 测试登录
echo [3/4] 测试登录...
curl -s -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"phoneOrEmail\":\"13800138000\",\"password\":\"123456\"}"
echo.
echo.

REM 4. 用邮箱登录
echo [4/4] 用邮箱测试登录...
curl -s -X POST "%BASE_URL%/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"phoneOrEmail\":\"test@example.com\",\"password\":\"123456\"}"
echo.
echo.

echo ==========================================
echo 测试完成！
echo.
echo 如果看到 "success": true，说明登录成功
echo 如果看到 "success": false，查看 message 字段的错误信息
echo ==========================================
pause
