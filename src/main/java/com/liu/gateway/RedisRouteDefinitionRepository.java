package com.liu.gateway;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RedisRouteDefinitionRepository
 * @Description 该类用来替代动态路由提供路由信息
 * @Author 刘培振
 * @Date 2020-07-08 9:33
 * @Version 1.0
 */
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    private Logger logger = LoggerFactory.getLogger(RedisRouteDefinitionRepository.class);

    public static final String GATEWAY_ROUTES = "geteway_routes";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 提供动态路由信息，路由信息都被放到redis中，无论是否重启，都会保存在redis中
     *
     * @return
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        stringRedisTemplate.opsForHash().values(GATEWAY_ROUTES).stream()
                .forEach(routeDefinition -> routeDefinitions.add(JSON.parseObject(routeDefinition.toString(), RouteDefinition.class)));
        return Flux.fromIterable(routeDefinitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            logger.info("save route {}", r.getId());
            stringRedisTemplate.opsForHash().put(GATEWAY_ROUTES, r.getId(), JSON.toJSONString(r));
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (stringRedisTemplate.opsForHash().hasKey(GATEWAY_ROUTES, id)) {
                logger.info("del routeId {}", id);
                stringRedisTemplate.opsForHash().delete(GATEWAY_ROUTES, id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(new NotFoundException("Unsupported operation")));
        });
    }
}
