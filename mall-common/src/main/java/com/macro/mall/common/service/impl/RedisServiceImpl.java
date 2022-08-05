package com.macro.mall.common.service.impl;

import cn.hutool.core.date.DateUtil;
import com.macro.mall.common.domain.RedisZSetVo;
import com.macro.mall.common.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis操作实现类
 * Created by macro on 2020/3/3.
 */
public class RedisServiceImpl implements RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public boolean setIfAbs(String key, Object value,Long second) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value,second,TimeUnit.SECONDS));
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Long del(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    @Override
    public Boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    @Override
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    @Override
    public Boolean hSet(String key, String hashKey, Object value, long time) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        return expire(key, time);
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public Boolean hSetAll(String key, Map<String, Object> map, long time) {
        redisTemplate.opsForHash().putAll(key, map);
        return expire(key, time);
    }

    @Override
    public void hSetAll(String key, Map<String, ?> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void hDel(String key, Object... hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long hIncr(String key, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public Long hDecr(String key, String hashKey, Long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, -delta);
    }

    @Override
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Long sAdd(String key, long time, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        expire(key, time);
        return count;
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    @Override
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Long lPush(String key, Object value, long time) {
        Long index = redisTemplate.opsForList().rightPush(key, value);
        expire(key, time);
        return index;
    }

    @Override
    public Long lPushAll(String key, Object... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public Long lPushAll(String key, Long time, Object... values) {
        Long count = redisTemplate.opsForList().rightPushAll(key, values);
        expire(key, time);
        return count;
    }

    @Override
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    @Override
    public Boolean bitSet(String key, long index, boolean bit) {
        return redisTemplate.opsForValue().setBit(key,index,bit);
    }

    @Override
    public Long bitCount(String key) {
        return redisTemplate.execute((RedisCallback<Long>) con->con.bitCount(key.getBytes()));
    }

    @Override
    public Boolean bitGet(String key, long index) {
        return redisTemplate.opsForValue().getBit(key,index);
    }

    @Override
    public String getBitStr(String key, Integer index) {
        BitFieldSubCommands command = BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(index+1)).valueAt(0);
        // 获取用户从当前日期开始到 1 号的所有签到状态
        List<Long> data = redisTemplate.opsForValue().bitField(key, command);
        Long num = data.get(0);
        if (num == null || num == 0){
            return null;
        }
        return Long.toBinaryString(num);
    }

    @Override
    public Double zSet(String key, Object filed, Double score) {
        return redisTemplate.opsForZSet().incrementScore(key,filed,score);
    }


    @Override
    public List<RedisZSetVo> zGetTop(String key, Long top) {
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, top);
        return typedTuples.stream().map(item -> {
            RedisZSetVo redisZSetVo = new RedisZSetVo();
            redisZSetVo.setKey(item.getValue());
            redisZSetVo.setValue(item.getScore());
            return redisZSetVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RedisZSetVo> zGetAllTop(List<String> keys, Long top) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        String originKey = keys.get(0);
        keys.remove(0);
        String destKey = "mall:portal:rank:week:%s";
        String finalKey = String.format(destKey, DateUtil.format(new Date(), "yyyyMM"));
        redisTemplate.opsForZSet().unionAndStore(originKey, keys,finalKey);
        return zGetTop(finalKey, 30L);
    }

    @Override
    public void setWithMill(String key, String toJSONString, long expire) {
        redisTemplate.opsForValue().set(key,toJSONString,expire,TimeUnit.MILLISECONDS);
    }

    @Override
    public Object execLua(RedisScript script, List keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }


}
