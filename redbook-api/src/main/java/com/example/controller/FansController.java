package com.example.controller;

import com.example.base.BaseInfoProperties;
import com.example.grace.result.GraceJSONResult;
import com.example.grace.result.ResponseStatusEnum;
import com.example.service.FansService;
import com.example.service.UserService;
import com.example.thrd.pojo.Users;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "Fans 粉丝相关业务测试接口")
@RequestMapping("fans")
@RestController
public class FansController extends BaseInfoProperties {

    @Autowired
    private FansService fansService;

    @Autowired
    private UserService userService;

    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId, @RequestParam String vlogerId) {

        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        Users vloger = userService.getUser(vlogerId);
        Users me = userService.getUser(myId);

        if (vloger == null || me == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        fansService.follow(myId, vlogerId);

        //博主的粉丝+1，我的关注+1
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        redis.set(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + "vlogerId", "1");

        return GraceJSONResult.ok();
    }

    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId, @RequestParam String vlogerId) {

        fansService.cancel(myId, vlogerId);

        //博主的粉丝+1，我的关注+1
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        redis.del(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + "vlogerId");

        return GraceJSONResult.ok();
    }

    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId, @RequestParam String vlogerId) {

        return GraceJSONResult.ok(fansService.queryDoIFollowVloger(myId, vlogerId));
    }

    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId, @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {


        return GraceJSONResult.ok(fansService.queryMyFollows(myId, page, pageSize));
    }

    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId, @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {


        return GraceJSONResult.ok(fansService.queryMyFans(myId, page, pageSize));
    }
}
