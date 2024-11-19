<div align='center'>

<h1>MoBot</h1>

![image](https://github.com/user-attachments/assets/ac8ec29f-fb02-45c3-aba3-0bf985a935fc)
<h4> </span> <a href="https://mobot.siea.dev/mudular"> Modular Discord Bot </a> <span> · </span> <a href="https://github.com/orgs/VitacraftOrg/MoBot/issues"> Report Bug </a> <span> · </span> <a href="https://github.com/orgs/VitacraftOrg/MoBot/issues"> Request Feature </a> </h4>
<br>
</div>

## Overview

MoBot is a modular, extensible bot framework designed for Discord.
With MoBot, developers can create and manage custom modules that hook into MoBot,
allowing for rich, interactive features to be added seamlessly.
Each module is loaded independently and can contain its own configuration, making it easy to extend and customize.

##  Table of Contents
- [Roadmap](#roadmap)
- [Features](#features)
- [Installation](#installation)
[Usage](#usage)
   - [Getting Started](#getting-started)
   - [Creating a Module](#creating-a-module)
   - [Module Configuration](#module-configuration)
   - [Loading and Managing Modules](#loading-and-managing-modules)
- [Module Development](#module-development)
   - [Creating a Module](#creating-a-module)
   - [Module Configuration](#module-configuration)
   - [Loading and Managing Modules](#loading-and-managing-modules)
- [License](#license)

## Roadmap
- [x] Modules
  - [x] Load modules
  - [x] Load module information from `module.yml`
  - [x] Execute `preEnable`, `onEnable`, `onDisable` and `postDisable`
  - [x] Account for priority when enabling
- [ ] Convenient Addons
   - [x] Slash Command Addon
   - [ ] ????
- [x] Module Configs


## Features

- **Modular Architecture:** Create and load custom modules easily.
- **Discord Integration:** Built-in support for Discord features like slash commands and event listeners.
-
- **Flexible Configuration:** Each module can have its own `config.yml`, stored in a structured directory.
- **Logging:** Centralized logging for each module, making it easy to debug and monitor activity.
- **Error Reporting:** Identify which module errors are coming from, with detailed logging.


## Module Development

### Creating a Module

To create a module for MoBot, you need to extend the `MBModule` class. Below is an example of a simple module that logs a message when it is enabled and disabled.

First, add the MoBot dependency to your `pom.xml`:

```xml
<repository>
  <id>pixel-services-releases</id>
  <name>Pixel Services</name>
  <url>https://maven.pixel-services.com/releases</url>
</repository>

<dependency>
    <groupId>net.vitacraft</groupId>
    <artifactId>MoBot</artifactId>
    <version>VERSION</version> <!-- Replace VERSION with the latest version -->
</dependency>
```

Next, create a new class that extends `MBModule`. Here is an example:

```java
package dev.siea;

import net.vitacraft.api.MBModule;

public class WelcomeModule extends MBModule {
    @Override
    public void onEnable() {
        // Log a message when the module is enabled
        getLogger().info("WelcomeModule enabled!");
    }

    @Override
    public void onDisable() {
        // Log a message when the module is disabled
        getLogger().info("WelcomeModule disabled!");
    }
}
```

Last but not least your bot needs a `bot.yml` file in your `resources` directory with information about the bot:

```yaml
name: WelcomeModule
version: '${project.version}'
description: A Test Module
dependencies: [ANOTHER_MODULE]
authors: [sieadev]
priority: DEFAULT
```

### Module Configuration

Each module can have its own configuration file. You can load the configuration using the `getConfigLoader` method. If no file name is passed, it defaults to `config.yml`. Here is an example:

```java
@Override
public void onEnable() {
    // Load the default configuration (config.yml)
    ConfigLoader config = getConfigLoader();
    // Log a message when the module is enabled
    getLogger().info("Hello World!");
}
```

### Loading and Managing Modules

Modules are loaded and managed by MoBot. You can use the `getModuleInfo` method to get information about the module, such as its name and version. Here is an example:

```java
@Override
public void onEnable() {
    // Get module information
    ModuleInfo info = getModuleInfo();
    // Log the module name and version
    getLogger().info("Hey from: module - " + info.getName() + " v" + info.getVersion());
}
```