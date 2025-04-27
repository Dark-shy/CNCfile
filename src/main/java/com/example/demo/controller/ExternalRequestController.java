package com.example.demo.controller;

import com.example.demo.dto.OAformDto;
import com.example.demo.utils.GenerateReminderUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Controller
@CrossOrigin(origins = "*")
@ResponseBody
public class ExternalRequestController {

    @PostMapping("/toOA")
    public String toOA(@RequestBody OAformDto dto) throws Exception {
        Instant timestamp = Instant.parse(dto.getGqrq());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dto.setGqrq(localDateTime.format(formatter));
        String body = GenerateReminderUtils.setReminder(dto);
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(body, Map.class);
        return map.get("code") != "SUCCESS" ? "已提醒工程师" : Objects.toString(map.get("errMsg"), "");

    }
}
