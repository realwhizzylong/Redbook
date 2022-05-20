package com.example.controller;

import com.example.enums.MessageEnum;
import com.example.service.CommentService;
import com.example.base.BaseInfoProperties;
import com.example.grace.result.GraceJSONResult;
import com.example.service.MsgService;
import com.example.service.VlogService;
import com.example.thrd.bo.CommentBO;
import com.example.thrd.pojo.Comment;
import com.example.thrd.pojo.Vlog;
import com.example.thrd.vo.CommentVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api(tags = "Comment测试接口")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;

    @Autowired
    private VlogService vlogService;

    @Autowired
    private MsgService msgService;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception {

        CommentVO commentVO = commentService.createComment(commentBO);

        return GraceJSONResult.ok(commentVO);
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) throws Exception {

        String counts = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);

        if (StringUtils.isBlank(counts)) {
            return GraceJSONResult.ok(0);
        }

        return GraceJSONResult.ok(Integer.parseInt(counts));
    }

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId, @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page, @RequestParam Integer pageSize) throws Exception {


        return GraceJSONResult.ok(commentService.queryVlogComments(vlogId, userId, page, pageSize));
    }

    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId, @RequestParam String commentId,
                                  @RequestParam String vlogId) throws Exception {

        commentService.deleteComment(commentUserId, commentId, vlogId);

        return GraceJSONResult.ok();
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId, @RequestParam String userId) throws Exception {

        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");

        Comment comment = commentService.getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());

        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);

        //系统消息：点赞评论
        msgService.createMsg(userId, comment.getCommentUserId(), MessageEnum.LIKE_COMMENT.type, msgContent);

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String commentId, @RequestParam String userId) throws Exception {

        redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);

        return GraceJSONResult.ok();
    }
}
