package com.example.service;

import com.example.thrd.bo.VlogBO;
import com.example.thrd.pojo.Vlog;
import com.example.thrd.vo.IndexVlogVO;
import com.example.utils.PagedGridResult;

public interface VlogService {

    /**
     * 新增Vlog视频
     */
    public void createVlog(VlogBO vlogBO);

    /**
     * 查询首页/搜索的Vlog列表
     */
    public PagedGridResult getIndexVlogList(String search, String userId, Integer page, Integer pageSize);

    /**
     * 根据视频主键查询Vlog
     */
    public IndexVlogVO getVlogDetailById(String vlogId, String userId);

    /**
     * 将视频改为私密/公开
     */
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo);

    /**
     * 查询私密/公开视频列表
     */
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo);

    /**
     * 用户点赞视频
     */
    public void likeVlog(String userId, String vlogId);

    /**
     * 用户取消点赞视频
     */
    public void unlikeVlog(String userId, String vlogId);

    /**
     * 视频点赞数量
     */
    public int getVlogLikedCounts(String vlogId);

    /**
     * 查询用户点赞过的视频列表
     */
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize);

    /**
     * 查询用户关注的博主发布的视频列表
     */
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize);

    /**
     * 查询用户的朋友发布的视频列表
     */
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize);

    /**
     * 根据主键查询vlog
     */
    public Vlog getVlog(String id);

    /**
     * 把counts入数据库
     */
    public void flushCounts(String vlogId, int counts);

}
