package com.example.thrd.mapper;

import com.example.thrd.my.mapper.MyMapper;
import com.example.thrd.pojo.Comment;
import com.example.thrd.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom extends MyMapper<Comment> {

    public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);

}