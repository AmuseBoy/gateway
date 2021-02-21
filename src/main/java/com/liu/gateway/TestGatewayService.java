package com.liu.gateway;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;

/**
 * @ClassName TestGatewayService
 * @Description TODO
 * @Author 刘培振
 * @Date 2020-07-07 16:57
 * @Version 1.0
 */
@Service
public class TestGatewayService implements ApplicationEventPublisherAware {

    public static final String GATEWAY_ROUTES = "geteway_routes";

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * 通知刷新路由
     */
    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }


    /**
     * 增加路由
     *
     * @param definition
     * @return
     */
    public String add(RouteDefinition definition) {
        //调用RouteDefinitionWriter的实现类RedisRouteDefinitionRepository方法
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.notifyChanged();
        return "success";
    }

    /**
     * 路由更新
     *
     * @param definition
     * @return
     */
    public String update(RouteDefinition definition) {
        //调用RouteDefinitionWriter的实现类RedisRouteDefinitionRepository方法
        this.routeDefinitionWriter.delete(Mono.just((definition.getId()))).subscribe();
        this.routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        notifyChanged();
        return "success";
    }

    /**
     * 路由删除
     *
     * @param id
     * @return
     */
    public String delete(String id) {
        //调用RouteDefinitionWriter的实现类RedisRouteDefinitionRepository方法
        this.routeDefinitionWriter.delete(Mono.just((id))).subscribe();
        notifyChanged();
        return "success";
    }


    /**
     * 初始化
     */
    @PostConstruct
    public void main() {
        //路由定义类
        RouteDefinition definition = new RouteDefinition();
        //断言
        List<PredicateDefinition> predicates = new ArrayList<>();
        //过滤器
        List<FilterDefinition> filters = new ArrayList<>();
        definition.setId("route_one");
        //URI uri = UriComponentsBuilder.fromHttpUrl("http://192.168.143.222:8089").build().toUri();
        URI uri = URI.create("lb://DEMO");
        definition.setUri(uri);
        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                PredicateDefinition predicateDefinition = new PredicateDefinition();
                predicateDefinition.setName("Path");
                Map<String, String> predicateParams = new HashMap<>();
                predicateParams.put("pattern", "/test/**");
                predicateDefinition.setArgs(predicateParams);
                predicates.add(predicateDefinition);
            } else {
                PredicateDefinition predicateDefinition = new PredicateDefinition();
                predicateDefinition.setName("Query");
                Map<String, String> predicateParams = new HashMap<>();
                predicateParams.put("_genkey_0", "id");//_genkey_0，1，2
                predicateDefinition.setArgs(predicateParams);
                predicates.add(predicateDefinition);
            }
        }
        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                FilterDefinition filterDefinition = new FilterDefinition();
                filterDefinition.setName("AddRequestHeader");
                Map<String, String> filterParams = new HashMap<>();
                filterParams.put("_genkey_0", "secret");
                filterParams.put("_genkey_1", "123");
                filterDefinition.setArgs(filterParams);
                filters.add(filterDefinition);
            } else {
                FilterDefinition filterDefinition = new FilterDefinition();
                filterDefinition.setName("AddRequestParameter");
                Map<String, String> filterParams = new HashMap<>();
                filterParams.put("_genkey_0", "foo");
                filterParams.put("_genkey_1", "bar");
                filterDefinition.setArgs(filterParams);
                filters.add(filterDefinition);
            }

        }
        definition.setPredicates(predicates);
        definition.setFilters(filters);
        stringRedisTemplate.opsForHash().put(GATEWAY_ROUTES, definition.getId(), JSON.toJSONString(definition));
    }


}
