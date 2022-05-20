package com.example.thrd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FanVO {

    private String fanId;
    private String nickname;
    private String face;
    private boolean isFriend = true;
}
