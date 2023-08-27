# MySQL中Innodb引擎不通插入方式对速度的影响

## 摘要

最近在研究如何插入大量数据到MySQL中的Innodb数据库中，后来查阅资料发现有三种方法

- **使用Mybatis**
- **使用JDBC**
- **使用JDBC批处理**

其中每种方法还可选以下操作

- **单线程**  或 **多线程**
- **开启事务** 或 **关闭事务**

现在对这三种方法做对比分析。

## 测试环境

``` 
宿主机：i7-12700（因虚拟机优先选择小核，所以关闭所有小核）、内存：32G、固态：致态TiPlus7100 2T
虚拟机：VMware、6核8G、centos7、docker中安装MySQL 8.0、my.conf为空。
jdbc连接：url: jdbc:mysql://192.168.193.101:3306/demos?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
```

**建表语句**

```sql
CREATE TABLE `user`  (
  `id` varchar(32),
  `name` varchar(255),
  `age` int NULL DEFAULT NULL,
  `create_time` varchar(32) ,
  `update_time` varchar(32)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
```

## 测试用例

### MyBatis-Plus 框架

#### 单线程、不开启事务

``` java
public void insertUser(){
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
        String simpleUUID = IdUtil.simpleUUID();
        User user = new User(simpleUUID,"ldd",20,"","");
        userMapper.insert(user);
    }
    long end = System.currentTimeMillis();
    System.out.println((end - start)/1000L);
}
```

> 测试结果：插入1W条数据耗时11s，平均插入速度约1000/s

#### 单线程、开启事务

``` java
public void insertUser(){
    long start = System.currentTimeMillis();
    DefaultTransactionDefinition df = new DefaultTransactionDefinition();
    df.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus transaction = dataSourceTransactionManager.getTransaction(df);
    for (int i = 0; i < 10000; i++) {
        String simpleUUID = IdUtil.simpleUUID();
        User user = new User(simpleUUID,"ldd",20,"","");
        userMapper.insert(user);
    }
    dataSourceTransactionManager.commit(transaction);
    long end = System.currentTimeMillis();
    System.out.println((end - start)/1000L);
}
```

> 测试结果：插入1W条数据耗时8s，平均插入速度约1200/s，稍微快一些

####  10线程、关闭事务

经多次测试10线程最优

``` java
public void threadInsertUser(){
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < 10000; i++) {
                        String simpleUUID = IdUtil.simpleUUID();
                        User user = new User(simpleUUID,"ldd",20,"","");
                        userMapper.insert(user);
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start)/1000L);
    }
```

> 测试结果：插入1W条数据耗时2.5s，平均插入速度约4000/s，快非常多

#### 10线程、每个线程单独开启事务

``` java
public void threadInsertUser(){
    long start = System.currentTimeMillis();
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                DefaultTransactionDefinition df = new DefaultTransactionDefinition();
                df.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus transaction = dataSourceTransactionManager.getTransaction(df);
                for (int i = 0; i < 1000; i++) {
                    String simpleUUID = IdUtil.simpleUUID();
                    User user = new User(simpleUUID,"ldd",20,"","");
                    userMapper.insert(user);
                }
                dataSourceTransactionManager.commit(transaction);
            }
        }));
    }
    for (Thread thread : threads) {
        thread.start();
    }
    for (Thread thread : threads) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    long end = System.currentTimeMillis();
    System.out.println((end - start)/100L);
}
```

> 测试结果：插入1W条数据耗时1s，平均插入速度约10000/s，快非常多

### JDBC

#### 单线程、不开启事务

```java
    public void jdbcInsert() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            jdbcTemplate.execute("INSERT INTO `USER` (`id`, `name`, `age`, `create_time`, `update_time`) VALUES" +
                    " ('0000507d42e042bca735943016fa2750', 'ldd', 20, '2023-02-14 19:41:18', '2023-02-14 19:41:18');");
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start)/1000L);
    }
```

> 测试结果：插入1W条数据耗时12秒，平均插入1000/s，和mybaits差不多一样

#### 10线程、不开启事务

```java
public void jdbcInsetThread(){
    long start = System.currentTimeMillis();
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    jdbcTemplate.execute("INSERT INTO `USER` (`id`, `name`, `age`, `create_time`, `update_time`) VALUES" +
                                         " ('0000507d42e042bca735943016fa2750', 'ldd', 20, '2023-02-14 19:41:18', '2023-02-14 19:41:18');");
                }
            }
        }));
    }
    for (Thread thread : threads) {
        thread.start();
    }
    for (Thread thread : threads) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    long end = System.currentTimeMillis();
    System.out.println((end - start)/1000L);
}
```

> 测试结果：插入10W条数据耗时26秒，平均插入4000/s，和mybaits差不多一样

#### 10线程、开启事务

