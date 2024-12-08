# 我的记账APP

## 项目概述
一个简单易用、功能完整的个人记账Android应用程序。

### 开始时间
- 项目启动日期：2024年12月07日

### 项目目标
打造一个界面简洁、操作便捷、功能实用的个人记账应用，帮助用户更好地管理个人财务。

### 当前进度
- 总体进度：35%
- 已完成功能：
  - ✅ 数据层架构
  - ✅ 核心业务逻辑
  - ✅ 依赖注入配置
  - 🚧 UI层实现（进行中）

## 核心功能
1. 快速记账
   - 支持多种记账方式
   - 智能分类系统
   - 便捷的数据输入

2. 数据统计
   - 收支概览
   - 分类统计
   - 趋势分析

3. 预算管理
   - 月度预算
   - 超支提醒
   - 预算分析

4. 多账户管理
   - 现金账户
   - 银行卡
   - 电子支付

## 技术栈
- 开发语言：Kotlin
- 最低支持Android版本：Android 8.0 (API 26)
- 目标Android版本：Android 14 (API 34)
- 架构模式：MVVM
- 数据库：Room
- UI框架：Jetpack Compose
- 依赖注入：Hilt
- 异步处理：Coroutines + Flow
- 单元测试：JUnit + Mockito

## 项目结构
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/myapp/
│   │   │   ├── data/          # 数据层
│   │   │   ├── domain/        # 领域层
│   │   │   ├── presentation/  # 表现层
│   │   │   └── utils/         # 工具类
│   │   └── res/               # 资源文件
│   ├── test/                  # 单元测试
│   └── androidTest/           # UI测试
├── build.gradle
└── proguard-rules.pro
```

## 开发规范
1. 代码规范
   - 遵循Kotlin官方编码规范
   - 使用ktlint进行代码格式化
   - 重要的类和方法必须添加注释

2. Git提交规范
   - feat: 新功能
   - fix: 修复bug
   - docs: 文档更新
   - style: 代码格式化
   - refactor: 代码重构
   - test: 测试相关
   - chore: 构建过程或辅助工具的变动

## 开发计划
详细的开发进度请查看 [COMPLETION.md](docs/COMPLETION.md)

## 环境要求
- Android Studio Hedgehog | 2023.1.1
- JDK 17
- Gradle 8.0
- Kotlin 1.9.0

## 如何运行
1. 克隆项目
```bash
git clone [项目地址]
```

2. 使用Android Studio打开项目

3. 同步Gradle文件

4. 运行项目到模拟器或实体设备

## 测试
- 单元测试：`./gradlew test`
- UI测试：`./gradlew connectedAndroidTest`

## 文档
- [开发进度](docs/COMPLETION.md)
- [API文档](docs/API.md)
- [数据库设计](docs/DATABASE.md)
- [UI设计](docs/UI_DESIGN.md)

## 贡献指南
1. Fork 项目
2. 创建特性分支
3. 提交变更
4. 推送到分支
5. 创建Pull Request

## 版本历史
- v0.1.0 (开发中) - 初始版���

## 作者
[您的名字]

## 许可证
MIT License

## 联系方式
- Email: [您的邮箱]
- GitHub: [您的GitHub] 