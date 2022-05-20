package com.example.thrd.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersVO implements Serializable {

    private String id;
    private String mobile;
    private String nickname;
    private String imoocNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;
    private static final long serialVersionUID = 1L;

    private String userToken; //用户token，返回给前端
    private Integer myFollowsCounts;
    private Integer myFansCounts;
    private Integer totalLikeMeCounts;
}