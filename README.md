# mall-swarm
## 项目二次开发内容

1. 通过Redis的bitmap进行用户每日签到，连续签到统计.
2. 通过ThreadLocal加Aspect切面，对一次请求的用户信息进行存储本地的操作,避免每次都需要注入用户service，还需要传入id，才能查询用户信息，使得一次请求中所有使用用户信息的地方都直接从内存获取，不需要查数据库或者Redis，也减少了用户信息在方法之间的传递
3. 购物车修改为纯Redis实现，购物车是一个重修改的业务，使用数据库实现，延迟会稍大，并且购物车信息可以在生成订单后永久取消，大部分场景并不需要长时间持久化存储，Redis的数据结构选择hash，因为购物车中每个item都要支持独立修改。
4. 订单生成增加接口等幂性保证，生成订单连续点击两次，需要保证只有一次成功，但是就是要生成两个订单，也不能被认为是一个订单，使用生成订单的页面产生一个唯一Token，传递到后端，后段将其存入Redis，过期时间设置为订单自动取消的时间，只有来自同一个订单页面的点击，就会认为是重复点击。
5. 使用Redis实现用户消费排行榜，支持日榜，月榜，可拓展 
6. 使用Redis配合切面，拦截器实现注解对方法限流
7. 实现秒杀业务，考虑接口限流，接口隐藏，分布式超卖，商品预热，下单异步，接口等幂
8. 实现基于lua脚本的令牌桶限流算法，支持接口模式与ip模式
 
## 一些流程图
<img width="280" alt="截屏2022-08-20 20 27 15" src="https://user-images.githubusercontent.com/83215491/185746266-f031aab4-3800-49b1-8e1a-4ba4b1e4c77b.png">
<img width="448" alt="截屏2022-08-20 23 34 37" src="https://user-images.githubusercontent.com/83215491/185754858-d7ff7484-853c-4af7-bb9a-fccf1cb70487.png">

<p>
  <a href="#公众号"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E5%85%AC%E4%BC%97%E5%8F%B7-macrozheng-blue.svg" alt="公众号"></a>
  <a href="#公众号"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E4%BA%A4%E6%B5%81-%E5%BE%AE%E4%BF%A1%E7%BE%A4-2BA245.svg" alt="交流"></a>
  <a href="https://github.com/macrozheng/mall-learning"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E5%AD%A6%E4%B9%A0%E6%95%99%E7%A8%8B-mall--learning-green.svg" alt="学习教程"></a>
  <a href="https://github.com/macrozheng/mall"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E5%90%8E%E5%8F%B0%E9%A1%B9%E7%9B%AE-mall-blue.svg" alt="后台项目"></a>
  <a href="https://github.com/macrozheng/mall-admin-web"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E5%89%8D%E7%AB%AF%E9%A1%B9%E7%9B%AE-mall--admin--web-green.svg" alt="前端项目"></a>
  <a href="https://gitee.com/macrozheng/mall-swarm"><img src="http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/badge/%E7%A0%81%E4%BA%91-%E9%A1%B9%E7%9B%AE%E5%9C%B0%E5%9D%80-orange.svg" alt="码云"></a>
</p>
