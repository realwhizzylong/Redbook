package com.example.service.impl;

import com.example.base.BaseInfoProperties;
import com.example.base.RabbitMQConfig;
import com.example.enums.MessageEnum;
import com.example.enums.YesOrNo;
import com.example.service.FansService;
import com.example.service.MsgService;
import com.example.thrd.mapper.FansMapper;
import com.example.thrd.mapper.FansMapperCustom;
import com.example.thrd.mo.MessageMO;
import com.example.thrd.pojo.Fans;
import com.example.thrd.vo.FanVO;
import com.example.thrd.vo.VlogerVO;
import com.example.utils.JsonUtils;
import com.example.utils.PagedGridResult;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;

    @Autowired
    private MsgService msgService;

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void follow(String myId, String vlogerId) {

        String fid = sid.nextShort();

        Fans fan = new Fans();
        fan.setId(fid);
        fan.setFanId(myId);
        fan.setVlogerId(vlogerId);

        Fans vloger = queryFansRelationship(vlogerId, myId);
        if (vloger != null) {
            fan.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        } else {
            fan.setIsFanFriendOfMine(YesOrNo.NO.type);
        }

        fansMapper.insert(fan);

        //系统消息：关注
        //msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);

        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);

        //优化：使用rabbitmq异步解耦
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.FOLLOW_YOU.enValue,
                JsonUtils.objectToJson(messageMO));

    }

    @Transactional
    @Override
    public void cancel(String myId, String vlogerId) {

        Fans fan = queryFansRelationship(myId, vlogerId);
        if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }

        fansMapper.delete(fan);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {

        Fans vloger = queryFansRelationship(myId, vlogerId);

        return vloger != null;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<FanVO> list = fansMapperCustom.queryMyFans(map);

        for (FanVO f : list) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }

        return setterPagedGrid(list, page);
    }

    public Fans queryFansRelationship(String fanId, String vlogerId) {

        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId);
        criteria.andEqualTo("fanId", fanId);

        List<Fans> list = fansMapper.selectByExample(example);

        Fans fan = null;
        if (list != null && list.size() > 0) {
            fan = list.get(0);
        }

        return fan;
    }
}
