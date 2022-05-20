package com.example.service.impl;

import com.example.base.BaseInfoProperties;
import com.example.enums.MessageEnum;
import com.example.enums.YesOrNo;
import com.example.service.FansService;
import com.example.service.MsgService;
import com.example.service.VlogService;
import com.example.thrd.bo.VlogBO;
import com.example.thrd.mapper.MyLikedVlogMapper;
import com.example.thrd.mapper.VlogMapper;
import com.example.thrd.mapper.VlogMapperCustom;
import com.example.thrd.pojo.MyLikedVlog;
import com.example.thrd.pojo.Vlog;
import com.example.thrd.vo.IndexVlogVO;
import com.example.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private FansService fansService;

    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {

        String vid = sid.nextShort();

        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);
        vlog.setId(vid);
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogList(String search, String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }

        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);

        for (IndexVlogVO v : list) {
            setVO(v, userId);
        }

        return setterPagedGrid(list, page);
    }

    private IndexVlogVO setVO(IndexVlogVO v, String userId) {

        String vlogerId = v.getVlogerId();
        String vlogId = v.getVlogId();
        if (StringUtils.isNotBlank(userId)) {
            //判断用户是否关注该博主
            if (fansService.queryDoIFollowVloger(userId, vlogerId)) {
                v.setDoIFollowVloger(true);
            }
            //判断用户是否点赞过视频
            v.setDoILikeThisVlog(doILike(userId, vlogId));
        }
        //判断当前视频点赞数量
        v.setLikeCounts(getVlogLikedCounts(vlogId));

        return v;
    }

    private boolean doILike(String myId, String vlogId) {

        String doIlike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);

        return StringUtils.isNotBlank(doIlike) && doIlike.equalsIgnoreCase("1");
    }

    @Override
    public IndexVlogVO getVlogDetailById(String vlogId, String userId) {

        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(vlogId)) {
            map.put("vlogId", vlogId);
        }

        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);

        if (list != null && list.size() > 0) {
            IndexVlogVO vlogVO = list.get(0);
            setVO(vlogVO, userId);
            return vlogVO;
        }

        return null;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("id", vlogId);

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);

        vlogMapper.updateByExampleSelective(pendingVlog, example);
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);

        PageHelper.startPage(page, pageSize);

        List<Vlog> list = vlogMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void likeVlog(String userId, String vlogId) {

        String id = sid.nextShort();

        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(id);
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);

        myLikedVlogMapper.insert(myLikedVlog);

        Vlog vlog = this.getVlog(vlogId);

        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());

        //系统消息：点赞短视频
        msgService.createMsg(userId, vlog.getVlogerId(), MessageEnum.LIKE_VLOG.type, msgContent);
    }

    @Override
    public Vlog getVlog(String id) {
        return vlogMapper.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void unlikeVlog(String userId, String vlogId) {

        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);

        myLikedVlogMapper.delete(myLikedVlog);
    }

    @Override
    public int getVlogLikedCounts(String vlogId) {

        String counts = redis.get(REDIS_VLOG_LIKED_COUNTS + ":" + vlogId);

        if (StringUtils.isBlank(counts)) {
            return 0;
        }

        return Integer.parseInt(counts);
    }

    @Override
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(myId)) {
                //用户必定关注该博主
                v.setDoIFollowVloger(true);
                //判断用户是否点赞过视频
                v.setDoILikeThisVlog(doILike(myId, vlogId));
            }
            //判断当前视频点赞数量
            v.setLikeCounts(getVlogLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(myId)) {
                //用户必定关注该博主
                v.setDoIFollowVloger(true);
                //判断用户是否点赞过视频
                v.setDoILikeThisVlog(doILike(myId, vlogId));
            }
            //判断当前视频点赞数量
            v.setLikeCounts(getVlogLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void flushCounts(String vlogId, int counts) {

        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);

        vlogMapper.updateByPrimaryKeySelective(vlog);
    }
}
