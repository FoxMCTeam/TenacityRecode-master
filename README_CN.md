# 🎮 特纳城市 重生版（Tenacity Recode）

[切换到 English Version](README.md)

## 🧩 客户端信息

本客户端基于 [zitalem/Tenacity](https://github.com/zitalem/Tenacity) Recode，原为 Minecraft 的开源 hacked client。

相比 Tenacity 5.1，我们进行了如下改进：

- 🔧 **高 LWJGL 版本**：带来更佳优化体验
- ☕ **JDK 18**：支持更现代化语法
- 🧹 **移除无用 API**：如 Internet Store、Spotify、Kinggen（显著减小体积和提升性能，同时保留 Script 系统，是其他 JDK 18 的分支所做不到的）
- 👤 **优化 AltManager**：保留原 UI，增强微软登录逻辑
- 🔁 **更新 ViaMCP**：可支持最新 Minecraft 版本
- ⚙️ **更强的 EventManager**：采用 [cubk1/EventManager](https://github.com/cubk1/EventManager)，语法简洁，性能更强
- 🈶 **自主 UnicodeFontRenderer**：纯手写，无 AI、无抄袭，性能出众
- 🚀 **FPSBooster / FastLoader**：启动速度压缩至约 5 秒，并能提升游戏帧率
- 🌐 **I18N 多语言系统**：支持国际化

---
## 📦 使用的库 / Repos

| 名称                                                            | 描述                 |
|---------------------------------------------------------------|--------------------|
| [cubk1/EventManager](https://github.com/cubk1/EventManager)   | 高性能事件系统            |
| [LWJGL](https://github.com/LWJGL/lwjgl3)                      | Java 游戏开发库         |
| [nashorn-core](https://github.com/OpenJDK/nashorn)            | JavaScript 引擎      |
| [FFmpeg](https://github.com/FFmpeg/FFmpeg)                    | 多媒体解码器             |
| [JavaCPP](https://github.com/bytedeco/javacpp)                | Java/C++ 桥接库       |
| [JavaCV](https://github.com/bytedeco/javacv)                  | 视频/图像处理            |
| [jflac](https://github.com/jflac-player/jflac)                | FLAC 解码            |
| [Lombok](https://github.com/projectlombok/lombok)             | 自动生成 Getter/Setter |
| [OkHttp](https://github.com/square/okhttp)                    | 高性能 HTTP 客户端       |
| [mojang-authlib](https://github.com/Mojang/authlib)           | Mojang 登录认证库       |
| [commons-lang](https://github.com/apache/commons-lang)        | Apache 公共工具包       |
| [Gson](https://github.com/google/gson)                        | JSON 序列化           |
| [JCodec](https://github.com/jcodec/jcodec)                    | Java 视频编解码         |
| [JInput](https://github.com/jinput/jinput)                    | Java 控制器输入         |
| [JNA](https://github.com/java-native-access/jna)              | 本地方法调用支持           |
| [JOpt Simple](https://github.com/jopt-simple/jopt-simple)     | 命令行参数解析            |
| [Log4j](https://github.com/apache/log4j)                      | 日志系统               |
| [Vecmath](https://github.com/notaz/vecmath)                   | 向量数学库              |
| [nv-i18n](https://github.com/NVlabs/nv-i18n)                  | 国际化支持              |
| [OpenJDK](https://github.com/openjdk/jdk)                     | Java 开源实现          |
| [Rise6.0](https://github.com/ZeathDev/Rise6.0-Src)            | Rise 客户端源码         |
| [Moonlight](https://github.com/randomguy3725/MoonLight)       | Moonlight 客户端组件    |
| [Untitled](https://github.com/ChengF3ng233/Untitled)          | 模块化功能集成            |
| [JSObject](https://github.com/holoisme/Fox)                   | JavaScript 模块支持    |
| [ViaMCP](https://github.com/CloudburstMC/ViaMCP)              | Minecraft 跨版本支持    |
| [ViaLoadingBase](https://github.com/Viaversion/maven)         | Via 系列加载器支持库       |
| [ViaVersion](https://github.com/ViaVersion/ViaVersion)        | 向上兼容支持             |
| [ViaBackwards](https://github.com/ViaBackwards/ViaBackwards)  | 向下兼容支持             |
| [OptiFine](https://github.com/sp614x/optifine)                | 画质优化               |
| [BetterFPS](https://github.com/mezz/BetterFps)                | FPS 优化工具           |
| [ASM](https://github.com/ow2/asm)                             | Java 字节码编辑器        |
| [OpenToL](https://github.com/kubik-hackathon/cubik-hackathon) | 模块框架支持             |
| [OpenXylitol](https://github.com/talting/OpenXylitol)         | 图形 & 渲染相关组件        |
| Foxsense-recode                                               | （暂未公开）             |

---

## 🧱 模块与事件开发示例

### ✅ 创建模块

````java
public class ExampleModule extends Module {
    public ExampleModule() {
        super("ExampleModule", Category.COMBAT, "这是一个示例模块");
    }
}
````

### 📤 注册事件

````java
//在ModuleManager#inti中
modules.put(ExampleModule.class, new ExampleModule());
````

### 📣 创建事件

````java
public class ExampleEvent extends CancellableEvent {
}
````

### 📤 调用事件

````java
ExampleEvent event = new ExampleEvent();
Client.INSTANCE.getEventManager().call(event);
````

## 📜 许可证说明 / License Notice

本项目采用自定义非商业许可证（Custom Non-Commercial License v1.2），禁止任何商业使用，包括但不限于销售、转售、授权或将本项目及其衍生作品用于任何形式的盈利活动。  

- 允许无限制的个人和非商业用途使用、复制、修改和分发。  
- 任何重新分发（包括发布衍生作品）必须事先获得开发者或仓库所有者书面许可。  
- 重新分发时必须提供完整源代码，并且修改必须遵守同样的许可证条款。  
- 保留原作者署名，禁止移除或篡改版权信息。  
- 违反规定者须支付违约金人民币50元，并被剥夺本仓库使用权。  
- 本软件按“现状”提供，不承担因使用本软件导致的任何损失责任。  

详情请参见仓库根目录下的 `LICENSE` 文件。

### 📘 补充条款 / Additional Terms

- ❗禁止任何人在 fork 或复制本项目后闭源分发，无论是否修改了源代码。任何 fork、clone、衍生项目 **必须保留完整源代码并公开可访问**。  

- ❗禁止对本项目进行任何形式的商业化处理，包括但不限于出售、收费授权、投广告、用于商业项目中等。  

- ❗禁止移除本项目 LICENSE、README、开发者署名与链接，否则视为侵权。  

- 🔁 若将本项目或衍生版本用于发布（即对外可下载/可使用），**必须保留此 LICENSE 和原始仓库链接**，且：
  - 必须完整开源；
  - 必须明确标识为修改版本；
  - 必须遵守本 LICENSE 中所有限制。
  
🎉 感谢所有开源项目的支持。
