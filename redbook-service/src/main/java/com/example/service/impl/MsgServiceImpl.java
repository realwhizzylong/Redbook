package com.example.service.impl;

import com.example.base.BaseInfoProperties;
import com.example.enums.MessageEnum;
import com.example.service.MsgService;
import com.example.service.UserService;
import com.example.thrd.mo.MessageMO;
import com.example.thrd.pojo.Users;
import com.example.thrd.repository.MessageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Transactional
    @Override
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map<String, Object> msgContent) {

        Users fromUser = userService.getUser(fromUserId);

        MessageMO messageMO = new MessageMO();

        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());
        messageMO.setToUserId(toUserId);
        messageMO.setMsgType(msgType);
        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTime");

        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageable);

        for (MessageMO msg : list) {
            //如果类型数关注消息，则需要查询我有没有关注他，用于在前端标记"互粉"
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map<String, Object> map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap<>();
                }
                String relationship = redis.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":"
                        + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
            }
        }

        return list;
    }
}
