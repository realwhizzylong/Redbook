package com.example.service.impl;

import com.example.enums.MessageEnum;
import com.example.service.CommentService;
import com.example.base.BaseInfoProperties;
import com.example.enums.YesOrNo;
import com.example.service.MsgService;
import com.example.service.VlogService;
import com.example.thrd.bo.CommentBO;
import com.example.thrd.mapper.CommentMapper;
import com.example.thrd.mapper.CommentMapperCustom;
import com.example.thrd.pojo.Comment;
import com.example.thrd.pojo.Vlog;
import com.example.thrd.vo.CommentVO;
import com.example.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private VlogService vlogService;

    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public CommentVO createComment(CommentBO commentBO) {

        String cid = sid.nextShort();

        Comment comment = new Comment();
        comment.setId(cid);
        comment.setVlogerId(commentBO.getVlogerId());
        comment.setVlogId(commentBO.getVlogId());
        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());
        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        //redis操作评论数+1
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);


        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());

        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", commentBO.getVlogId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", cid);
        msgContent.put("commentContent", commentBO.getContent());

        Integer type = MessageEnum.COMMENT_VLOG.type;

        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) &&
                !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
            type = MessageEnum.REPLY_YOU.type;
        }

        //系统消息：评论/回复
        msgService.createMsg(commentBO.getCommentUserId(), commentBO.getVlogerId(), type, msgContent);

        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        PageHelper.startPage(page, pageSize);

        List<CommentVO> list = commentMapperCustom.getCommentList(map);

        for (CommentVO cv : list) {
            String commentId = cv.getCommentId();
            //当前视频的某个评论的点赞总数
            String counts = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            if (StringUtils.isBlank(counts)) {
                cv.setLikeCounts(0);
            } else {
                cv.setLikeCounts(Integer.parseInt(counts));
            }
            //判断当前用户是否点赞过该评论
            String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {

        Comment pendingComment = new Comment();

        pendingComment.setId(commentId);
        pendingComment.setCommentUserId(commentUserId);

        commentMapper.delete(pendingComment);

        //redis操作评论数-1
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }
}
