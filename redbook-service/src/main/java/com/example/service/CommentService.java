package com.example.service;

import com.example.thrd.bo.CommentBO;
import com.example.thrd.pojo.Comment;
import com.example.thrd.vo.CommentVO;
import com.example.utils.PagedGridResult;

public interface CommentService {

    /**
     * 发表评论
     */
    public CommentVO createComment(CommentBO commentBO);

    /**
     * 查询评论列表
     */
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize);

    /**
     * 删除评论
     */
    public void deleteComment(String commentUserId, String commentId, String vlogId);

    /**
     * 根据主键查询评论
     */
    public Comment getComment(String id);

}
