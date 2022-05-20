package com.example.thrd.mo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document("message")
public class MessageMO {

    @Id
    private String id;

    @Field("fromUserId")
    private String fromUserId;

    @Field("fromNickname")
    private String fromNickname;

    @Field("fromFace")
    private String fromFace;

    @Field("toUserId")
    private String toUserId;

    @Field("msgType")
    private Integer msgType;

    @Field("msgContent")
    private Map<String, Object> msgContent;

    @Field("createTime")
    private Date createTime;
}
