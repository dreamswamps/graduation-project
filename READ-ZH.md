# 多模态业务协同平台

> 一个基于`Vue3`和`Springboot`的多模态业务协同平台。实现了“待办事务”、“文章管理”、“申请与审批”和“用户权限管理”等功能于一体的综合管理系统。

## 语言

- [英文](README.md)
- [中文](README-ZH.md)

## ✨ 功能描述

1. **待办事务管理**
    - **状态管理**：标记事务“完成”和“收藏”两种状态，状态切换时加以动画反馈
    - **交互展示**：默认展示事务标题，展开/折叠详情时伴有背景渐变动画
    - **灵活排序**：支持拖拽修改事务位置，允许手动保存持久化排序结果
    - **防拖延机制**：限制7条待办数量，超过时禁用新增并给予提示
    - **智能时间**：新建待办时，时间范围默认为“当前时间至一小时后”
    - **空值处理**：若无待办事务，则显示占位图片

2. **用户权限管理**
    - **基础服务**：提供用户注册、登录、密码修改与重置等功能。
    - **请求身份验证**：对受限接口，使用`X-Current-User-ID`请求头信息识别用户身份并隔离数据
    - **批量操作**：管理员可以对用户信息进行批量软删除、批量撤回操作
    - **数据操作**：支持`Excel`格式导入导出用户数据

3. **文章管理**
    - **富文本编辑**：集成`wangEditor`编辑器，提供文章内容的格式化编辑能力
    - **权限控制**：用户可以“撰写文章”，管理员可以对文章进行批量管理

4. **申请与审批**
    - **[制作中]**
    - **示例：密码重置申请**：支持两种密码重置方式：其一，完成图片验证码，提交重置密码申请；其二，发送邮件验证码快速重置密码，跳过审批流程
    - **标准化申请**：根据不同的重置类型，填入用户信息，生成标准化申请单
    - **进度查询**：查询账号的历史申请记录及结果
    - **审批并发控制**：管理员审批申请时，系统自动锁定该申请，保证同时只有一个管理能审批，防止数据冲突
    - **超时释放**：管理员开始审批时进行计时，超过15分钟若未完成，触发心跳检测释放锁定资源。后端可释放所有异常的未释放锁定资源。
    - **审批追溯**：申请结果分为“未审批”/“已审批”，并且可追溯审批人和审批时间
    - **架构设计**：使用策略模式，方便后续的审批功能拓展

## 🚮 废弃功能

1. **Game模块**
    - 目标：一个仿照谷歌小恐龙的小游戏，支持保存分数和展示排行榜
    - 目前：仅实现简单的控制，暂停，碰撞检测

## 🚀 快速开始

### 环境要求

- `Node.js`: 20.18.2
- `Java`: 17
- `Spring Boot`: 3.5.5
- `MySQL`: 8.0.40

### 安装与运行

1. 克隆项目：
    ```bash
    git clone https://github.com/dreamswamps/graduation-project.git
    ```

2. 安装前端依赖：
    在/frontend文件夹下打开终端
    ```bash
    npm install
    ```

3. 安装后端依赖：
    IDEA可能自动完成这一步，按需执行
    在/backend文件下打开终端
    ```bash
    mvn clean install
    ```

4. 配置前端环境变量：<a id="frontend_env"></a>
    - 在/frontend目录下创建名为`.env`的文件
    - 编辑`.env`文件，填入以下内容：
    ```
    VITE_API_BASE_URL = 'http://localhost:8081'
    ```
    如果需要修改端口，则将8081替换为其他值

5. 配置后端环境变量：<a id="backend_env"></a>
    - 在/backend目录下创建名为`.env`的文件
    - 编辑`.env`文件，填入并修改以下内容：
    ```
    DB_USERNAME=<MySQL用户名>
    DB_PASSWORD=<MySQL密码>
    DB_URL=jdbc:mysql://localhost:3306/project?serverTimezone=Asia/Shanghai
    MAIL_HOST=smtp.qq.com
    MAIL_USERNAME=<QQ邮箱>
    MAIL_PASSWORD=<邮箱授权码>
    JWT_SECRET=<JWTBase64密钥>
    ```

6. 配置数据库：
    详见[db.sql](db.sql)

## 📖 使用说明 <a id="configuration"></a>

1. **后端服务**：
    - 将/backend文件夹移动到IDEA中打开
    - 运行SpringBoot项目

2. **前端服务**：
    - 将/frontend文件夹移动到vscode中打开
    - 在终端中输入以下内容
    ```bash
    npm run dev
    ```
    - 访问终端的网址

## 📁 项目结构

> 已剔除废弃部分。
> 省略Springboot中结构重复的部分。例如，Mapper中已有`A.xml`文件，则在Controller等层不在出现类似`A.java`文件。
> 如果出现`[ ]`表示该部分代码正在制作。

