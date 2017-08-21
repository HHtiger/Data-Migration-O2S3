package com.tiger.mapper.newDb;

import com.tiger.model.Qyzt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface NewMapper {

    @Select("select * from ${tablename} where qyzt is null and XZQH = #{xzqh}")
    List<Qyzt> queryAll(@Param("xzqh")String xzqh,@Param("tablename")String tablename);

    @Select("select count(1) from ${tablename} where qyzt is null and XZQH = #{xzqh}")
    int countUnFinishedNumber(@Param("xzqh")String xzqh,@Param("tablename")String tablename);

}