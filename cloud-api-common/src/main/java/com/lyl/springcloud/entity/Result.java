package com.lyl.springcloud.entity;

import cn.hutool.core.collection.ListUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 罗亚龙
 * @date 2022/1/21 13:14
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private Integer code;
    private String msg;
    private Object data = ListUtil.empty();
    
    public static Result success(){
        return success ("操作成功");
    }

    public static Result success(String msg) {
        return success(msg,ListUtil.empty());
    }


    public static Result success(Object data) {
        return success("操作成功", data);
    }

    public static Result success(String msg, Object data) {
        return success(200, msg, data);
    }

    public static Result success(Integer code, String msg, Object data){
        return new Result(code,msg,data);
    }


    public static Result fail() {
        return fail("操作失败");
    }

    public static Result fail(String msg) {
        return fail(400, msg);
    }

    public static Result fail(Integer code, String msg) {
        return fail(code, msg, ListUtil.empty());
    }

    public static Result fail(Object data) {
        return fail(400,"操作失败" , data);
    }

    public static Result fail(Integer code, String msg, Object data){
        return new Result(code,msg,data);
    }
    
}
