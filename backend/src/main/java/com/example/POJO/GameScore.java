package com.example.POJO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameScore {
    private Integer id;
    private Integer gamer_uid;
    private String gamer_name;
    private Integer gamer_score;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date create_time;

    private Integer is_deleted;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date delete_time;

    public void setCreateTime(){
        this.create_time = new Date();
    }

    public void setDeleteTime(){
        this.delete_time = new Date();
    }
}
