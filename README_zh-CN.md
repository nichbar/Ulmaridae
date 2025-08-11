# Ulmaridae

[![Build and Release](https://github.com/nichbar/Ulmaridae/actions/workflows/build-release.yml/badge.svg)](https://github.com/nichbar/Ulmaridae/actions/workflows/build-release.yml)
[![CI](https://github.com/nichbar/Ulmaridae/actions/workflows/ci.yml/badge.svg)](https://github.com/nichbar/Ulmaridae/actions/workflows/ci.yml)

**语言:** [English](README.md) | [简体中文](README_zh-CN.md)

Ulmaridae 是一个支持多种监控 Agent 的 Android 包装器应用程序，可以在 Android 设备上运行监控 Agent ，甚至无需 root 权限。

## 支持的监控 Agent 

-   **[Nezha Agent](https://github.com/nichbar/agent)** - 修改版哪吒监控 Agent 
-   **[Komari Agent](https://github.com/nichbar/komari-agent)** - 修改版 Komari 监控 Agent 

## 安装与配置

1. 从 [发布页面](https://github.com/nichbar/Ulmaridae/releases) 下载并安装最新的 APK
2. 在您的 Android 设备上打开应用程序
3. 选择您的监控 Agent ：

    - **Nezha Agent**: 用于哪吒监控面板
    - **Komari Agent**: 用于 Komari 监控系统

4. 点击"配置"来设置您选择的 Agent ：

    **对于 Nezha Agent：**

    - **服务器 URL**: 您的 Nezha 服务器 URL（例如：`your-server.com:443`）
    - **密钥**: 您的密钥（在 Nezha 控制面板中找到）
    - **UUID**: Agent 的唯一标识符（可选，可以自动生成）

    **对于 Komari Agent：**

    - **地址**: 您的 Komari 服务器地址
    - **Token**: 您的 Komari 身份验证令牌

    ⚠️ **重要提示**: 如果您计划为此 Agent 启用 WebSSH，请先阅读[安全警告](#️-安全警告webssh-功能)。

5. 出现提示时授予必要的权限：

    - Root 访问权限（如果可用）
    - 前台服务权限
    - 电池优化豁免

6. 切换"启用 Agent"开关以启动监控 Agent 

## ⚠️ 安全警告：WebSSH 功能

**重要提示：** 如果您在 Nezha 控制面板中为此 Agent 启用了 WebSSH 功能，请注意以下安全风险：

-   **直接设备访问**: WebSSH 提供对您 Android 设备的终端访问
-   **权限提升**: 在已 root 的设备上，WebSSH 可能具有提升的系统访问权限
-   **网络暴露**: WebSSH 打开一个可通过您的 Nezha 服务器访问的远程 shell
-   **数据安全**: 敏感的设备数据和文件可能通过 shell 被访问

**建议：**

-   只有在绝对需要远程终端访问时才启用 WebSSH
-   确保您的 Nezha 服务器使用强身份验证进行适当保护
-   仅在受信任的网络上使用 WebSSH
-   定期监控 WebSSH 访问日志
-   在不积极需要时考虑禁用 WebSSH

## 架构

### Root 与非 Root 操作

**具有 Root 访问权限：**

-   Agent 以提升权限运行
-   更好的系统监控能力
-   更准确的硬件信息
-   可以监控系统级指标

**无 Root 访问权限：**

-   Agent 以标准应用权限运行
-   有限但功能性的监控（应用级指标）
-   基本系统信息可用
-   网络和存储监控正常工作

## 从源码构建

### 先决条件

-   Android Studio 或 Android SDK
-   Java 17 或更高版本
-   Gradle 8.11.0+

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/nichbar/Ulmaridae.git
cd Ulmaridae

# 为 ARM 或 ARM64 架构下载 Agent 二进制文件
# 下载 Nezha Agent：
./download-agent.sh nezha arm64

# 下载 Komari Agent：
./download-agent.sh komari arm64

# 或下载两个 Agent ：
./download-agent.sh nezha arm64
./download-agent.sh komari arm64

# 构建调试 APK（包含捆绑的 Agent 二进制文件）
./gradlew assembleDebug

# 构建发布 APK（需要签名配置）
./gradlew assembleRelease
```

## 贡献

1. Fork 仓库
2. 创建功能分支
3. 进行更改
4. 在真实 Android 设备上测试
5. 提交拉取请求

## 许可证

此项目根据 MIT 许可证授权 - 有关详细信息，请参阅 [LICENSE](LICENSE) 文件。

## 致谢

-   [Nezha 项目](https://github.com/nezhahq/agent) - 原始监控服务
-   [Nezha Agent](https://github.com/nezhahq/agent) - 哪吒 Agent 
-   [Komari Agent](https://github.com/komari-monitor/komari-agent) - Komari 监控 Agent 
-   Android 开源项目 - 为 Android 框架
-   Material Design - 为 UI 组件

## 支持

如有问题和疑问：

1. 检查上述故障排除部分
2. 通过在应用中启用内存日志记录来查看日志
3. 在仓库上打开问题
