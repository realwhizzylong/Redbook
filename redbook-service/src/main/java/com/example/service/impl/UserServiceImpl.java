package com.example.service.impl;

import com.example.enums.Sex;
import com.example.enums.UserInfoModifyType;
import com.example.enums.YesOrNo;
import com.example.exceptions.GraceException;
import com.example.grace.result.ResponseStatusEnum;
import com.example.service.UserService;
import com.example.thrd.bo.UpdateUsersBO;
import com.example.thrd.mapper.UsersMapper;
import com.example.thrd.pojo.Users;
import com.example.utils.DateUtil;
import com.example.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";

    @Override
    public Users queryMobileExist(String mobile) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    @Transactional
    @Override
    public Users createUser(String mobile) {

        //获得全局唯一主键
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);
        user.setMobile(mobile);
        user.setNickname("用户: " + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户: " + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);
        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下～");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);

        return user;
    }

    @Override
    public Users getUser(String userId) {

        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional
    @Override
    public Users updateUserInfo(UpdateUsersBO updateUsersBO) {

        Users pendingUser = new Users();
        BeanUtils.copyProperties(updateUsersBO, pendingUser);

        int result = usersMapper.updateByPrimaryKeySelective(pendingUser);

        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        return getUser(updateUsersBO.getId());
    }

    @Override
    public Users updateUserInfo(UpdateUsersBO updateUsersBO, int type) {

        if (type == UserInfoModifyType.NICKNAME.type) {
            Example example = new Example(Users.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("nickname", updateUsersBO.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }

        if (type == UserInfoModifyType.IMOOCNUM.type) {
            Example example = new Example(Users.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("imoocNum", updateUsersBO.getImoocNum());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            Users currentUser = getUser(updateUsersBO.getId());
            if (currentUser.getCanImoocNumBeUpdated() == YesOrNo.NO.type) {
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            updateUsersBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }

        return updateUserInfo(updateUsersBO);
    }
}
