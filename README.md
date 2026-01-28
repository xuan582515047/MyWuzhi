# 遇到的部署问题：
    ## 跨域问题：
        ① 没啥说的，nginx代理转发，超级恶心就是了
    ## Gradle怎么打包：
        ① 打包要用【gradle build】，才能生成完整的jar包（含依赖的）
        ② 如果你用【gradle jar】的话，生成的【xxx-plain.jar】是不包含依赖的
        ③ 生成的jar包在 build -> libs 下面
    ## mysql怎么改：
        ①【docker exec -it mysql bash】
        ②【mysql -u root -p】
    ## 怎么查看端口占用，怎么关闭进程：
        ①【fuser 8090/tcp】       // 查看 占用8090 的tcp端口 的进程 的进程号
        ②【fuser -k -9 8090/tcp】 // 关闭 占用8090 的tcp端口 的进程
    ## mysql 权限问题：
        ① 不知道为什么，明明开始的时候部署得好好的，结果莫名其妙就mysql不允许访问了
        ② 不管怎样，授权：
        ③【CREATE USER 'root'@'172.17.0.1' IDENTIFIED BY '1234';】
        ④【GRANT ALL PRIVILEGES ON wuzhi.* TO 'root'@'172.17.0.1';]
        ⑤【FLUSH PRIVILEGES;】
# 部署：
    ① 记得redis的IP改成localhost
    ② nginx监听80和8081端口，80给前端部署，8081转发到后端
    ③ 后端部署在8090
    ④ 文件访问暂时是直接访问的8090，后续要改成8081给nginx转发
    ⑤ 总的来说，开放80,8081,8090端口

# 表修改：
    ① 新增表：data_schedule，员工的日程

# 格式规范：
    ① 前后端日期传输，全部使用ISO格式但是不带毫秒和时区，也就是LocalDateTime能直接.parse的格式
    ② 也就是：yyyy-MM-dd'T'HH:mm:ss，比如【2026-01-23T17:34:31】

# 让AI生成建表语句：
    请根据我Server下所有的pojo下的po下的所有类，帮我写建表语句，放在根目录的database下面
    请注意mybatis-plus的逻辑删除字段，在 @application.yaml 里面
    请不要使用任何物理外键，我已经在代码中用逻辑外键处理完成了
    请根据每一个po类的构成，大概预测哪些字段需要加上索引
    所有的id相关字段，都用varchar(64)，对应去掉连字符的UUID