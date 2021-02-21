package com.liu.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName TestGatewayController
 * @Description TODO
 * @Author 刘培振
 * @Date 2020-07-07 17:51
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "/test")
public class TestGatewayController {

    @Autowired
    private TestGatewayService testGatewayService;

    @PostMapping(value = "/add")
    public String save(@RequestBody RouteDefinition definition) {
        return testGatewayService.add(definition);
    }

    @PostMapping(value = "/update")
    public String update(@RequestBody RouteDefinition definition) {
        return testGatewayService.update(definition);
    }

    @GetMapping(value = "/del")
    public String del(@RequestParam String routeId) {
        return testGatewayService.delete(routeId);
    }

}
