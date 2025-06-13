# 🎮 Tenacity Recode

[Switch to 中文版](README_CN.md)

## 🧩 Client Information

This client is based on the [zitalem/Tenacity](https://github.com/zitalem/Tenacity) Recode, originally an open source hacked client for Minecraft.

Compared to Tenacity 5.1, we have made the following improvements:

- 🔧 **Higher LWJGL Version**: Better optimization experience
- ☕ **JDK 18**: Support for more modern syntax
- 🧹 **Removed Unused APIs**: Such as Internet Store, Spotify, Kinggen (significantly reduces size and improves performance, while keeping the Script system — something other JDK 18 forks cannot do)
- 👤 **Optimized AltManager**: Retains original UI, enhanced Microsoft login logic
- 🔁 **Updated ViaMCP**: Supports the latest Minecraft versions
- ⚙️ **Stronger EventManager**: Uses [cubk1/EventManager](https://github.com/cubk1/EventManager), simpler syntax, better performance
- 🈶 **Own UnicodeFontRenderer**: Handwritten, no AI, no plagiarism, outstanding performance
- 🚀 **FPSBooster / FastLoader**: Startup speed compressed to about 5 seconds and improves game FPS
- 🌐 **I18N Multilingual System**: Supports internationalization

---
## 📦 Libraries / Repos Used

| Name                                                           | Description          |
|---------------------------------------------------------------|---------------------|
| [cubk1/EventManager](https://github.com/cubk1/EventManager)   | High performance event system |
| [LWJGL](https://github.com/LWJGL/lwjgl3)                      | Java game development library |
| [nashorn-core](https://github.com/OpenJDK/nashorn)            | JavaScript engine    |
| [FFmpeg](https://github.com/FFmpeg/FFmpeg)                    | Multimedia decoder   |
| [JavaCPP](https://github.com/bytedeco/javacpp)                | Java/C++ bridge library |
| [JavaCV](https://github.com/bytedeco/javacv)                  | Video/image processing |
| [jflac](https://github.com/jflac-player/jflac)                | FLAC decoder         |
| [Lombok](https://github.com/projectlombok/lombok)             | Auto-generated Getter/Setter |
| [OkHttp](https://github.com/square/okhttp)                    | High performance HTTP client |
| [mojang-authlib](https://github.com/Mojang/authlib)           | Mojang authentication library |
| [commons-lang](https://github.com/apache/commons-lang)        | Apache utilities     |
| [Gson](https://github.com/google/gson)                        | JSON serialization   |
| [JCodec](https://github.com/jcodec/jcodec)                    | Java video codec     |
| [JInput](https://github.com/jinput/jinput)                    | Java controller input |
| [JNA](https://github.com/java-native-access/jna)              | Native method invocation support |
| [JOpt Simple](https://github.com/jopt-simple/jopt-simple)     | Command line argument parsing |
| [Log4j](https://github.com/apache/log4j)                      | Logging system       |
| [Vecmath](https://github.com/notaz/vecmath)                   | Vector math library  |
| [nv-i18n](https://github.com/NVlabs/nv-i18n)                  | Internationalization support |
| [OpenJDK](https://github.com/openjdk/jdk)                     | Java open source implementation |
| [Rise6.0](https://github.com/ZeathDev/Rise6.0-Src)            | Rise client source   |
| [Moonlight](https://github.com/randomguy3725/MoonLight)       | Moonlight client components |
| [Untitled](https://github.com/ChengF3ng233/Untitled)          | Modular feature integration |
| [JSObject](https://github.com/holoisme/Fox)                   | JavaScript module support |
| [ViaMCP](https://github.com/CloudburstMC/ViaMCP)              | Minecraft cross-version support |
| [ViaLoadingBase](https://github.com/Viaversion/maven)         | Via series loader support library |
| [ViaVersion](https://github.com/ViaVersion/ViaVersion)        | Upwards compatibility |
| [ViaBackwards](https://github.com/ViaBackwards/ViaBackwards)  | Downwards compatibility |
| [OptiFine](https://github.com/sp614x/optifine)                | Graphics optimization |
| [BetterFPS](https://github.com/mezz/BetterFps)                | FPS optimization tool |
| [ASM](https://github.com/ow2/asm)                             | Java bytecode editor |
| [OpenToL](https://github.com/kubik-hackathon/cubik-hackathon) | Module framework support |
| [OpenXylitol](https://github.com/talting/OpenXylitol)         | Graphics & rendering components |
| Foxsense-recode                                               | (Not public yet)     |

---

## 🧱 Module and Event Development Examples

### ✅ Create Module

````java
public class ExampleModule extends Module {
    public ExampleModule() {
        super("ExampleModule", Category.COMBAT, "This is an example module");
    }
}
````

### 📤 Register Event

````java
// Inside ModuleManager#init
modules.put(ExampleModule.class, new ExampleModule());
````

### 📣 Create Event

````java
public class ExampleEvent extends CancellableEvent {
}
````

### 📤 Call Event

````java
ExampleEvent event = new ExampleEvent();
Client.INSTANCE.getEventManager().call(event);
````

## 📜 License Notice

This project uses a Custom Non-Commercial License v1.2, prohibiting any commercial use, including but not limited to sales, resale, licensing, or any form of profit activity involving this project or its derivatives.

- Unlimited personal and non-commercial use, copying, modifying, and distributing is allowed.  
- Any redistribution (including derivative works) requires prior written permission from the developer or repository owner.  
- Redistribution must include full source code, and modifications must comply with the same license terms.  
- Original author attribution must be retained; removal or alteration of copyright information is prohibited.  
- Violators must pay a penalty of RMB 50 and will lose the right to use this repository.  
- This software is provided "as is" without any warranty for losses caused by its use.

Please see the `LICENSE` file in the root directory for details.


### 📘 Additional Enforcement Terms

- ❗Forking, cloning, or redistributing this repository without **keeping it open-source** is strictly prohibited.  
  Any redistributed or derived version must remain **fully open-source and publicly accessible**.

- ❗Commercial use in any form (e.g. sale, monetization, ads, use in paid services) is strictly forbidden.

- ❗Removal or modification of this LICENSE, README, or developer attribution is prohibited and constitutes a violation.

- 🔁 If you **redistribute or publish** any version of this project (even modified), you must:
  - Keep the entire source code public and accessible;
  - Clearly state it's a modified version;
  - Include this LICENSE and the link to the original repository;
  - Fully comply with all the terms in this license.

🎉 Thanks to all open source projects for their support.
