package com.example.service;

import com.example.utils.PagedGridResult;

public interface FansService {

    /**
     * 关注
     */
    public void follow(String myId, String vlogerId);

    /**
     * 取消关注
     */
    public void cancel(String myId, String vlogerId);

    /**
     * 查询用户是否关注博主
     */
    public boolean queryDoIFollowVloger(String myId, String vlogerId);

    /**
     * 查询用户关注博主列表
     */
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize);

    /**
     * 查询用户粉丝列表
     */
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize);
}
