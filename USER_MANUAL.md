# EasyFK Generator IDEA 插件使用手册

> **版本**: 1.0.0  
> **适用平台**: IntelliJ IDEA 2024.3+  
> **JDK 要求**: Java 21  
> **框架版本**: EasyFK 3.2.12

---

## 目录

- [1. 概述](#1-概述)
- [2. 安装与配置](#2-安装与配置)
  - [2.1 安装插件](#21-安装插件)
  - [2.2 全局设置](#22-全局设置)
- [3. 快速入门](#3-快速入门)
- [4. 插件入口](#4-插件入口)
  - [4.1 菜单入口](#41-菜单入口)
  - [4.2 工具窗口](#42-工具窗口)
  - [4.3 快捷键](#43-快捷键)
- [5. 生成器对话框](#5-生成器对话框)
  - [5.1 项目配置](#51-项目配置)
  - [5.2 模型配置](#52-模型配置)
  - [5.3 代码配置](#53-代码配置)
  - [5.4 操作按钮](#54-操作按钮)
- [6. 生成模式详解](#6-生成模式详解)
  - [6.1 全量生成（新建项目）](#61-全量生成新建项目)
  - [6.2 增量生成](#62-增量生成)
  - [6.3 刷新模型](#63-刷新模型)
  - [6.4 仅生成业务代码](#64-仅生成业务代码)
  - [6.5 仅刷新 DTO/Param](#65-仅刷新-dtoparam)
  - [6.6 生成自动装配配置](#66-生成自动装配配置)
- [7. 数据库导入](#7-数据库导入)
  - [7.1 支持的数据库](#71-支持的数据库)
  - [7.2 连接配置](#72-连接配置)
  - [7.3 导入表结构](#73-导入表结构)
- [8. 配置文件](#8-配置文件)
  - [8.1 项目配置文件](#81-项目配置文件)
  - [8.2 加载与保存](#82-加载与保存)
- [9. 典型使用场景](#9-典型使用场景)
- [10. 常见问题](#10-常见问题)

---

## 1. 概述

EasyFK Generator 是一款 IntelliJ IDEA 插件，用于基于 EasyFK 框架快速生成项目骨架和业务代码。主要能力包括：

- **一键生成项目骨架**：自动创建符合 EasyFK 框架规范的完整项目结构
- **生成 Entity / Mapper**：根据数据库表结构或手动定义的模型，生成 JPA 实体类和数据访问层
- **生成业务代码**：自动生成 Repository、Service、API、Controller、Remote 等分层业务代码
- **生成自动装配配置**：生成 Spring Boot AutoConfiguration 配置类
- **支持多种项目架构**：Single（单体）、Microservice（微服务）、Smart（多栈微服务）
- **支持多种构建工具**：Maven、Gradle（Groovy DSL / Kotlin DSL）
- **支持多种 ORM 框架**：MyBatis、MyBatis-Flex、Hibernate
- **支持从数据库导入表结构**：MySQL、PostgreSQL、Oracle

---

## 2. 安装与配置

### 2.1 安装插件

1. 打开 IntelliJ IDEA，进入 **File → Settings → Plugins**
2. 选择 **Install Plugin from Disk...**
3. 选择构建好的插件 ZIP 包进行安装
4. 重启 IDE 使插件生效

### 2.2 全局设置

安装完成后，进入 **File → Settings → Tools → EasyFK Generator** 进行全局默认参数配置：

| 设置项 | 说明 | 默认值 |
|--------|------|--------|
| **默认作者** | 生成代码中 `@author` 注解的作者名 | `eb-jack` |
| **默认框架版本** | EasyFK 框架版本号 | `3.2.12` |
| **生成后自动刷新项目树** | 代码生成完成后是否自动刷新 IDEA 项目目录 | 开启 |
| **生成后弹出结果统计** | 生成完成后是否弹出结果信息对话框 | 开启 |

> 这些全局设置会作为每次打开生成器对话框时的默认值，可在对话框中按需覆盖。

---

## 3. 快速入门

以创建一个全新的单体项目为例：

1. **打开生成器**：菜单 **File → New → EasyFK Project**，或在右侧工具窗口点击 "全量生成"
2. **填写项目配置**：输入项目名称、GroupId、包根路径，选择项目输出目录
3. **配置模型**：切换到"模型配置"页签，连接数据库并导入表结构（或手动添加）
4. **配置代码选项**：切换到"代码配置"页签，填写模块名称
5. **开始生成**：
   - 点击底部 **"生成项目"** 按钮 → 生成项目骨架
   - 点击 **"生成模型"** 按钮 → 生成 Entity/Mapper
   - 点击 **"生成业务代码"** 按钮 → 生成 Repository/Service/API/Controller
   - 点击 **"生成自动装配"** 按钮 → 生成 Spring Boot AutoConfiguration
6. **关闭对话框**：点击"关闭"按钮保存配置并退出

---

## 4. 插件入口

### 4.1 菜单入口

插件在 IDEA 菜单中注册了以下入口：

**新建项目（New 菜单）**

| 菜单路径 | 说明 |
|---------|------|
| File → New → **EasyFK Project** | 打开全量生成对话框，创建新项目 |

**增量操作（Generate 菜单，需在已有项目内右键触发）**

| 菜单路径 | 说明 |
|---------|------|
| Generate → **EasyFK 增量生成** | 对已有项目增量生成 Entity/Mapper + 业务代码 |
| Generate → **EasyFK 生成业务代码** | 仅生成 Repository/Service/API/Remote/Controller |
| Generate → **EasyFK 刷新模型 (字段变更)** | 表字段变更后重新生成 Entity/Mapper + DTO/Param |
| Generate → **EasyFK 刷新 DTO/Param** | 仅重新生成 DTO 和 Param 类 |
| Generate → **EasyFK 生成自动装配** | 生成 Spring Boot AutoConfiguration |

> Generate 菜单可通过在编辑器中右键 → Generate，或使用快捷键 `Alt + Insert` 打开。

### 4.2 工具窗口

插件在 IDEA 右侧注册了 **EasyFK** 工具窗口，提供可视化的操作面板。工具窗口按功能分为三个区域：

**新建项目**
- `全量生成 (项目骨架 + 全部代码)` — 创建全新项目

**增量生成（已有项目）**
- `增量生成 (新增表: Entity + 业务代码)` — 为新增的表生成模型和代码
- `刷新模型 (字段变更: Entity + DTO/Param)` — 当表字段变更时刷新模型

**单独生成**
- `仅生成业务代码` — 只生成业务层代码
- `仅刷新 DTO / Param` — 只更新数据传输对象
- `生成自动装配配置` — 生成 Spring Boot 自动配置

### 4.3 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl + Alt + O` | EasyFK 增量生成 |

---

## 5. 生成器对话框

所有操作最终都通过统一的生成器对话框完成。对话框包含三个配置页签和多个操作按钮。

### 5.1 项目配置

项目配置页签用于定义项目的基本信息和技术选型。

**基本信息（必填）**

| 字段 | 说明 | 示例 |
|------|------|------|
| 项目名称 * | 项目（工程）名称 | `my-shop` |
| GroupId * | Maven/Gradle 的 Group ID | `com.example` |
| 包根路径 * | Java 包的根路径 | `com.example.shop` |
| 项目目录 * | 项目输出的父目录（可通过浏览按钮选择） | `D:\workspace` |

**版本信息**

| 字段 | 说明 | 默认值 |
|------|------|--------|
| 框架版本 | EasyFK 框架版本号 | `3.2.12` |
| 项目版本 | 项目自身版本号 | `1.0.0-SNAPSHOT` |

**技术选型**

| 字段 | 可选值 | 说明 |
|------|--------|------|
| 项目类型 | **Single**（单体） / **Microservice**（微服务） / **Smart**（多栈微服务） | 决定项目整体架构 |
| 构建工具 | **Maven** / **Gradle** | 选择 Gradle 时可进一步选择 Groovy 或 Kotlin DSL |
| Gradle DSL | **Groovy** / **Kotlin** | 仅在选择 Gradle 时显示 |
| ORM 框架 | **MYBATIS** / **MYBATIS_FLEX** / **HIBERNATE** | 数据库访问框架 |
| RPC 类型 | 可选的远程调用类型 | 仅在 Microservice / Smart 类型时显示 |
| PRD 策略 | **SINGLE** 等 | 部署策略 |
| 应用类型 * | **BMS**（后台管理端） / **CLIENT**（C端） | 必须选择 |
| 日志框架 | **Logback** / **Log4j2** | 日志实现 |

**业务模块**

可勾选需要包含的预置业务模块：

`auth` · `banner` · `brand` · `category` · `vip` · `user` · `dict` · `goods` · `group` · `tag` · `merchant` · `order` · `trading` · `payment` · `pickup` · `container` · `media`

### 5.2 模型配置

模型配置页签用于定义数据模型（Entity），支持两种创建方式。

**创建方式选择**

- **从数据库导入**（默认）：连接数据库自动读取表结构
- **手动创建模型**：在模型列表中手动添加和编辑

**数据库连接面板**（从数据库导入时显示）

| 字段 | 说明 | 示例 |
|------|------|------|
| 数据库类型 | MySQL / PostgreSQL / Oracle / Oracle 12c | `MySQL` |
| 连接地址 | 数据库的 `host:port/database` 格式 | `localhost:3306/my_database` |
| 用户名 | 数据库用户名 | `root` |
| 密码 | 数据库密码 | |
| 表前缀 | 自动去除的表名前缀 | `t_` |
| 指定表名 | 限定导入的表名，多个用逗号分隔；留空则显示全部 | `t_user,t_order` |

> 填写连接信息后可点击 **"测试连接"** 验证数据库是否可达。

**模型列表**

| 列 | 说明 | 是否可编辑 |
|----|------|-----------|
| Model 名称 | 生成的实体类名（大驼峰） | 是 |
| 表名 | 对应的数据库表名 | 是 |
| 说明 | 模型备注 / 表备注 | 是 |
| ID 类型 | 主键类型：`String` / `Long` / `Integer` | 是 |
| 仅 Repository | 是否只生成 Repository，不生成 Service 等 | 是 |
| 生成 Controller | 是否生成 Controller 层 | 是 |

可通过工具栏的 `+` / `-` 按钮手动添加或删除模型行。

### 5.3 代码配置

代码配置页签包含两个子标签页。

**子标签页一：生成业务代码**

| 字段 | 说明 | 默认值 |
|------|------|--------|
| 模块名称 * | 业务模块名（用于包路径和目录） | |
| 作者 | 代码注释中的作者 | 从全局设置读取 |
| ORM 类型 | 覆盖项目配置中的 ORM 选择 | `MYBATIS` |

**生成选项（复选框）**

| 选项 | 说明 | 默认 |
|------|------|------|
| 使用 Spring 注解 | 生成的代码是否使用 `@Service`、`@Repository` 等注解 | 开启 |
| 生成 Controller | 是否生成 Controller 层 | 开启 |
| 继承父类 | 是否让生成的类继承框架父类 | 开启 |
| 生成资源权限注解 | 是否生成 `@Resource` 资源权限注解 | 关闭 |

**子标签页二：更新领域模型**

用于仅更新指定模型的 DTO（Data Transfer Object）和 Param（请求参数）类。在列表中添加需要更新的 **Model 名称**，系统只刷新对应的领域模型文件，不会修改业务代码。

### 5.4 操作按钮

对话框底部提供以下操作按钮：

**左侧操作按钮（分步生成）**

| 按钮 | 功能 |
|------|------|
| **生成项目** | 仅生成项目骨架目录和构建文件（pom.xml / build.gradle 等） |
| **生成模型** | 生成 Entity 实体类 + Mapper 接口/XML |
| **生成业务代码** | 生成 Repository / Service / API / Remote / Controller |
| **生成自动装配** | 生成 Spring Boot AutoConfiguration 配置 |

**右侧按钮**

| 按钮 | 功能 |
|------|------|
| **关闭** | 保存当前配置到项目目录，并关闭对话框 |
| **取消** | 不保存，直接关闭对话框 |

**顶部辅助按钮**

| 按钮 | 功能 |
|------|------|
| **加载配置...** | 从文件系统选择 `.easyfk-generator.json` 配置文件加载 |
| **重置配置** | 将所有配置项恢复为默认值 |

---

## 6. 生成模式详解

### 6.1 全量生成（新建项目）

**入口**: File → New → EasyFK Project | 工具窗口 → 全量生成

**适用场景**: 从零开始创建一个新的 EasyFK 项目。

**操作流程**:
1. 填写完整的项目配置
2. 配置数据库连接并导入表结构
3. 填写代码配置
4. 依次点击：生成项目 → 生成模型 → 生成业务代码 → 生成自动装配

**生成产物**:
- 完整的项目目录结构（多模块 Maven/Gradle 项目）
- 所有配置文件（application.yml、pom.xml 等）
- Entity / Mapper / Repository / Service / API / Controller / Remote
- Spring Boot AutoConfiguration

### 6.2 增量生成

**入口**: Generate → EasyFK 增量生成 | `Ctrl + Alt + O` | 工具窗口 → 增量生成

**适用场景**: 已有项目中新增了数据库表，需要为新表生成完整的模型和业务代码。

**操作流程**:
1. 在模型配置页签中导入或手动添加新的表/模型
2. 填写代码配置中的模块名称
3. 点击 "生成模型" 和 "生成业务代码"

### 6.3 刷新模型

**入口**: Generate → EasyFK 刷新模型 (字段变更) | 工具窗口 → 刷新模型

**适用场景**: 数据库表的字段发生了变更（新增、删除、修改字段），需要重新生成 Entity 和 DTO/Param，但不影响已有的业务代码。

**注意**: 此操作会重新生成 Entity、Mapper、DTO、Param，但 **不会** 修改 Service、Controller 等业务层代码。

### 6.4 仅生成业务代码

**入口**: Generate → EasyFK 生成业务代码 | 工具窗口 → 仅生成业务代码

**适用场景**: Entity/Mapper 已经存在，只需要生成 Repository、Service、API、Remote、Controller 等业务层代码。

### 6.5 仅刷新 DTO/Param

**入口**: Generate → EasyFK 刷新 DTO/Param | 工具窗口 → 仅刷新 DTO / Param

**适用场景**: 只需要更新指定模型的 DTO 和 Param 类（例如字段变更后只影响数据传输对象）。

**操作步骤**:
1. 切换到代码配置 → "更新领域模型"子标签页
2. 添加需要更新的 Model 名称
3. 点击 "生成业务代码"

### 6.6 生成自动装配配置

**入口**: Generate → EasyFK 生成自动装配 | 工具窗口 → 生成自动装配配置

**适用场景**: 需要单独生成或重新生成 Spring Boot AutoConfiguration 自动装配配置。

---

## 7. 数据库导入

### 7.1 支持的数据库

| 数据库类型 | JDBC 驱动 | 连接地址格式 |
|-----------|----------|-------------|
| MySQL | `com.mysql.cj.jdbc.Driver` | `host:port/database` |
| PostgreSQL | `org.postgresql.Driver` | `host:port/database` |
| Oracle | `oracle.jdbc.OracleDriver` | `host:port:SID` 或 `host:port/service` |
| Oracle 12c | `oracle.jdbc.OracleDriver` | 同 Oracle |

### 7.2 连接配置

1. 在模型配置页签中选择"从数据库导入"
2. 选择数据库类型
3. 填写连接地址（只需 `host:port/database` 部分，插件会自动拼接完整 JDBC URL）
4. 输入用户名和密码
5. 点击 **"测试连接"** 验证连通性

### 7.3 导入表结构

1. 完成数据库连接配置后，点击 **"从数据库导入表..."**
2. 在弹出的导入对话框中：
   - 查看数据库中所有表的列表（含表名和备注）
   - 可修改"表前缀"用于自动去除前缀并生成 Model 名称
   - 勾选需要导入的表
3. 点击 **"导入选中"**，选中的表将添加到模型列表

> **表前缀处理示例**: 若表前缀为 `t_`，表名 `t_user_order` 将自动生成 Model 名称 `UserOrder`。

---

## 8. 配置文件

### 8.1 项目配置文件

插件会在项目根目录自动保存配置文件 **`.easyfk-generator.json`**，包含以下信息：

```json
{
  "project": {
    "projectName": "my-shop",
    "groupId": "com.example",
    "basePackage": "com.example.shop",
    "projectDir": "D:\\workspace",
    "frameworkVersion": "3.2.12",
    "projectVersion": "1.0.0-SNAPSHOT",
    "projectType": "SINGLE",
    "buildType": "MAVEN",
    "ormType": "MYBATIS",
    "appType": "BMS",
    "logType": "LOGBACK",
    "modules": ["user", "order"]
  },
  "code": {
    "moduleName": "user",
    "author": "eb-jack",
    "ormType": "MYBATIS",
    "springAnnotation": true,
    "createController": true,
    "extendsSupperClass": true,
    "createResourceAnnotation": false
  },
  "db": {
    "dbType": "MYSQL",
    "dbShortUrl": "localhost:3306/my_database",
    "dbUser": "root",
    "tablePrefix": "t_"
  }
}
```

### 8.2 加载与保存

- **自动保存**: 点击对话框的"关闭"按钮，或执行任一生成操作后，配置会自动保存到项目目录
- **自动加载**: 再次打开对话框时，如果当前 IDEA 项目根目录下存在 `.easyfk-generator.json`，会自动加载配置
- **手动加载**: 点击对话框顶部 **"加载配置..."** 按钮，可从任意位置选择配置文件
- **全局记忆**: 上次使用的项目目录、GroupId、包路径、数据库连接等信息会持久化到 IDEA 全局设置中，下次新建项目时自动填充

---

## 9. 典型使用场景

### 场景一：创建全新项目

```
File → New → EasyFK Project
  → 填写项目配置（名称、GroupId、包路径、目录）
  → 选择技术栈（Single + Maven + MyBatis + BMS）
  → 配置数据库并导入表结构
  → 填写模块名称
  → 生成项目 → 生成模型 → 生成业务代码 → 生成自动装配
```

### 场景二：已有项目新增业务表

```
在项目中右键 → Generate → EasyFK 增量生成 (Ctrl+Alt+O)
  → 连接数据库，导入新增的表
  → 生成模型 → 生成业务代码
```

### 场景三：数据库字段变更

```
Generate → EasyFK 刷新模型 (字段变更)
  → 导入变更的表（或手动修改模型列表）
  → 生成模型（重新生成 Entity/Mapper/DTO/Param）
```

### 场景四：仅更新某个模型的 DTO

```
Generate → EasyFK 刷新 DTO/Param
  → 代码配置 → "更新领域模型"标签页
  → 添加需要更新的 Model 名称
  → 生成业务代码
```

### 场景五：团队共享配置

将项目根目录下的 `.easyfk-generator.json` 提交到版本控制。团队成员拉取代码后，打开生成器对话框会自动加载配置，确保配置一致。

---

## 10. 常见问题

**Q: 插件在菜单中找不到 Generate 菜单项？**  
A: Generate 菜单下的操作需要在已有项目上下文中使用。确保当前打开了一个项目，并在编辑器中右键 → Generate 查看。

**Q: 数据库连接测试失败？**  
A: 请检查：①连接地址格式是否正确（无需 `jdbc:` 前缀）；②数据库服务是否正常运行；③用户名密码是否正确；④如果是远程数据库，检查防火墙和网络连接。

**Q: 导入的 Model 名称不符合预期？**  
A: 检查"表前缀"设置是否正确。导入后也可以直接在模型列表中双击编辑 Model 名称。

**Q: 生成的代码覆盖了我的手动修改？**  
A: Entity、Mapper、DTO、Param 等是可重复生成的代码，每次刷新会覆盖。如需自定义逻辑，请在 Service 等业务层代码中编写，业务层代码只在首次生成时创建，不会被覆盖。

**Q: 如何切换已有项目的 ORM 框架？**  
A: 在代码配置页签中修改 ORM 类型下拉框即可，但建议在项目初始化时确定 ORM 框架，中途切换可能需要手动处理兼容性问题。

**Q: 配置文件 `.easyfk-generator.json` 应该加入版本控制吗？**  
A: 推荐加入。该文件不包含敏感信息（数据库密码不保存），可以帮助团队成员保持配置一致。

**Q: 支持哪些 IDEA 版本？**  
A: 支持 IntelliJ IDEA 2024.3 及以上版本（包括 Community 和 Ultimate 版本）。

---

*本手册基于 EasyFK Generator 插件 v1.0.0 编写。如有疑问，请联系 作者本人*
