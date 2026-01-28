# 测试脚本

本目录包含用于测试 API 接口的脚本。

## 脚本列表

### Windows 批处理脚本

- `test-auth-api.bat` - 测试认证接口（注册、登录）
- `快速测试登录.bat` - 快速测试登录功能
- `测试忘记密码.bat` - 测试忘记密码功能

### Linux/Mac Shell 脚本

- `test-auth-api.sh` - 测试认证接口（注册、登录）

## 使用方法

### Windows

```bash
# 测试认证接口
scripts\test-auth-api.bat

# 测试登录
scripts\快速测试登录.bat

# 测试忘记密码
scripts\测试忘记密码.bat
```

### Linux/Mac

```bash
# 添加执行权限
chmod +x scripts/test-auth-api.sh

# 运行测试
./scripts/test-auth-api.sh
```

## 注意事项

- 确保后端服务已启动（`http://localhost:8080`）
- 确保数据库已初始化
- 测试前请查看各脚本的说明
