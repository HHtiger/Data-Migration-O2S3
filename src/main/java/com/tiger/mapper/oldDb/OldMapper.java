package com.tiger.mapper.oldDb;

import com.tiger.model.BRyzp;
import com.tiger.model.BZaFjxx;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OldMapper {

    @Select("select * from ${dbUsername}.B_RYZP where SYSTEMID = #{id}")
    BRyzp findBRyzpById(@Param("id") String id,@Param("dbUsername") String dbUsername);

    @Select("select * from ${dbUsername}.B_ZA_FJXX where SYSTEMID = #{id}")
    BZaFjxx findBZaFjxxById(@Param("id") String id,@Param("dbUsername") String dbUsername);

    @InsertProvider(type =BatchInsertBuilder.class, method = "buildInsertBRyzp")
    int BatchInsertBRyzp(@Param("list") List<BRyzp> list);

    @InsertProvider(type =BatchInsertBuilder.class, method = "buildInsertBZaFjxx")
    int BatchInsertBZaFjxx(@Param("list") List<BZaFjxx> list);

}