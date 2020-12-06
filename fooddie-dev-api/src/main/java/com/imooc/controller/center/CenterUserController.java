package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.center.CenterUserBO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.resource.FileUpload;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.DateUtil;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: 用户中心
 * @author: Young
 * @create: 2020-11-16 08:37
 **/

@Api(value = "用户信息接口", tags = {"用户信息接口的api接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value="修改用户信息",notes = "修改用户信息",httpMethod = "POST")
    @PostMapping("update")
    public IMOOCJSONResult update(
                    @ApiParam(name = "userId",value = "用户id", required = true)
                    @RequestParam String userId,
                    @RequestBody @Valid CenterUserBO centerUserBO,
                    BindingResult result,
                    HttpServletRequest request, HttpServletResponse response) {

        //用户信息验证
        if (result.hasErrors()) {
            Map<String, String> errorMap = getErrors(result);
            return IMOOCJSONResult.errorMap(errorMap);
        }

        Users users = centerUserService.updateUserInfo(userId, centerUserBO);
//        users = setNullProperty(users);

        UsersVO usersVO = conventUserVo(users);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);

        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value="修改用户信息",notes = "修改用户信息",httpMethod = "POST")
    @PostMapping("uploadFace")
    public IMOOCJSONResult uploadFace(
        @ApiParam(name = "userId",value = "用户id", required = true)
        @RequestParam String userId,
        @ApiParam(name = "file",value = "用户头像", required = true)
        MultipartFile file,
        HttpServletRequest request, HttpServletResponse response) {

        String fileSpace = fileUpload.getImageUserFaceLocation();
        //加上用户id,区分用户
        String uploadPathPrefix = File.separator + userId;
        if (file != null) {
            FileOutputStream fileOutputStream = null;
            try {
                //上传名称
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    String[] fileNameArr = fileName.split("\\.");
                    //文件后缀名
                    String suffix = fileNameArr[fileNameArr.length - 1];

                    //文件格式验证
                    if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg") ){
                        return IMOOCJSONResult.errorMsg("图片格式不正确");
                    }

                    String newFileName = "face-" + userId + "." + suffix;

                    //最终位置
                    String fianlFacePath = fileSpace + uploadPathPrefix + File.separator + newFileName;

                    uploadPathPrefix += ("/" + newFileName);

                    File outFile = new File(fianlFacePath);
                    if (outFile.getParentFile() != null) {
                        //创建文件夹
                        outFile.getParentFile().mkdirs();
                    }

                    //文件保存到目录
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream, fileOutputStream);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            return IMOOCJSONResult.errorMsg("文件不能为空");
        }

        //获取图片服务地址
        String imageServerUrl = fileUpload.getImageServerUrl();
        //增加时间戳保证浏览器图片刷新
        String finalUserFaceUrl = imageServerUrl + uploadPathPrefix
            + "?t=" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);

        //更新用户头像到数据库
        Users usersResult = centerUserService.updateUserFace(userId,finalUserFaceUrl);
//        usersResult = setNullProperty(usersResult);

        UsersVO usersVO = conventUserVo(usersResult);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);

        return IMOOCJSONResult.ok();
    }


    /**
     * bindingResult错误集合封装map
     * @param result
     * @return
     */
    private Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> fieldErrors = result.getFieldErrors();
        for (FieldError fieldError : fieldErrors){
            //错误属性
            String field = fieldError.getField();
            //错误信息
            String defaultMessage = fieldError.getDefaultMessage();
            map.put(field, defaultMessage);
        }
        return map;
    }

    /**
     * 用户敏感信息清空
     * @param users
     */
    private Users setNullProperty(Users users) {
        users.setPassword(null);
        users.setMobile(null);
        users.setEmail(null);
        users.setUpdatedTime(null);
        users.setCreatedTime(null);
        users.setBirthday(null);
        return users;
    }

}
