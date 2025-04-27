package com.example.demo.utils;

import com.example.demo.dto.OAformDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateReminderUtils {
    private static final String URL = "/api/workflow/paService/doCreateRequest";

    public static String setReminder(OAformDto dto) throws Exception {
        OATokenUtils oaTokenUtils = new OATokenUtils();
        Map<String, String> headers = new HashMap<>();
        headers.put("token", oaTokenUtils.getToken());
        headers.put("appid", OATokenUtils.APP_ID);
        String userid = oaTokenUtils.rsa.encrypt(null, dto.getOaId(), null, "utf8", oaTokenUtils.spk, false);
        headers.put("userid", userid);
        Map<String, Object> inMap = new HashMap<>();
        List<Map<String, Object>> mainData = new ArrayList<>();
        /*
         * 主表数据设置
         * */
        String[][] dataConfig = {
                {"gqrq", dto.getGqrq()},
                {"cnccxmc", dto.getCnccxmc()},
                {"gqyy", dto.getGqyy()}
        };
        for (String[] strings : dataConfig) {
            Map<String, Object> map = new HashMap<>();
            map.put("fieldName", strings[0]);
            map.put("fieldValue", strings[1]);
            mainData.add(map);
        }
        inMap.put("mainData", new ObjectMapper().writeValueAsString(mainData));
        inMap.put("workflowId", "227");
        inMap.put("requestName", "text");
        return OATokenUtils.HttpManager.postDataSSL(OATokenUtils.IP + URL, inMap, headers);
    }

}
