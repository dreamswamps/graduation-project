package com.example.Controller;

import com.example.POJO.GameScore;
import com.example.Service.GameScoreService;
import com.example.Util.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameScoreController {
    @Resource
    private GameScoreService gameScoreService;

    @GetMapping("/selectbyuid")
    public Result selectByUid(@RequestHeader("X-Current-User-ID") Integer currentUserId) {
        return Result.success(gameScoreService.selectByUid(currentUserId));
    }

    @GetMapping("/selecttopscore/{limit}")
    public Result selectTopScore(@PathVariable(required = false) Integer limit) {
        return Result.success(gameScoreService.selectTopScore(limit));
    }

    @PostMapping("/insertnewscore")
    public Result insertNewScore(@RequestBody GameScore gameScore,
                                 @RequestHeader("X-Current-User-ID") Integer currentUserId) {
        gameScoreService.insertNewScore(gameScore, currentUserId);
        return Result.success();
    }


}
