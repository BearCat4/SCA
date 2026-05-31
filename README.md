# THSSCA

基于 Trivy 的 SCA 系统 V1。后端使用 Spring Boot，前端使用 Vue，数据库使用 PostgreSQL。

## 功能

- Git 仓库依赖扫描
- 本地目录直接扫描
- 本机 Trivy CLI 扫描引擎
- 用户登录和项目权限
- REST Token CI 触发扫描
- 漏洞、组件、许可证结果展示
- JSON 和 HTML 报告

## 开发启动

默认使用 PostgreSQL，数据保存在 PG 数据库中，后端重启不会丢失：

```bash
docker compose up -d postgres
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

本机如果没有 Docker，也可以使用已安装的 PostgreSQL。默认连接参数：

- 地址：`localhost:5432`
- 数据库：`sca`
- 用户：`sca`
- 密码：`sca`

`local` profile 只是备用的嵌入式 H2 文件库，适合临时演示，不作为首选运行方式：

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local
```

默认管理员：

- 用户名：`admin`
- 密码：`admin123`

后端默认要求本机存在 `trivy` 和 `git` 命令。可通过环境变量覆盖：

```bash
TRIVY_BIN=/opt/homebrew/bin/trivy
SCA_WORK_DIR=/tmp/sca-work
SCA_SCAN_TIMEOUT_SECONDS=600
SCA_TRIVY_SKIP_DB_UPDATE=true
```

项目的“Git URL / 本地路径”字段可以填写：

- `https://github.com/WebGoat/WebGoat.git`
- `/Users/yebaolin/Desktop/nacos-develop`
- `file:///Users/yebaolin/Desktop/nacos-develop`

当字段是本机已存在目录时，后端会跳过 `git clone`，直接对该目录执行 Trivy 扫描。

默认会跳过 Trivy DB 更新，避免首次扫描被外网镜像下载卡住。需要更新漏洞库时，可先在终端单独执行：

```bash
trivy fs --download-db-only --db-repository ghcr.io/aquasecurity/trivy-db:2 .
```
