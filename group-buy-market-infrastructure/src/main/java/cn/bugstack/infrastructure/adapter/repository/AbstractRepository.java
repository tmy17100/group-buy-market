package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.infrastructure.dcc.DCCService;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import javax.annotation.Resource;
import java.util.function.Supplier;

/**
 * @author TanMengyang
 * @description
 * @create 2025-12-01 10:14
 */
public  abstract class AbstractRepository {
    @Resource
    protected IRedisService redisService;
    @Resource
    protected DCCService dccService;
    private Logger logger= LoggerFactory.getLogger(AbstractRepository.class);

    protected <T>T getFromCacheOrDb(String cacheKey, Supplier<T> dbFallback){
        if(dccService.isCacheOpenSwitch()){
            T cacheResult = redisService.getValue(cacheKey);
            if(cacheResult!=null){
                return cacheResult;
            }
            T dbResult = dbFallback.get();
            if(dbResult==null){
                return null;
            }
            redisService.setValue(cacheKey,dbResult);
            return dbResult;
        }
        else{
            return dbFallback.get();
        }
    }
}

