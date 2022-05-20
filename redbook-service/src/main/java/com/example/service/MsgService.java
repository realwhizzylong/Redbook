package com.example.service;

import com.example.thrd.mo.MessageMO;

import java.util.List;
import java.util.Map;

public interface MsgService {

    /**
     * 创建消息
     */
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map<String, Object> msgContent);

    /**
     * 查询消息列表
     */
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize);
}
