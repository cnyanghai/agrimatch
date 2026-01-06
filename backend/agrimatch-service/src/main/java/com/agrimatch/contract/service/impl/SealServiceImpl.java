package com.agrimatch.contract.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.company.domain.BusCompany;
import com.agrimatch.company.mapper.CompanyMapper;
import com.agrimatch.contract.domain.BusCompanySeal;
import com.agrimatch.contract.mapper.CompanySealMapper;
import com.agrimatch.contract.service.SealService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class SealServiceImpl implements SealService {

    private final CompanySealMapper sealMapper;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    
    private static final String SEAL_UPLOAD_DIR = "uploads/seals";

    public SealServiceImpl(CompanySealMapper sealMapper, UserMapper userMapper, CompanyMapper companyMapper) {
        this.sealMapper = sealMapper;
        this.userMapper = userMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    @Transactional
    public Long uploadSeal(Long userId, String sealName, String sealType, String sealUrl) {
        SysUser user = requireUserWithCompany(userId);
        
        BusCompanySeal seal = new BusCompanySeal();
        seal.setCompanyId(user.getCompanyId());
        seal.setUserId(userId);
        seal.setSealName(sealName);
        seal.setSealType(sealType != null ? sealType : "official");
        seal.setSealUrl(sealUrl);
        seal.setIsGenerated(false);
        seal.setIsDefault(false);
        
        // 如果是第一个章，设为默认
        List<BusCompanySeal> existing = sealMapper.selectByCompanyId(user.getCompanyId());
        if (existing.isEmpty()) {
            seal.setIsDefault(true);
        }
        
        sealMapper.insert(seal);
        return seal.getId();
    }

    @Override
    @Transactional
    public Long generateSeal(Long userId, String sealName, String sealType) {
        SysUser user = requireUserWithCompany(userId);
        BusCompany company = companyMapper.selectById(user.getCompanyId());
        if (company == null) {
            throw new ApiException(ResultCode.NOT_FOUND.getCode(), "公司信息不存在");
        }
        
        // 生成电子章图片
        String sealUrl = generateSealImage(company.getCompanyName(), sealType);
        
        BusCompanySeal seal = new BusCompanySeal();
        seal.setCompanyId(user.getCompanyId());
        seal.setUserId(userId);
        seal.setSealName(sealName != null ? sealName : company.getCompanyName() + " 电子章");
        seal.setSealType(sealType != null ? sealType : "official");
        seal.setSealUrl(sealUrl);
        seal.setIsGenerated(true);
        seal.setIsDefault(false);
        
        // 如果是第一个章，设为默认
        List<BusCompanySeal> existing = sealMapper.selectByCompanyId(user.getCompanyId());
        if (existing.isEmpty()) {
            seal.setIsDefault(true);
        }
        
        sealMapper.insert(seal);
        return seal.getId();
    }

    @Override
    public List<BusCompanySeal> listByCompany(Long userId) {
        SysUser user = requireUserWithCompany(userId);
        return sealMapper.selectByCompanyId(user.getCompanyId());
    }

    @Override
    public BusCompanySeal getDefault(Long userId) {
        SysUser user = requireUserWithCompany(userId);
        return sealMapper.selectDefaultByCompanyId(user.getCompanyId());
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long sealId) {
        SysUser user = requireUserWithCompany(userId);
        
        BusCompanySeal seal = sealMapper.selectById(sealId);
        if (seal == null || !seal.getCompanyId().equals(user.getCompanyId())) {
            throw new ApiException(ResultCode.NOT_FOUND.getCode(), "电子章不存在");
        }
        
        // 清除当前默认
        sealMapper.clearDefault(user.getCompanyId());
        
        // 设置新默认
        seal.setIsDefault(true);
        sealMapper.update(seal);
    }

    @Override
    public void delete(Long userId, Long sealId) {
        SysUser user = requireUserWithCompany(userId);
        
        BusCompanySeal seal = sealMapper.selectById(sealId);
        if (seal == null || !seal.getCompanyId().equals(user.getCompanyId())) {
            throw new ApiException(ResultCode.NOT_FOUND.getCode(), "电子章不存在");
        }
        
        sealMapper.logicalDelete(sealId);
    }

    private SysUser requireUserWithCompany(Long userId) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new ApiException(401, "未登录");
        if (user.getCompanyId() == null) {
            throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案");
        }
        return user;
    }

    /**
     * 生成电子章图片
     * @param companyName 公司名称
     * @param sealType 章类型
     * @return 图片URL
     */
    private String generateSealImage(String companyName, String sealType) {
        try {
            int size = 200;
            int padding = 10;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 透明背景
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, size, size);
            g2d.setComposite(AlphaComposite.SrcOver);
            
            // 红色
            Color sealColor = new Color(220, 38, 38);
            g2d.setColor(sealColor);
            
            int center = size / 2;
            int radius = (size - padding * 2) / 2;
            
            // 画外圈
            g2d.setStroke(new BasicStroke(4));
            g2d.drawOval(padding, padding, radius * 2, radius * 2);
            
            // 画内圈
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(padding + 8, padding + 8, radius * 2 - 16, radius * 2 - 16);
            
            // 画五角星
            drawStar(g2d, center, center, 25);
            
            // 画公司名称（环形排列）
            Font font = new Font("SimHei", Font.BOLD, 16);
            g2d.setFont(font);
            drawCircularText(g2d, companyName, center, center, radius - 20);
            
            // 画底部文字
            String bottomText = "official".equals(sealType) ? "公章" : 
                               "contract".equals(sealType) ? "合同专用章" : "电子章";
            Font bottomFont = new Font("SimHei", Font.BOLD, 14);
            g2d.setFont(bottomFont);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(bottomText);
            g2d.drawString(bottomText, center - textWidth / 2, center + 50);
            
            g2d.dispose();
            
            // 保存图片
            String fileName = UUID.randomUUID().toString() + ".png";
            String monthDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path dirPath = Paths.get(SEAL_UPLOAD_DIR, monthDir);
            Files.createDirectories(dirPath);
            
            File outputFile = dirPath.resolve(fileName).toFile();
            ImageIO.write(image, "PNG", outputFile);
            
            return "/" + SEAL_UPLOAD_DIR + "/" + monthDir + "/" + fileName;
        } catch (Exception e) {
            throw new ApiException(ResultCode.SERVER_ERROR.getCode(), "生成电子章失败: " + e.getMessage());
        }
    }

    private void drawStar(Graphics2D g2d, int cx, int cy, int r) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            int radius = (i % 2 == 0) ? r : r / 2;
            xPoints[i] = (int) (cx + radius * Math.cos(angle));
            yPoints[i] = (int) (cy - radius * Math.sin(angle));
        }
        
        g2d.fillPolygon(xPoints, yPoints, 10);
    }

    private void drawCircularText(Graphics2D g2d, String text, int cx, int cy, int radius) {
        FontMetrics fm = g2d.getFontMetrics();
        double startAngle = Math.PI / 2 + Math.PI / 6; // 从左上方开始
        double totalAngle = Math.PI + Math.PI / 3; // 跨越的角度
        double anglePerChar = totalAngle / (text.length() - 1);
        
        for (int i = 0; i < text.length(); i++) {
            double angle = startAngle + i * anglePerChar;
            int x = (int) (cx + radius * Math.cos(angle));
            int y = (int) (cy - radius * Math.sin(angle));
            
            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(Math.PI / 2 - angle);
            
            String ch = String.valueOf(text.charAt(i));
            int charWidth = fm.stringWidth(ch);
            g2d.drawString(ch, -charWidth / 2, fm.getAscent() / 2);
            
            g2d.setTransform(oldTransform);
        }
    }
}