- 根目录
```
graduation-project/
├── 📁 backend      # 后端SpringBoot项目
├── 📁 frontend     # 前端Vue3项目
├── 📁 docs         # 项目文档
└── .gitignore
```

- 后端文件夹
```
backend/
├── 📄 files   # 用户上传文件（自动生成）
├── 📁 src/main
|   ├── 📁 java/com/example
|   |   ├── 📁 Controller
|   |   |   ├── FileController.java     # 用户文件操作
|   |   |   └── UtilController.java     # 专门使用/Util方法
|   |   |
|   |   ├── 📁 Service
|   |   |   ├── 📁 Strategy    # [ ] 申请相关策略模式
|   |   |   |   ├── ApplyStrategyMap.java
|   |   |   |   ├── ApprovalStrategy.java
|   |   |   |   └── PwdResetStrategy.java
|   |   |   |
|   |   |   ├── ApplyMapService.java    # [ ] 申请类型匹配
|   |   |   ├── RandomIntService.java   # 随机ID生成
|   |   |   └── UtilService.java        # Util方法业务
|   |   |
|   |   ├── 📁 POJO
|   |   |
|   |   ├── 📁 Mapper
|   |   |
|   |   ├── 📁 Exception
|   |   |   ├── CustomException.java    # 自定义异常类
|   |   |   └── GlobalExceptionHandler.java     # 异常处理
|   |   |
|   |   ├── 📁 Util
|   |   |   ├── 📁 ValidateCode
|   |   |   |   ├── EmailHandlerConfig.java     # 邮件发送类
|   |   |   |   ├── EmailHandlerCreator.java    # 邮件发送方法
|   |   |   |   ├── ValidateCodeConfig.java     # 图片验证码类
|   |   |   |   └── ValidateCodeCreator.java    # 图片验证码方法
|   |   |   |
|   |   |   ├── CorsConfig.java     # 跨域处理
|   |   |   ├── JWTManager.java     # JWT管理
|   |   |   └── Result.java         # 统一返回数据类
|   |   |
|   |   └── SpringBootDemoApplication.java
|   |
|   └── 📁 resources
|       ├── 📁 Mapper
|       |   ├── AdminAuthMapper.xml     # 用户身份认证
|       |   ├── AdminMapper.xml         # 用户信息
|       |   ├── ApplicationMapper.xml   # 用户申请
|       |   ├── ApplyMapper.xml         # 管理员审批
|       |   ├── ArticleMapper.xml       # 文章管理
|       |   └── TodoListMapper.xml      # 待办事务
|       |
|       ├── 📁 templates
|       |   └── EmailTemplate.html      # 邮件验证码模板
|       |
|       └── application.yml     # 属性配置
|
├── ⚙️ .env     # 环境配置（需手动创建）
└── pom.xml     # 依赖配置
```

- 前端文件夹
```
frontend/
├── 📁 public       # 少量图片资源
|
├── 📁 src
|   ├── 📁 assets       # 静态资源
|   |
|   ├── 📁 components/base  # 自定义封装组件
|   |   └── RippleCircleButton.vue  # 涟漪动效按钮
|   |
|   ├── 📁 router
|   |   └── index.js    # 路由管理
|   |
|   ├── 📁 utils
|   |   └── request.js  # 请求管理
|   |
|   ├── 📁 views
|   |   ├── 📁 Login
|   |   |   ├── 📁 ResetPwdCom              # 重置密码子页面
|   |   |   |   ├── CheckApplication.vue    # 查看申请
|   |   |   |   ├── InfoConfirm.vue         # 信息确认
|   |   |   |   ├── InfoGather.vue          # 信息收集
|   |   |   |   ├── StepResult.vue          # 结果展示
|   |   |   |   └── VirefyCode.vue          # 验证码检验
|   |   |   |
|   |   |   ├── Forgetpwd.vue   # 重置密码页面
|   |   |   ├── Login.vue       # 登录页面
|   |   |   └── Register.vue    # 注册页面
|   |   |
|   |   ├── 404.vue             # 未知路由跳转
|   |   ├── Admin.vue           # 用户信息管理
|   |   ├── Article.vue         # 文章管理
|   |   ├── Board.vue           # 待办事务
|   |   ├── Data.vue            # 数据管理
|   |   ├── ElementTest.vue     # 测试页
|   |   ├── Home.vue            # 测试页
|   |   ├── Manager.vue         # 背景父组件
|   |   └── UserInfo.vue        # 个人信息
|   |
|   ├── App.vue
|   └── main.js
|
├── ⚙️ .env         # 环境配置（需手动创建）
├── index.html
├── jsconfig.json
├── package-lock.json
├── package.json
└── vite.config.js
```
