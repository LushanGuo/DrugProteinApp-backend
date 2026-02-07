# IDEA 配置指南

## 问题描述
IDEA 无法正确识别 Maven 项目，依赖无法加载。

## 解决方案

### 方法 1: 完全重新导入（推荐）

#### 步骤 1: 关闭项目
1. 在 IDEA 中，点击 `File` → `Close Project`
2. 完全关闭 IDEA

#### 步骤 2: 清理缓存
1. 删除项目根目录下的 `.idea` 文件夹（如果存在）
2. 删除 `platform/.idea` 文件夹（如果存在）

#### 步骤 3: 重新打开
1. 启动 IDEA
2. 点击 `Open`
3. **重要**: 选择 `platform` 文件夹（不是根目录 `New-Protein-Splicing`）
4. 点击 `OK`

#### 步骤 4: 等待 Maven 同步
1. IDEA 会自动识别 `pom.xml`
2. 右下角会显示 "Importing Maven projects..."
3. 等待进度条完成（可能需要几分钟）

#### 步骤 5: 验证
1. 查看左侧项目结构，应该看到 Maven 依赖
2. 展开 `External Libraries`，应该看到所有依赖的 JAR 包

---

### 方法 2: 手动触发 Maven 导入

如果方法 1 不行，尝试手动触发：

#### 步骤 1: 打开 Maven 面板
1. 点击 IDEA 右侧的 `Maven` 标签
2. 如果没有看到，点击 `View` → `Tool Windows` → `Maven`

#### 步骤 2: 刷新 Maven 项目
1. 在 Maven 面板中，点击刷新按钮（🔄）
2. 或者右键点击 `platform` → `Reload Project`

#### 步骤 3: 重新导入
1. 右键点击 `pom.xml`
2. 选择 `Maven` → `Reload Project`

---

### 方法 3: 使用命令行下载依赖

如果 IDEA 仍然无法工作，使用命令行：

#### 步骤 1: 运行修复脚本
```bash
修复Maven依赖.bat
```

或者手动执行：
```bash
cd platform
mvnw.cmd clean
mvnw.cmd dependency:resolve
mvnw.cmd compile
```

#### 步骤 2: 在 IDEA 中刷新
1. 回到 IDEA
2. 右键点击 `platform` 文件夹
3. 选择 `Maven` → `Reload Project`

---

### 方法 4: 检查 Maven 配置

#### 步骤 1: 检查 Maven 设置
1. 打开 `File` → `Settings` (Windows/Linux) 或 `IntelliJ IDEA` → `Preferences` (Mac)
2. 导航到 `Build, Execution, Deployment` → `Build Tools` → `Maven`

#### 步骤 2: 验证配置
- **Maven home path**: 应该指向 Maven 安装目录或使用 Bundled (Maven 3)
- **User settings file**: 通常是 `C:\Users\你的用户名\.m2\settings.xml`
- **Local repository**: 通常是 `C:\Users\你的用户名\.m2\repository`

#### 步骤 3: 检查 JDK
1. 在 Settings 中，导航到 `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner`
2. 确保 `JRE` 设置为 Java 17 或更高版本

---

### 方法 5: 清理 Maven 本地仓库

如果依赖损坏，清理本地仓库：

#### 步骤 1: 删除本地仓库
```bash
# Windows
rmdir /s /q %USERPROFILE%\.m2\repository\com\drugscreen
rmdir /s /q %USERPROFILE%\.m2\repository\org\springframework
```

#### 步骤 2: 重新下载
```bash
cd platform
mvnw.cmd clean install
```

---

## 常见错误及解决方案

### 错误 1: "Cannot resolve symbol 'springframework'"
**原因**: Maven 依赖未正确下载

**解决方案**:
1. 运行 `修复Maven依赖.bat`
2. 在 IDEA 中刷新 Maven 项目

### 错误 2: "Project JDK is not defined"
**原因**: 未配置 JDK

**解决方案**:
1. `File` → `Project Structure` → `Project`
2. 设置 `SDK` 为 Java 17
3. 设置 `Language level` 为 17

### 错误 3: "Maven home directory is not specified"
**原因**: Maven 未配置

**解决方案**:
1. `File` → `Settings` → `Build Tools` → `Maven`
2. 设置 `Maven home path` 为 Bundled (Maven 3)

### 错误 4: 依赖下载失败
**原因**: 网络问题或仓库配置问题

**解决方案**:
1. 检查网络连接
2. 配置 Maven 镜像（如阿里云镜像）
3. 编辑 `C:\Users\你的用户名\.m2\settings.xml`:

```xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>Aliyun Maven</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 验证步骤

### 1. 检查项目结构
左侧项目树应该显示：
```
platform
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.drugscreen.platform
│   │   └── resources
│   └── test
├── External Libraries
│   ├── Maven: org.springframework.boot:spring-boot-starter-web:3.2.0
│   ├── Maven: org.springframework.boot:spring-boot-starter-data-jpa:3.2.0
│   └── ... (其他依赖)
└── pom.xml
```

### 2. 检查 Maven 面板
右侧 Maven 面板应该显示：
```
platform
├── Lifecycle
│   ├── clean
│   ├── validate
│   ├── compile
│   └── ...
└── Dependencies
    ├── org.springframework.boot:spring-boot-starter-web:3.2.0
    └── ...
```

### 3. 运行测试
1. 找到 `PlatformApplication.java`
2. 右键 → `Run 'PlatformApplication'`
3. 应该看到 Spring Boot 启动日志

---

## 快速诊断命令

在 `platform` 目录下运行：

```bash
# 检查 Maven 版本
mvnw.cmd -v

# 检查依赖树
mvnw.cmd dependency:tree

# 验证 pom.xml
mvnw.cmd validate

# 下载所有依赖
mvnw.cmd dependency:resolve

# 编译项目
mvnw.cmd compile
```

---

## 如果所有方法都失败

### 最后的解决方案：使用命令行启动

1. 不使用 IDEA，直接用命令行：
```bash
cd platform
mvnw.cmd spring-boot:run
```

2. 如果成功启动，说明项目本身没问题，只是 IDEA 配置问题

3. 考虑：
   - 重新安装 IDEA
   - 使用其他 IDE（如 Eclipse、VS Code）
   - 继续使用命令行开发

---

## 联系支持

如果问题仍然存在，请提供：
1. IDEA 版本
2. Java 版本 (`java -version`)
3. Maven 版本 (`mvnw.cmd -v`)
4. 完整的错误日志
5. `pom.xml` 文件内容
