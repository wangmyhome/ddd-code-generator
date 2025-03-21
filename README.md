# DDD代码生成器插件

这个IntelliJ IDEA插件用于根据Mapper文件自动生成DDD架构相关的代码，包括实体类、DTO、Gateway接口及实现、服务接口及实现。

## 功能特点

- 自动生成完整的DDD架构代码
- 直观的进度指示器，显示生成过程
- 从Mapper文件中提取信息创建相关代码

## 安装方法

1. 下载最新版本的插件：`ddd-code-generator/build/libs/ddd-code-generator-1.0-SNAPSHOT.jar`
2. 在IDEA中打开：File -> Settings -> Plugins -> ⚙️ -> Install Plugin from Disk...
3. 选择下载的jar文件并安装
4. 重启IDEA

## 使用方法

1. 在项目中右键点击一个Mapper接口文件
2. 选择"生成DDD代码"选项
3. 插件将自动生成所有相关DDD代码，并显示进度

## 配置

插件会自动根据项目结构和Mapper文件生成代码。如需修改默认配置，请参考Settings -> Tools -> DDD Code Generator设置。 