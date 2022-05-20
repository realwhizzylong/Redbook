package com.example.thrd.mapper;

import com.example.thrd.vo.FanVO;
import com.example.thrd.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom {

    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

    public List<FanVO> queryMyFans(@Param("paramMap") Map<String, Object> map);
}