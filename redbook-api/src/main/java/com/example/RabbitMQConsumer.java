package com.example;

import com.example.base.RabbitMQConfig;
import com.example.enums.MessageEnum;
import com.example.exceptions.GraceException;
import com.example.grace.result.ResponseStatusEnum;
import com.example.service.MsgService;
import com.example.thrd.mo.MessageMO;
import com.example.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMQConsumer {

    @Autowired
    private MsgService msgService;

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_SYS_MSG})
    public void watchQueue(String payload, Message message) {

        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        log.info(payload);
        log.info(routingKey);

        if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.FOLLOW_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.FOLLOW_YOU.type,
                    null);
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_VLOG.type,
                    null);
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.COMMENT_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.COMMENT_VLOG.type,
                    null);
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.REPLY_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.REPLY_YOU.type,
                    null);
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_COMMENT.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_COMMENT.type,
                    null);
        } else {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
    }
}
