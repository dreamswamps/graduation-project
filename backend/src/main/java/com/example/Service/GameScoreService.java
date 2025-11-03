package com.example.Service;

import com.example.Mapper.GameScoreMapper;
import com.example.POJO.GameScore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameScoreService {

    @Resource
    private GameScoreMapper gameScoreMapper;

    @Resource
    private AdminService adminService;

    public List<GameScore> selectByUid(Integer currentUserId) {
        return gameScoreMapper.selectByUid(currentUserId);
    }

    public List<GameScore> selectAllByIds(List<Integer> ids) {
        return gameScoreMapper.selectAllByIds(ids);
    }

    public List<GameScore> selectTopScore(Integer limit) {
        if (limit == null || limit < 3) {
            limit = 3;
        } else if (limit > 7) {
            limit = 7;
        }
        return gameScoreMapper.selectTopScore(limit);
    }

    public void insertNewScore(GameScore gameScore, Integer currentUserId) {
        gameScore.setGamer_uid(currentUserId);
        if (gameScore.getGamer_name() == null || gameScore.getGamer_name().isEmpty()) {
//            默认用户名称
            gameScore.setGamer_name(adminService.SelectById(currentUserId).getName());
        }
        gameScore.setCreateTime();
//        11111111111
    }
}
