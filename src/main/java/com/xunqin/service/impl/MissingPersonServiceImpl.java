package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.vo.MissingPersonVO;
import com.xunqin.entity.MissingPerson;
import com.xunqin.mapper.MissingPersonMapper;
import com.xunqin.service.MissingPersonService;
import com.xunqin.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.xunqin.entity.User;

@Service
public class MissingPersonServiceImpl implements MissingPersonService {

    @Autowired
    private MissingPersonMapper missingPersonMapper;

    @Autowired
    private com.xunqin.service.UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.xunqin.mapper.UserMapper userMapper;

    @Override
    public Page<MissingPerson> searchMissingPersons(String name, String gender, String location, String startDate, String endDate,
                                                  Integer pageNum, Integer pageSize) {
        Page<MissingPerson> page = new Page<>(pageNum, pageSize);
        QueryWrapper<MissingPerson> wrapper = new QueryWrapper<>();

        wrapper.eq("status", 1); // 1: 已通过

        if (name != null) {
            wrapper.like("name", name);
        }
        if (gender != null) {
            wrapper.eq("gender", gender);
        }
        if (location != null) {
            wrapper.like("missing_location", location);
        }
        if (startDate != null) {
            wrapper.ge("missing_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("missing_date", endDate);
        }

        wrapper.orderByDesc("create_time");
        return missingPersonMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public MissingPerson getMissingPersonById(Long id) {
        MissingPerson missingPerson = missingPersonMapper.selectById(id);
        if (missingPerson == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        // 增加浏览次数
        missingPerson.setViewCount(missingPerson.getViewCount() + 1);
        missingPersonMapper.updateById(missingPerson);

        return missingPerson;
    }

    @Override
    public List<MissingPerson> getHotMissingPersons(Integer limit) {
        Page<MissingPerson> page = new Page<>(1, limit);
        QueryWrapper<MissingPerson> wrapper = new QueryWrapper<>();

        wrapper.eq("status", 1)
               .orderByDesc("viewCount")
               .orderByDesc("create_time");

        return missingPersonMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    @Transactional
    public MissingPerson createMissingPerson(Long userId, MissingPerson missingPerson) {
        missingPerson.setSeekerId(userId);
        missingPerson.setViewCount(0);
        missingPerson.setClueCount(0);
        missingPerson.setStatus(0); // 0: 待审核, 1: 已通过, 2: 已拒绝
        missingPerson.setMatchStatus(0); // 0: 未匹配, 1: 匹配中, 2: 已匹配
        missingPerson.setCreateTime(LocalDateTime.now());

        missingPersonMapper.insert(missingPerson);
        
        // 发送通知给管理员
        List<com.xunqin.entity.User> admins = userService.getAdmins();
        for (com.xunqin.entity.User admin : admins) {
            String title = "新的寻亲信息待审核";
            String content = "用户" + userService.getUserById(userId).getUsername() + "提交了新的寻亲信息" + missingPerson.getName() + "，需要审核。";
            notificationService.sendAdminNotification(admin.getId(), title, content, "MISSING_PERSON_CREATED", missingPerson.getId(), "MISSING_PERSON", 1);
        }
        
        return missingPerson;
    }

    @Override
    @Transactional
    public MissingPerson createMissingPerson(Long userId, MissingPerson missingPerson, MultipartFile[] photos) {
        // 处理图片上传
        if (photos != null && photos.length > 0) {
            String photoUrls = uploadPhotos(photos);
            missingPerson.setPhotos(photoUrls);
        }
        
        return createMissingPerson(userId, missingPerson);
    }

    private String uploadPhotos(MultipartFile[] photos) {
        List<String> photoUrls = new ArrayList<>();
        String uploadDir = System.getProperty("user.dir") + "/uploads/missing-persons/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (MultipartFile photo : photos) {
            if (!photo.isEmpty()) {
                try {
                    String originalFilename = photo.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + extension;
                    File dest = new File(uploadDir + newFilename);
                    photo.transferTo(dest);
                    photoUrls.add("/uploads/missing-persons/" + newFilename);
                } catch (IOException e) {
                    throw new BusinessException("图片上传失败: " + e.getMessage());
                }
            }
        }

        return String.join(",", photoUrls);
    }

    @Override
    @Transactional
    public MissingPerson updateMissingPerson(Long id, Long userId, MissingPerson missingPerson) {
        MissingPerson existing = missingPersonMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        // 检查权限：管理员可以修改任何寻亲信息，普通用户只能修改自己的
        com.xunqin.entity.User user = userService.getUserById(userId);
        if (!user.getRole().equals("ADMIN") && !existing.getSeekerId().equals(userId)) {
            throw new BusinessException("无权修改他人的寻亲信息");
        }

        existing.setName(missingPerson.getName());
        existing.setGender(missingPerson.getGender());
        existing.setBirthDate(missingPerson.getBirthDate());
        existing.setAgeAtMissing(missingPerson.getAgeAtMissing());
        existing.setCurrentAge(missingPerson.getCurrentAge());
        existing.setHeight(missingPerson.getHeight());
        existing.setWeight(missingPerson.getWeight());
        existing.setAppearance(missingPerson.getAppearance());
        existing.setClothing(missingPerson.getClothing());
        existing.setSpecialFeatures(missingPerson.getSpecialFeatures());
        existing.setMissingDate(missingPerson.getMissingDate());
        existing.setMissingLocation(missingPerson.getMissingLocation());
        existing.setMissingLocationLat(missingPerson.getMissingLocationLat());
        existing.setMissingLocationLng(missingPerson.getMissingLocationLng());
        existing.setMissingCause(missingPerson.getMissingCause());
        existing.setDescription(missingPerson.getDescription());
        existing.setPhotos(missingPerson.getPhotos());
        existing.setVideos(missingPerson.getVideos());
        existing.setContactName(missingPerson.getContactName());
        existing.setContactPhone(missingPerson.getContactPhone());
        existing.setContactEmail(missingPerson.getContactEmail());
        existing.setReward(missingPerson.getReward());
        
        // 管理员可以直接修改状态，普通用户修改后需要重新审核
        if (user.getRole().equals("ADMIN")) {
            existing.setStatus(missingPerson.getStatus());
        } else {
            existing.setStatus(0); // 修改后重新审核
            
            // 发送通知给管理员
            List<com.xunqin.entity.User> admins = userService.getAdmins();
            for (com.xunqin.entity.User admin : admins) {
                String title = "寻亲信息修改待审核";
                String content = "用户" + user.getUsername() + "修改了寻亲信息" + existing.getName() + "，需要重新审核。";
                notificationService.sendAdminNotification(admin.getId(), title, content, "MISSING_PERSON_UPDATED", existing.getId(), "MISSING_PERSON", 1);
            }
        }
        
        existing.setUpdateTime(LocalDateTime.now());

        missingPersonMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public MissingPerson updateMissingPerson(Long id, Long userId, MissingPerson missingPerson, MultipartFile[] photos) {
        MissingPerson existing = missingPersonMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        // 检查权限：管理员可以修改任何寻亲信息，普通用户只能修改自己的
        com.xunqin.entity.User user = userService.getUserById(userId);
        if (!user.getRole().equals("ADMIN") && !existing.getSeekerId().equals(userId)) {
            throw new BusinessException("无权修改他人的寻亲信息");
        }

        existing.setName(missingPerson.getName());
        existing.setGender(missingPerson.getGender());
        existing.setBirthDate(missingPerson.getBirthDate());
        existing.setAgeAtMissing(missingPerson.getAgeAtMissing());
        existing.setCurrentAge(missingPerson.getCurrentAge());
        existing.setHeight(missingPerson.getHeight());
        existing.setWeight(missingPerson.getWeight());
        existing.setAppearance(missingPerson.getAppearance());
        existing.setClothing(missingPerson.getClothing());
        existing.setSpecialFeatures(missingPerson.getSpecialFeatures());
        existing.setMissingDate(missingPerson.getMissingDate());
        existing.setMissingLocation(missingPerson.getMissingLocation());
        existing.setMissingLocationLat(missingPerson.getMissingLocationLat());
        existing.setMissingLocationLng(missingPerson.getMissingLocationLng());
        existing.setMissingCause(missingPerson.getMissingCause());
        existing.setDescription(missingPerson.getDescription());
        existing.setContactName(missingPerson.getContactName());
        existing.setContactPhone(missingPerson.getContactPhone());
        existing.setContactEmail(missingPerson.getContactEmail());
        existing.setReward(missingPerson.getReward());
        
        // 处理图片上传
        if (photos != null && photos.length > 0) {
            // 删除旧图片
            String oldPhotos = existing.getPhotos();
            if (oldPhotos != null && !oldPhotos.isEmpty()) {
                deletePhotos(oldPhotos);
            }
            
            // 上传新图片
            String photoUrls = uploadPhotos(photos);
            existing.setPhotos(photoUrls);
        }
        
        // 管理员可以直接修改状态，普通用户修改后需要重新审核
        if (user.getRole().equals("ADMIN")) {
            existing.setStatus(missingPerson.getStatus());
        } else {
            existing.setStatus(0); // 修改后重新审核
            
            // 发送通知给管理员
            List<com.xunqin.entity.User> admins = userService.getAdmins();
            for (com.xunqin.entity.User admin : admins) {
                String title = "寻亲信息修改待审核";
                String content = "用户" + user.getUsername() + "修改了寻亲信息" + existing.getName() + "，需要重新审核。";
                notificationService.sendAdminNotification(admin.getId(), title, content, "MISSING_PERSON_UPDATED", existing.getId(), "MISSING_PERSON", 1);
            }
        }
        
        existing.setUpdateTime(LocalDateTime.now());

        missingPersonMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteMissingPerson(Long id, Long userId) {
        MissingPerson missingPerson = missingPersonMapper.selectById(id);
        if (missingPerson == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        // 检查权限：管理员可以删除任何寻亲信息，普通用户只能删除自己的
        if (userId != null) {
            com.xunqin.entity.User user = userService.getUserById(userId);
            if (!user.getRole().equals("ADMIN") && !missingPerson.getSeekerId().equals(userId)) {
                throw new BusinessException("无权删除他人的寻亲信息");
            }
        }

        // 删除关联的图片文件
        String photos = missingPerson.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            deletePhotos(photos);
        }

        missingPersonMapper.deleteByIdPhysical(id);
    }

    /**
     * 删除图片文件
     */
    private void deletePhotos(String photos) {
        String[] photoUrls = photos.split(",");
        for (String photoUrl : photoUrls) {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                try {
                    // 从URL中提取文件路径
                    String filePath = photoUrl;
                    if (photoUrl.startsWith("/uploads/")) {
                        filePath = System.getProperty("user.dir") + photoUrl;
                    }
                    
                    java.nio.file.Path path = java.nio.file.Paths.get(filePath);
                    if (java.nio.file.Files.exists(path)) {
                        java.nio.file.Files.delete(path);
                        System.out.println("删除图片文件: " + filePath);
                    }
                } catch (IOException e) {
                    System.err.println("删除图片文件失败: " + photoUrl + ", 错误: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Page<MissingPerson> getMissingPersonsBySeeker(Long userId, Integer pageNum, Integer pageSize) {
        Page<MissingPerson> page = new Page<>(pageNum, pageSize);
        QueryWrapper<MissingPerson> wrapper = new QueryWrapper<>();

        wrapper.eq("seeker_id", userId)
               .orderByDesc("create_time");

        return missingPersonMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void approveMissingPerson(Long id, String approvalRemark) {
        MissingPerson missingPerson = missingPersonMapper.selectById(id);
        if (missingPerson == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        Long seekerId = missingPerson.getSeekerId();
        com.xunqin.entity.User seeker = userService.getUserById(seekerId);

        missingPerson.setStatus(1);
        missingPerson.setUpdateTime(LocalDateTime.now());

        missingPersonMapper.updateById(missingPerson);

        // 发送通知给寻亲者
        if (seeker != null) {
            String title = "寻亲信息审核通过"; 
            String content = "您提交的寻亲信息\"" + missingPerson.getName() + "\"已审核通过，现在可以在网站上查看。";
            notificationService.sendSeekerNotification(seekerId, title, content, "MISSING_PERSON_APPROVED", id, "MISSING_PERSON", null);
        }
    }

    @Override
    @Transactional
    public void rejectMissingPerson(Long id, String rejectionRemark) {
        MissingPerson missingPerson = missingPersonMapper.selectById(id);
        if (missingPerson == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        missingPerson.setStatus(2);
        missingPerson.setRejectReason(rejectionRemark);
        missingPerson.setUpdateTime(LocalDateTime.now());

        missingPersonMapper.updateById(missingPerson);
    }

    @Override
    public Page<MissingPersonVO> getMissingPersonsForAdmin(String status, String name, String username, Integer pageNum, Integer pageSize) {
        Page<MissingPerson> page = new Page<>(pageNum, pageSize);
        QueryWrapper<MissingPerson> wrapper = new QueryWrapper<>();

        if (status != null) {
            wrapper.eq("status", Integer.parseInt(status));
        }
        if (name != null) {
            wrapper.like("name", name);
        }
        if (username != null) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.select("id").like("username", username);
            List<User> users = userMapper.selectList(userWrapper);
            if (users.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            List<Long> userIds = new ArrayList<>();
            for (User u : users) {
                userIds.add(u.getId());
            }
            wrapper.in("seeker_id", userIds);
        }

        wrapper.orderByDesc("create_time");
        Page<MissingPerson> resultPage = missingPersonMapper.selectPage(page, wrapper);

        Page<MissingPersonVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<MissingPersonVO> voList = new ArrayList<>();
        for (MissingPerson p : resultPage.getRecords()) {
            String seekerName = "";
            if (p.getSeekerId() != null) {
                try {
                    User u = userService.getUserById(p.getSeekerId());
                    if (u != null) {
                        seekerName = u.getUsername();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            voList.add(new MissingPersonVO(p, seekerName));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void updateMissingPersonStatus(Long id, String status) {
        MissingPerson missingPerson = missingPersonMapper.selectById(id);
        if (missingPerson == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        missingPerson.setStatus(Integer.parseInt(status));
        missingPerson.setUpdateTime(LocalDateTime.now());

        missingPersonMapper.updateById(missingPerson);
    }

    @Override
    public List<Long> getMissingPersonIdsByUserId(Long userId) {
        QueryWrapper<MissingPerson> wrapper = new QueryWrapper<>();
        wrapper.select("id").eq("seeker_id", userId);
        
        List<MissingPerson> missingPersons = missingPersonMapper.selectList(wrapper);
        List<Long> ids = new java.util.ArrayList<>();
        
        for (MissingPerson person : missingPersons) {
            ids.add(person.getId());
        }
        
        return ids;
    }
}
