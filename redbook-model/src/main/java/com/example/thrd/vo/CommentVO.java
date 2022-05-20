package com.example.thrd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentVO {

    private String id;
    private String commentId;
    private String vlogerId;
    private String vlogId;
    private String fatherCommentId;
    private String commentUserId;
    private String commentUserNickname;
    private String commentUserFace;
    private String content;
    private Integer likeCounts;
    private String replyUserNickname;
    private Date createTime;
    private Integer isLike = 0;
}
