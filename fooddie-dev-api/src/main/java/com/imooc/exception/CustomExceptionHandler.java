package com.imooc.exception;

import com.imooc.utils.IMOOCJSONResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-16 09:57
 **/

@RestControllerAdvice
public class CustomExceptionHandler {

    //捕获异常: MaxUploadSizeExceededException
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public IMOOCJSONResult handlerMaxUploadFile(MaxUploadSizeExceededException maxUploadSizeExceededException){
        return IMOOCJSONResult.errorMsg("文件上传大小不能超过500kb,请降低图片质量再上传");
    }
}
