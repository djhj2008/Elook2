package com.elook.client.exception;

/**
 * Created by haiming on 5/28/16.
 */
public class ErrorCode{
    public int status_code;
    public String message;

    public ErrorCode(int code, String msg){
        this.status_code = code;
        this.message = msg;
    }

    public ErrorCode(int code ){
        this.status_code = code;
    }
}
