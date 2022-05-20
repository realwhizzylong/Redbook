package com.example.controller;

import com.example.MinIOConfig;
import com.example.base.BaseInfoProperties;
import com.example.enums.FileTypeEnum;
import com.example.enums.UserInfoModifyType;
import com.example.grace.result.GraceJSONResult;
import com.example.grace.result.ResponseStatusEnum;
import com.example.service.UserService;
import com.example.thrd.bo.UpdateUsersBO;
import com.example.thrd.pojo.Users;
import com.example.thrd.vo.UsersVO;
import com.example.utils.MinIOUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "UserInfo用户信息测试接口")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    @Autowired
    private MinIOConfig minIOConfig;

    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) throws Exception {

        Users user = userService.getUser(userId);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        //我的关注博主总数
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        //我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        //用户获赞总数
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_LIKED_COUNTS + ":" + userId);

        int myFollowsCounts = 0;
        int myFansCounts = 0;
        int likedVlogerCounts = 0;

        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.parseInt(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.parseInt(myFansCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.parseInt(likedVlogerCountsStr);
        }

        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(likedVlogerCounts);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdateUsersBO pendingUserInfo,
                                          @RequestParam int type) throws Exception {

        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        Users updatedUser = userService.updateUserInfo(pendingUserInfo, type);

        return GraceJSONResult.ok(updatedUser);
    }

    @PostMapping("modifyImage")
    public GraceJSONResult upload(@RequestParam String userId,
                                  @RequestParam int type,
                                  MultipartFile file) throws Exception {

        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String filename = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());

        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + filename;

        UpdateUsersBO updateUsersBO = new UpdateUsersBO();
        updateUsersBO.setId(userId);
        if (type == FileTypeEnum.BGIMG.type) {
            updateUsersBO.setBgImg(imgUrl);
        } else {
            updateUsersBO.setFace(imgUrl);
        }
        Users user = userService.updateUserInfo(updateUsersBO);

        return GraceJSONResult.ok(user);
    }

}
