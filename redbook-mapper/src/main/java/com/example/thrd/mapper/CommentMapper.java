package com.example.thrd.mapper;

import com.example.thrd.my.mapper.MyMapper;
import com.example.thrd.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {
}