```java
public void jdbcInsetThreadTrans(){
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    DefaultTransactionDefinition df = new DefaultTransactionDefinition();
                    df.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    TransactionStatus transaction = dataSourceTransactionManager.getTransaction(df);
                    for (int i = 0; i < 10000; i++) {
                        jdbcTemplate.execute("INSERT INTO `USER` (`id`, `name`, `age`, `create_time`, `update_time`) VALUES" +
                                " ('0000507d42e042bca735943016fa2750', 'ldd', 20, '2023-02-14 19:41:18', '2023-02-14 19:41:18');");
                    }
                    dataSourceTransactionManager.commit(transaction);
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start)/1000L);
    }
```

> 测试结果：插入10W条数据耗时9秒，平均插入10000/s，和mybaits差不多一样

#### 10线程、开启事务、开启预处理

``` java
public void jdbcBatchInsert(){
    long start = System.currentTimeMillis();
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                DefaultTransactionDefinition df = new DefaultTransactionDefinition();
                df.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus transaction = dataSourceTransactionManager.getTransaction(df);
                jdbcTemplate.batchUpdate("INSERT INTO `USER` (`id`, `name`, `age`, `create_time`, `update_time`) VALUES" +
                                         " (?, ?, ?, ?, ?);",new BatchPreparedStatementSetter() {
                                             @Override
                                             public void setValues(PreparedStatement ps, int i) throws SQLException {
                                                 ps.setString(1,"0000507d42e042bca735943016fa2750");
                                                 ps.setString(2, "ldd");
                                                 ps.setInt(3, 20);
                                                 ps.setString(4, "2023-02-14 19:41:18");
                                                 ps.setString(5, "2023-02-14 19:41:18");
                                             }
                                             @Override
                                             public int getBatchSize() {
                                                 return 10000;
                                             }
                                         });
                dataSourceTransactionManager.commit(transaction);
            }
        }));
    }
    for (Thread thread : threads) {
        thread.start();
    }
    for (Thread thread : threads) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    long end = System.currentTimeMillis();
    System.out.println((end - start)/100L);
}
```

> 测试结果：10W条数据，耗时5秒，平均20000/s，目前最快

### 最后还是发现从文件读取最快

```sql
SELECT *
INTO OUTFILE '/var/lib/mysql-files/user.csv'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\t'
LINES TERMINATED BY '\n'
FROM demos.user;

-- 查看保存文件地址
SHOW VARIABLES LIKE 'secure_file_priv';

DELETE FROM user
-- 解决报错
show variables like 'sql_mode';
set sql_mode='';

-- 加载文件
LOAD DATA INFILE '/var/lib/mysql-files/user.csv'
INTO TABLE demos.user
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
-- IGNORE 1 LINES -- 忽略第一行
(@dummy,name,age,create_time,update_time,role_id) -- 列出需要插入的列,不包括自增ID列
SET id = NULL; -- 将自增ID列设置为NULL,MySQL将自动生成自增值

-- 如果需要从客户端需要打开此模式，
-- 这是一次性打开的方式
set global local_infile=1

-- 然后执行LOAD语句

-- LOCAL从客户端加载文件
LOAD DATA LOCAL INFILE 'C:\\MyFile\\learn\\java\\code\\demo\\target\\users.csv'
INTO TABLE demos.user
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
-- IGNORE 1 LINES -- 忽略第一行
(@dummy,name,age,create_time,update_time,role_id) -- 列出需要插入的列,不包括自增ID列
SET id = NULL; -- 将自增ID列设置为NULL,MySQL将自动生成自增值


```

> Affected rows: 100000000
>时间: 533.738s
> 
> 187371/s

在下面查询语句中

```sql
select count(*) from user
```

5000W条数据耗时：1.42秒

4000W条数据耗时：1.22秒

3000W条数据耗时：1.22秒

2000W条数据耗时：0.6秒

1000W条数据耗时：0.2秒

100W条数据耗时：0.05秒

10W条数据耗时：0.02秒





在达梦数据库中和MySQL中执行下面语句耗时对比

数据1000W，都有id，create_time索引

达梦索引如下图

![image-20230603113505144](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/img/image-20230603113505144.png)

MySQL索引如下图

![image-20230603113527242](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/img/image-20230603113527242.png)

```sql
MySQL-耗时0.08s
SELECT * from user where name = 'ldd56' and age = 30 order by create_time desc limit 1
达梦-耗时0.002s
SELECT * from demos."USER" where name = 'ldd56' and age = 30 order by create_time desc limit 1
```



在4000W条数据时

```
MySQL-耗时0.22秒
SELECT * from user where name = 'ldd56' and age = 30 order by create_time desc limit 1
达梦-耗时0.26秒
SELECT * from demos."USER" where name = 'ldd56' and age = 30 order by create_time desc limit 1
Elasticsearch-耗时0.02秒
GET /user/_doc/0
```

