@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8080

echo ==========================================
echo 忘记密码功能测试
echo ==========================================
echo.

REM 1. 发送验证码
echo [1/3] 发送验证码...
echo 手机号: 18300899350
curl -s -X POST "%BASE_URL%/api/auth/forgot-password/send-code" ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\":\"18300899350\"}"
echo.
echo.
echo 请查看后端控制台获取验证码
echo.
pause

REM 2. 验证验证码
echo [2/3] 验证验证码...
set /p CODE="请输入验证码: "
curl -s -X POST "%BASE_URL%/api/auth/forgot-password/verify-code" ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\":\"18300899350\",\"code\":\"%CODE%\"}"
echo.
echo.

REM 3. 重置密码
echo [3/3] 重置密码...
echo 新密码: 654321
curl -s -X POST "%BASE_URL%/api/auth/forgot-password/reset" ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\":\"18300899350\",\"code\":\"%CODE%\",\"newPassword\":\"654321\"}"
echo.
echo.

echo ==========================================
echo 测试完成！
echo.
echo 现在可以用新密码登录：
echo 手机号: 18300899350
echo 密码: 654321
echo ==========================================
pause
