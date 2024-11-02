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
- [Module Development](#module-development)
   - [Creating a Module](#creating-a-module)
   - [Module Configuration](#module-configuration)
   - [Loading and Managing Modules](#loading-and-managing-modules)
- [Configuration](#configuration)
- [Contributing](#contributing)
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

