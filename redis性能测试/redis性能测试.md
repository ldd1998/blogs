# redis性能测试
## 前言
听说redis的单机QPS可以达到10W/s，于是我便测试一下
## JMeter安装插件管理器
1. 首先需要进到这个地址https://jmeter-plugins.org/install/Install/下载插件管理器

2. 根据要求放到`lib/ext` 下面

![image-20231007212527770](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/image-20231007212527770.png?imageSlim)

3. 重启JMeter后就可以在选项菜单里找到插件管理器了

![image-20231007212730889](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/image-20231007212730889.png?imageSlim)

## 安装Redis 插件

![image-20231007212922698](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/image-20231007212922698.png?imageSlim)

等待安装完成后重启

## 添加线程组

![image-20231007213015317](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/image-20231007213015317.png?imageSlim)

## 添加配置元件

![image-20231007213301122](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/image-20231007213301122.png?imageSlim)
这里报错信息如下，无法进行下去。

``` java
2023-10-07 23:38:01,335 INFO o.a.j.e.StandardJMeterEngine: Running the test!
2023-10-07 23:38:01,335 INFO o.a.j.s.SampleEvent: List of sample_variables: []
2023-10-07 23:38:01,335 INFO o.a.j.g.u.JMeterMenuBar: setRunning(true, *local*)
2023-10-07 23:38:01,443 INFO o.a.j.e.StandardJMeterEngine: Starting ThreadGroup: 1 : 线程组
2023-10-07 23:38:01,443 INFO o.a.j.e.StandardJMeterEngine: Starting 1 threads for group 线程组.
2023-10-07 23:38:01,443 INFO o.a.j.e.StandardJMeterEngine: Thread will continue on error
2023-10-07 23:38:01,443 INFO o.a.j.t.ThreadGroup: Starting thread group... number=1 threads=1 ramp-up=1 delayedStart=false
2023-10-07 23:38:01,446 INFO o.a.j.t.ThreadGroup: Started thread group number 1
2023-10-07 23:38:01,446 INFO o.a.j.e.StandardJMeterEngine: All thread groups have been started
2023-10-07 23:38:01,446 INFO o.a.j.t.JMeterThread: Thread started: 线程组 1-1
2023-10-07 23:38:01,448 ERROR k.a.j.c.r.RedisDataSet: Failed to retrieve data from redis key test:1011109
2023-10-07 23:38:01,448 INFO o.a.j.t.JMeterThread: Stop Thread seen for thread 线程组 1-1, reason: org.apache.jorphan.util.JMeterStopThreadException: End of redis data detected
2023-10-07 23:38:01,448 INFO o.a.j.t.JMeterThread: Thread finished: 线程组 1-1
2023-10-07 23:38:01,448 INFO o.a.j.e.StandardJMeterEngine: Notifying test listeners of end of test
2023-10-07 23:38:01,449 WARN r.c.j.JedisFactory: Error while close
redis.clients.jedis.exceptions.JedisException: Could not return the broken resource to the pool
	at redis.clients.jedis.util.Pool.returnBrokenResourceObject(Pool.java:116) ~[jedis-3.6.3.jar:?]
	at redis.clients.jedis.util.Pool.returnBrokenResource(Pool.java:98) ~[jedis-3.6.3.jar:?]
	at redis.clients.jedis.JedisPool.returnResource(JedisPool.java:382) ~[jedis-3.6.3.jar:?]
	at redis.clients.jedis.JedisPool.returnResource(JedisPool.java:15) ~[jedis-3.6.3.jar:?]
	at redis.clients.jedis.Jedis.close(Jedis.java:3957) ~[jedis-3.6.3.jar:?]
	at redis.clients.jedis.JedisFactory.destroyObject(JedisFactory.java:166) [jedis-3.6.3.jar:?]
	at org.apache.commons.pool2.PooledObjectFactory.destroyObject(PooledObjectFactory.java:126) [commons-pool2-2.9.0.jar:2.9.0]
	at org.apache.commons.pool2.impl.GenericObjectPool.destroy(GenericObjectPool.java:958) [commons-pool2-2.9.0.jar:2.9.0]
	at org.apache.commons.pool2.impl.GenericObjectPool.clear(GenericObjectPool.java:677) [commons-pool2-2.9.0.jar:2.9.0]
	at org.apache.commons.pool2.impl.GenericObjectPool.close(GenericObjectPool.java:721) [commons-pool2-2.9.0.jar:2.9.0]
	at redis.clients.jedis.util.Pool.closeInternalPool(Pool.java:122) [jedis-3.6.3.jar:?]
	at redis.clients.jedis.util.Pool.destroy(Pool.java:109) [jedis-3.6.3.jar:?]
	at kg.apc.jmeter.config.redis.RedisDataSet.testEnded(RedisDataSet.java:258) [jmeter-plugins-redis-0.6.jar:?]
	at kg.apc.jmeter.config.redis.RedisDataSet.testEnded(RedisDataSet.java:253) [jmeter-plugins-redis-0.6.jar:?]
	at org.apache.jmeter.engine.StandardJMeterEngine.notifyTestListenersOfEnd(StandardJMeterEngine.java:218) [ApacheJMeter_core.jar:5.4.3]
	at org.apache.jmeter.engine.StandardJMeterEngine.run(StandardJMeterEngine.java:493) [ApacheJMeter_core.jar:5.4.3]
	at java.lang.Thread.run(Thread.java:750) [?:1.8.0_382]
Caused by: java.lang.IllegalStateException: Invalidated object not currently part of this pool
	at org.apache.commons.pool2.impl.GenericObjectPool.invalidateObject(GenericObjectPool.java:642) ~[commons-pool2-2.9.0.jar:2.9.0]
	at org.apache.commons.pool2.impl.GenericObjectPool.invalidateObject(GenericObjectPool.java:620) ~[commons-pool2-2.9.0.jar:2.9.0]
	at redis.clients.jedis.util.Pool.returnBrokenResourceObject(Pool.java:114) ~[jedis-3.6.3.jar:?]
	... 16 more
2023-10-07 23:38:01,449 INFO o.a.j.g.u.JMeterMenuBar: setRunning(false, *local*)

```

