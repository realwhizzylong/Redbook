package com.example.controller;

import com.example.base.RabbitMQConfig;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import com.example.grace.result.GraceJSONResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "Hello测试接口")
@RestController
@RefreshScope
public class HelloController {

    @Value("${nacos.counts}")
    private Integer nacosCounts;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @GetMapping("hello")
    public Object hello() {
        return GraceJSONResult.ok("Hello SpringBoot");
    }

    @GetMapping("produce")
    public Object produce() {

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg.send", "我发了一个消息～");

        return GraceJSONResult.ok();
    }

    @GetMapping("nacosCounts")
    public Object nacosCounts() {
        return GraceJSONResult.ok("nacosCounts的数值为：" + nacosCounts);
    }

}