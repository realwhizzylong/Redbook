package com.example.controller;

import com.example.base.BaseInfoProperties;
import com.example.grace.result.GraceJSONResult;
import com.example.grace.result.ResponseStatusEnum;
import com.example.service.UserService;
import com.example.thrd.bo.RegisterLoginBO;
import com.example.thrd.pojo.Users;
import com.example.thrd.vo.UsersVO;
import com.example.utils.IPUtil;
import com.example.utils.SMSUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@Api(tags = "Passport测试接口")
@RequestMapping("passport")
@RestController
public class PassportController extends BaseInfoProperties {

    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {

        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.ok();
        }

        //获得用户IP
        String userIp = IPUtil.getRequestIp(request);
        //根据用户IP进行限制，限制用户在60s内只能获得一次验证码
        redis.setnx60s(MOBILE_SMS_CODE + ":" + userIp, userIp);

        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        smsUtils.sendSMS(mobile, code);

        log.info(code);

        //把验证码放入redis中，用于后续验证
        redis.set(MOBILE_SMS_CODE + ":" + mobile, code, 30 * 60);

        return GraceJSONResult.ok();
    }

    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegisterLoginBO registerLoginBO,
                                 HttpServletRequest request) throws Exception {

        String mobile = registerLoginBO.getMobile();
        String code = registerLoginBO.getSmsCode();

        //从redis中获得验证码校验是否匹配
        String redisCode = redis.get(MOBILE_SMS_CODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        //查询数据库，判断用户是否存在
        Users user = userService.queryMobileExist(mobile);
        if (user == null) {
            //如果用户为空，注册用户信息入库
            user = userService.createUser(mobile);
        }

        //如果用户不为空，保存用户会话信息
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);

        //用户注册登陆成功后，删除redis中的短信验证码
        redis.del(MOBILE_SMS_CODE + ":" + mobile);

        //返回用户信息,包含token令牌
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request) throws Exception {

        redis.del(REDIS_USER_TOKEN + ":" + userId);

        return GraceJSONResult.ok();
    }

}