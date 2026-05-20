package com.xunqin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunqin.entity.AIChatMessage;
import com.xunqin.entity.MissingPerson;
import com.xunqin.mapper.AIChatMessageMapper;
import com.xunqin.service.AIService;
import com.xunqin.service.MissingPersonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIServiceImpl implements AIService {

    private final MissingPersonService missingPersonService;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api.key}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.api.url}")
    private String deepseekApiUrl;

    public AIServiceImpl(MissingPersonService missingPersonService, AIChatMessageMapper aiChatMessageMapper, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.missingPersonService = missingPersonService;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Map<String, Object>> searchByRegion(String region) {
        // 调用MissingPersonService的搜索方法，根据地区搜索失踪人员
        Page<MissingPerson> page = missingPersonService.searchMissingPersons(null, null, region, null, null, 1, 100);
        List<MissingPerson> missingPersons = page.getRecords();

        // 转换为Map列表，方便前端展示
        List<Map<String, Object>> result = new ArrayList<>();
        for (MissingPerson person : missingPersons) {
            Map<String, Object> personMap = new HashMap<>();
            personMap.put("id", person.getId());
            personMap.put("name", person.getName());
            personMap.put("gender", person.getGender());
            personMap.put("age", person.getCurrentAge());
            personMap.put("missingLocation", person.getMissingLocation());
            personMap.put("missingDate", person.getMissingDate());
            personMap.put("description", person.getDescription());
            Map<String, Object> contactInfo = new HashMap<>();
            contactInfo.put("name", person.getContactName());
            contactInfo.put("phone", person.getContactPhone());
            contactInfo.put("email", person.getContactEmail());
            personMap.put("contactInfo", contactInfo);
            personMap.put("status", person.getStatus());
            result.add(personMap);
        }
        return result;
    }

    @Override
    public String askQuestion(Long userId, String sessionId, String question) {
        // 保存用户问题
        saveChatMessage(userId, sessionId, "user", question);
        
        // 1. 首先尝试使用系统数据和功能回答问题
        String systemAnswer = getSystemAnswer(question);
        if (systemAnswer != null) {
            // 保存系统回答
            saveChatMessage(userId, sessionId, "assistant", systemAnswer);
            return systemAnswer;
        }
        
        // 2. 如果系统无法回答，再调用AI API获取全网内容
        String aiAnswer = getAIAnswer(question);
        // 保存AI回答
        saveChatMessage(userId, sessionId, "assistant", aiAnswer);
        return aiAnswer;
    }
    
    private void saveChatMessage(Long userId, String sessionId, String role, String content) {
        AIChatMessage chatMessage = new AIChatMessage();
        chatMessage.setUserId(userId);
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        aiChatMessageMapper.insert(chatMessage);
    }
    
    private String getSystemAnswer(String question) {
        question = question.toLowerCase();
        
        // 检查是否是关于寻亲信息的问题
        if (question.contains("寻亲") || question.contains("失踪") || question.contains("找人")) {
            // 检查是否包含地区信息
            if (question.contains("北京")) {
                return getRegionAnswer("北京");
            } else if (question.contains("上海")) {
                return getRegionAnswer("上海");
            } else if (question.contains("广州")) {
                return getRegionAnswer("广州");
            } else if (question.contains("深圳")) {
                return getRegionAnswer("深圳");
            } else if (question.contains("成都")) {
                return getRegionAnswer("成都");
            } else if (question.contains("杭州")) {
                return getRegionAnswer("杭州");
            }
        }
        
        // 检查是否是关于系统功能的问题
        if (question.contains("如何") && (question.contains("发布") || question.contains("提交"))) {
            return "您可以在网站首页点击'发布寻亲信息'按钮，填写相关信息并上传照片，提交后等待管理员审核通过即可。";
        }
        
        if (question.contains("如何") && question.contains("搜索")) {
            return "您可以在网站首页的搜索框中输入姓名、性别、失踪地点等信息进行搜索，也可以通过筛选条件缩小搜索范围。";
        }
        
        if (question.contains("如何") && question.contains("联系")) {
            return "在每个寻亲信息详情页，您可以找到联系人信息，包括姓名、电话和邮箱。请直接通过这些方式与寻亲者联系。";
        }
        
        if (question.contains("什么是") && (question.contains("寻亲") || question.contains("失踪"))) {
            return "寻亲是指寻找失踪的亲人或朋友的过程。本网站致力于帮助用户发布寻亲信息、搜索失踪人员，并提供相关的支持和资源。";
        }
        
        if (question.contains("成功率") || question.contains("成功案例")) {
            return "本网站已经帮助多个家庭成功团聚。您可以在'成功案例'页面查看具体的成功故事，了解寻亲过程和经验。";
        }
        
        return null; // 系统无法回答
    }
    
    private String getRegionAnswer(String region) {
        List<Map<String, Object>> results = searchByRegion(region);
        if (results.isEmpty()) {
            return "目前在" + region + "地区没有找到相关的寻亲信息。您可以尝试其他地区，或发布新的寻亲信息。";
        } else {
            StringBuilder answer = new StringBuilder("在" + region + "地区找到以下寻亲信息：\n\n");
            int count = 0;
            for (Map<String, Object> person : results) {
                if (count >= 5) break; // 最多显示5条
                answer.append("姓名：").append(person.get("name"))
                      .append("，性别：").append(person.get("gender"))
                      .append("，年龄：").append(person.get("age"))
                      .append("，失踪地点：").append(person.get("missingLocation"))
                      .append("，失踪时间：").append(person.get("missingDate"))
                      .append("\n");
                count++;
            }
            if (results.size() > 5) {
                answer.append("...等共").append(results.size()).append("条信息");
            }
            return answer.toString();
        }
    }
    
    private String getAIAnswer(String question) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepseekApiKey);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        
        // 构建messages列表
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个寻亲网站的AI助手，专门帮助用户寻找失踪的亲人。请友好、专业地回答用户的问题，提供有关寻亲流程、注意事项等信息。");
        messages.add(systemMessage);
        
        // 用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        // 发送请求
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(deepseekApiUrl, requestEntity, String.class);

        // 解析响应
        try {
            JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
            JsonNode choicesNode = rootNode.get("choices");
            if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).get("message");
                if (messageNode != null) {
                    JsonNode contentNode = messageNode.get("content");
                    if (contentNode != null) {
                        return contentNode.asText();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "抱歉，我无法回答您的问题，请稍后再试。";
    }
}
