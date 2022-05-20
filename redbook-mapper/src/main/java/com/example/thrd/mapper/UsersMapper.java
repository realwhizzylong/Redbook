package com.example.thrd.mapper;

import com.example.thrd.my.mapper.MyMapper;
import com.example.thrd.pojo.Users;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersMapper extends MyMapper<Users> {
}