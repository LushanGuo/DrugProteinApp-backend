#!/bin/bash

# 药物筛选平台 - 认证接口测试脚本

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "药物筛选平台 - 认证接口测试"
echo "=========================================="
echo ""

# 1. 健康检查
echo "1. 测试健康检查接口..."
curl -s -X GET "$BASE_URL/api/auth/health" | jq '.'
echo ""
echo ""

# 2. 注册用户
echo "2. 测试用户注册..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "email": "test@example.com",
    "password": "test123456",
    "purpose": "科研学习"
  }')
echo "$REGISTER_RESPONSE" | jq '.'
echo ""
echo ""

# 3. 检查手机号
echo "3. 测试检查手机号..."
curl -s -X GET "$BASE_URL/api/auth/check-phone/13800138000" | jq '.'
echo ""
echo ""

# 4. 检查邮箱
echo "4. 测试检查邮箱..."
curl -s -X GET "$BASE_URL/api/auth/check-email/test@example.com" | jq '.'
echo ""
echo ""

# 5. 登录
echo "5. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneOrEmail": "test@example.com",
    "password": "test123456"
  }')
echo "$LOGIN_RESPONSE" | jq '.'

# 提取 token
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token')
echo ""
echo "Token: $TOKEN"
echo ""
echo ""

# 6. 使用 token 访问化合物接口（测试 token 是否有效）
echo "6. 使用 Token 访问化合物接口..."
curl -s -X GET "$BASE_URL/api/compounds?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""
echo ""

echo "=========================================="
echo "测试完成！"
echo "=========================================="
