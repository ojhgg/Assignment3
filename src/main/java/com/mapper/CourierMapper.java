package com.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pojo.Address;
import com.pojo.Courier;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourierMapper  extends BaseMapper<Courier> {

}
