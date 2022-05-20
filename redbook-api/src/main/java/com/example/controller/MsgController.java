package com.example.controller;

import com.example.base.BaseInfoProperties;
import com.example.grace.result.GraceJSONResult;
import com.example.service.MsgService;
import com.example.thrd.mo.MessageMO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "Msg测试接口")
@RequestMapping("msg")
@RestController
public class MsgController extends BaseInfoProperties {

    @Autowired
    private MsgService msgService;

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String userId, @RequestParam Integer page,
                                @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        List<MessageMO> list = msgService.queryList(userId, page, pageSize);

        return GraceJSONResult.ok(list);
    }
}