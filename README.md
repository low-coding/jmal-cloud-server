# [JmalCloud](https://github.com/jamebal/jmal-cloud-view) 个人网盘 服务端  [查看说明](https://github.com/jamebal/jmal-cloud-view/blob/master/README.md)

### 许可

[MIT](https://github.com/jamebal/jmal-cloud-view/blob/master/LICENSE) license.

Copyright (c) 2022-present jmal

## 打包docker镜像命令
    docker build --no-cache -t harbor.doublespring.top/ds-base/jmal-cloud-server-base:20230716-1 -f Dockerfile-base .
    docker build --build-arg VERSION=2.6.4 --no-cache -t harbor.doublespring.top/ds-base/jmal-cloud-server:20230716-1 -f Dockerfile-server .