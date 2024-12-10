# 简单记账

一个简单的记账应用，帮助你轻松管理个人财务。

## 功能特点

- 交易记录管理
  - 添加、编辑、删除交易记录
  - 支持收入、支出和转账类型
  - 自定义分类和标签
  - 添加备注和图片

- 账户管理
  - 多账户支持
  - 账户余额实时更新
  - 账户类型自定义
  - 账户统计分析

- 预算管理
  - 设置月度预算
  - 分类预算
  - 预算超支提醒
  - 预算执行分析

- 统计分析
  - 收支趋势分析
  - 分类统计
  - 月度/年度报表
  - 自定义统计周期

- 其他功能
  - 深色模式支持
  - 多语言支持
  - 数据备份恢复
  - 导出报表

## 快速开始

1. 克隆项目
```bash
git clone https://github.com/yourusername/simple-bookkeeping.git
```

2. 安装依赖
```bash
cd simple-bookkeeping
./gradlew build
```

3. 运行应用
```bash
./gradlew installDebug
```

## 技术栈

- Kotlin
- Jetpack Compose
- Material Design 3
- Room Database
- Hilt
- Coroutines
- Flow
- Navigation
- ViewModel

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── myapplication/
│   │   │               ├── data/
│   │   │               │   ├── local/
│   │   │               │   └── repository/
│   │   │               ├── di/
│   │   │               ├── domain/
│   │   │               │   ├── model/
│   │   │               │   ├── repository/
│   │   │               │   └── usecase/
│   │   │               └── presentation/
│   │   │                   ├── components/
│   │   │                   ├── navigation/
│   │   │                   ├── theme/
│   │   │                   └── screens/
│   │   └── res/
│   └── test/
└── build.gradle
```

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 发起 Pull Request

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 作者：Your Name
- 邮箱：your.email@example.com
- 主页：https://github.com/yourusername 