package com.example.controller;

import com.example.base.BaseInfoProperties;
import com.example.enums.YesOrNo;
import com.example.grace.result.GraceJSONResult;
import com.example.grace.result.ResponseStatusEnum;
import com.example.service.VlogService;
import com.example.thrd.bo.VlogBO;
import com.example.thrd.vo.IndexVlogVO;
import com.example.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "Vlog 短视频相关业务功能测试接口")
@RequestMapping("vlog")
@RestController
@RefreshScope
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @Value("${nacos.counts}")
    private Integer nacosCounts;

    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlogBO) {

        if (vlogBO == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.VLOG_PUBLISH_ERROR);
        }

        vlogService.createVlog(vlogBO);

        return GraceJSONResult.ok();
    }

    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String search,
                                     @RequestParam String userId,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getIndexVlogList(search, userId, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam String userId, @RequestParam String vlogId) {

        IndexVlogVO vlogVO = vlogService.getVlogDetailById(vlogId, userId);

        return GraceJSONResult.ok(vlogVO);
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId, @RequestParam String vlogId) {

        vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.YES.type);

        return GraceJSONResult.ok();
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId, @RequestParam String vlogId) {

        vlogService.changeToPrivateOrPublic(userId, vlogId, YesOrNo.NO.type);

        return GraceJSONResult.ok();
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId, @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId, @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId, @RequestParam String vlogId,
                                @RequestParam String vlogerId) {

        vlogService.likeVlog(userId, vlogId);

        //点赞后，视频和视频发布者的获赞数都会+1
        redis.increment(REDIS_VLOG_LIKED_COUNTS + ":" + vlogId, 1);
        redis.increment(REDIS_VLOGER_LIKED_COUNTS + ":" + vlogerId, 1);

        //我点赞的视频，需要在redis中保存关联关系
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");

        String countsStr = redis.get(REDIS_VLOG_LIKED_COUNTS + ":" + vlogerId);
        int counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.parseInt(countsStr);
            if (counts > nacosCounts) {
                vlogService.flushCounts(vlogId, counts);
            }
        }

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId, @RequestParam String vlogId,
                                  @RequestParam String vlogerId) {

        vlogService.unlikeVlog(userId, vlogId);

        //取消点赞后，视频和视频发布者的获赞数都会-1
        redis.decrement(REDIS_VLOG_LIKED_COUNTS + ":" + vlogId, 1);
        redis.decrement(REDIS_VLOGER_LIKED_COUNTS + ":" + vlogerId, 1);

        //我取消点赞的视频，需要在redis中删除关联关系
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();
    }

    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {

        return GraceJSONResult.ok(vlogService.getVlogLikedCounts(vlogId));
    }

    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId, @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getMyLikedVlogList(userId, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId, @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getMyFollowVlogList(myId, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId, @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            page = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getMyFriendVlogList(myId, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

}
