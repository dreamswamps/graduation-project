package com.example.Mapper;

import com.example.POJO.GameScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GameScoreMapper {
    @Select("SELECT * FROM game_score WHERE gamer_uid = #{uid} and is_deleted = 0")
    List<GameScore> selectByUid(Integer uid);

    List<GameScore> selectAllByIds(List<Integer> ids);

    List<GameScore> selectTopScore(Integer limit);
}
