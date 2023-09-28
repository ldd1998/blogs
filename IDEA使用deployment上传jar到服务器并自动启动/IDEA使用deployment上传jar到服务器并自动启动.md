# IDEA使用deployment上传jar到服务器并自动启动

## 前言

一般我们在开发中经常遇到需要手动将打好的jar包通过xshell等工具放到服务器上，然后手动执行停止原来jar包，启动新的jar包

这样就会很麻烦，因此我在网上找了一下有什么方便的办法，发现有下面几种

* **Jenkins**

  Jenkins是一个流行的开源自动化服务器，用于自动执行各种任务，包括构建、测试和部署

  但是使用jenkins需要做许多前置工作，比如部署Jenkins和配置Jenkins都是非常麻烦的。不够轻量化，稍微有一些学习成本，适合公司使用，对于个人不太友好。

* **IDEA中的deployment**

  相对于Jenkins它就非常轻量化了，而且是IDEA自带的工具，可以通过按一下按钮实现上传jar包到服务器，然后在服务器上通过脚本监听到有文件更新自动停止原来的程序并启动新的程序。下面将详细介绍如何进行配置

## 配置deployment

### 打开deployment的配置

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928230708.png?imageSlim)

### 选择SFTP

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928231024.png?imageSlim)



### 填写名称

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928231142.png?imageSlim)

### 新增服务器信息

![image-20230928231237543](/Users/macintoshhd/Library/Application Support/typora-user-images/image-20230928231237543.png)

这是已经填写好的服务器信息

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928231344.png?imageSlim)

### 配置上传路径

这里我们要填写打包完成后jar包所在目录和要上传到服务器的文件目录

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928231715.png?imageSlim)

填写好之后IDEA右下角会出现服务器信息

![image-20230928231524240](/Users/macintoshhd/Library/Application Support/typora-user-images/image-20230928231524240.png)

可以在这里右键选择要上传的文件进行上传

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928231841.png?imageSlim)

然后就可以成功上传到服务器了

但是到这里上传上去的文件还不能执行，我们需要配合脚本监听到文件上传然后自动停止和启动

### 自动停止启动脚本

> 需要注意替换里面的路径和jar包名称

```shell
#!/bin/bash

# 监视的文件路径
file_path="/home/ldd/demo/demo-1.0-SNAPSHOT.jar"

# 获取文件的初始inode号码和修改时间戳
initial_inode=$(stat -c %i "$file_path")
initial_mtime=$(stat -c %Y "$file_path")

# 持续监视文件变化
while true; do
    # 获取当前文件的inode号码和修改时间戳
    current_inode=$(stat -c %i "$file_path")
    current_mtime=$(stat -c %Y "$file_path")
    echo "$current_mtime"
    # 检查inode号码和修改时间戳是否变化
    if [ "$current_inode" != "$initial_inode" ] || [ "$current_mtime" -gt "$initial_mtime" ]; then
        echo "File has been replaced or modified."
        # 睡眠15秒，这里需要设置比文件上传时间稍微长一点，防止文件没有上传完成就执行了下面命令
        sleep 15
        # 停止正在运行的 Java 进程
        pkill -f "demo-1.0-SNAPSHOT.jar"
        chmod 777 demo-1.0-SNAPSHOT.jar
        # 启动 JAR 包
        nohup java -jar demo-1.0-SNAPSHOT.jar > nohup.out 2>&1 &
        # 更新初始inode号码和修改时间戳
        initial_inode=$current_inode
        initial_mtime=$current_mtime
    fi
    # 等待一段时间后再次检查
    sleep 2
done

```

将该脚本放到服务器上后台执行即可，我是放在了jar包所在目录，放到其他目录可能需要稍作调整

现在就已经完成了在IDEA中可以实现自动上传jar包然后停止启动的工作

## 设置顶部快捷图标

如果还想更快捷可以邮件IDEA上面菜单栏空白设置一个快捷图标

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928232534.png?imageSlim)

设置完成后效果如果所示，使用的时候也需要选中要上传的文件才可以点击并且IDEA右下角出现服务器信息也要被选中

![](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/20230928232816.png?imageSlim)
