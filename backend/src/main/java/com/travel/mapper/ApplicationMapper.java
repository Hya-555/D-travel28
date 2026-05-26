package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    @Select("SELECT * FROM application WHERE status = 'COMPLETED' AND DATE(complete_time) = #{date}")
    List<Application> findCompletedByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM application WHERE group_code = #{groupCode} AND departure_date = #{departureDate} AND contact_name = #{contactName}")
    List<Application> findByGroupAndContact(@Param("groupCode") String groupCode,
                                            @Param("departureDate") LocalDate departureDate,
                                            @Param("contactName") String contactName);
}
