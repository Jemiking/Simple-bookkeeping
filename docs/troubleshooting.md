# Simple-bookkeeping 项目问题记录

## 构建问题

### 1. Kotlin 与 Compose 编译器版本不匹配

**问题描述**：
- 项目使用 Kotlin 1.9.21 版本与 Compose 编译器 1.5.6 版本
- 这两个版本存在兼容性问题，导致编译失败

**解决方案**：
- 将 Kotlin 版本降级到 1.9.20
- 将 Compose 编译器版本调整为 1.5.4
- 修改位置：`gradle/libs.versions.toml`

### 2. 构建缓存和目录清理问题

**问题描述**：
- 执行 `./gradlew clean build` 时出现 `DirectoryNotEmptyException` 错误
- 无法删除以下目录：
  - `app/build/intermediates/external_libs_dex/release`
  - `app/build/intermediates/external_libs_dex`
  - `app/build/intermediates`

**可能的解决方案**：
1. 手动解决：
   - 关闭所有使用项目文件的应用（Android Studio、IDE 等）
   - 手动删除 build 目录
   - 重新执行构建命令

2. 清理 Gradle 缓存：
   ```bash
   ./gradlew cleanBuildCache
   ```

3. 如果问题持续：
   - 删除 `.gradle` 缓存目录
   - 重新同步项目

### 3. Hilt 依赖注入配置

**问题描述**：
- Hilt 相关的编译���误
- kapt 生成的代码可能存在问题

**解决方案**：
1. 确保 Hilt 配置正确：
   - 检查 Application 类是否正确标注 `@HiltAndroidApp`
   - 检查 Activity 和 Fragment 是否正确标注 `@AndroidEntryPoint`

2. 清理 kapt 生成的文件：
   ```bash
   ./gradlew clean kaptDebugKotlin
   ```

## 建议的预防措施

1. 版本管理：
   - 使用版本目录（libs.versions.toml）统一管理依赖版本
   - 在更新版本前检查兼容性

2. 构建优化：
   - 启用 Gradle 构建缓存
   - 使用并行构建
   - 配置适当的 JVM 参数

3. 开发流程：
   - 定期清理项目
   - 保持依赖版本更新
   - 遵循最佳实践和官方推荐配置

## 待解决问题

1. 构建性能优化：
   - 分析构建时间
   - 优化构建配置
   - 考虑使用 Gradle Enterprise

2. 依赖管理：
   - 检查是否存在依赖冲突
   - 移除未使用的依赖
   - 更新过时的依赖

3. 代码质量：
   - 添加静态代码分析
   - 配置 lint 检查
   - 添加单元测试 