package com.agrimatch.contract.service.impl;

import com.agrimatch.common.api.ResultCode;
import com.agrimatch.common.exception.ApiException;
import com.agrimatch.contract.domain.BusContract;
import com.agrimatch.contract.dto.ContractCreateRequest;
import com.agrimatch.contract.dto.ContractQuery;
import com.agrimatch.contract.dto.ContractResponse;
import com.agrimatch.contract.dto.ContractUpdateRequest;
import com.agrimatch.contract.mapper.ContractMapper;
import com.agrimatch.contract.service.ContractService;
import com.agrimatch.user.domain.SysUser;
import com.agrimatch.user.mapper.UserMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {
    private final ContractMapper contractMapper;
    private final UserMapper userMapper;

    public ContractServiceImpl(ContractMapper contractMapper, UserMapper userMapper) {
        this.contractMapper = contractMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Long create(Long userId, ContractCreateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new ApiException(401, "未登录");
        if (u.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案（绑定公司）");

        BusContract c = new BusContract();
        c.setCompanyId(u.getCompanyId());
        c.setUserId(userId);

        c.setContractNo(req.getContractNo().trim());
        c.setContractType(req.getContractType().trim());
        c.setTitle(req.getTitle().trim());
        c.setPartyA(req.getPartyA().trim());
        c.setPartyB(req.getPartyB().trim());
        c.setProductName(emptyToNull(req.getProductName()));
        c.setQuantity(req.getQuantity());
        c.setUnit(emptyToNull(req.getUnit()));
        c.setUnitPrice(req.getUnitPrice());
        c.setTotalAmount(calcTotal(req.getQuantity(), req.getUnitPrice()));
        c.setDeliveryDate(req.getDeliveryDate());
        c.setDeliveryAddress(emptyToNull(req.getDeliveryAddress()));
        c.setPaymentMethod(emptyToNull(req.getPaymentMethod()));
        c.setTerms(emptyToNull(req.getTerms()));
        c.setStatus(normalizeStatus(req.getStatus()));
        if ("signed".equalsIgnoreCase(c.getStatus())) {
            c.setSignTime(LocalDateTime.now());
        }

        int rows = contractMapper.insert(c);
        if (rows != 1 || c.getId() == null) throw new ApiException(ResultCode.SERVER_ERROR);
        return c.getId();
    }

    @Override
    public ContractResponse getById(Long viewerUserId, Long id) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        BusContract c = contractMapper.selectById(id);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);
        // MVP：只允许登录用户查看（后续可扩展 company 级权限）
        return toResponse(c);
    }

    @Override
    public List<ContractResponse> list(Long viewerUserId, ContractQuery q) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        SysUser u = userMapper.selectById(viewerUserId);
        if (u == null) throw new ApiException(401, "未登录");
        if (u.getCompanyId() == null) throw new ApiException(ResultCode.PARAM_ERROR.getCode(), "请先完善公司档案（绑定公司）");

        if (q == null) q = new ContractQuery();
        // 默认：查看自己公司的合同
        if (q.getCompanyId() == null) q.setCompanyId(u.getCompanyId());
        if (q.getKeyword() != null && q.getKeyword().length() > 64) q.setKeyword(q.getKeyword().substring(0, 64));

        List<BusContract> list = contractMapper.selectList(q);
        List<ContractResponse> out = new ArrayList<>();
        for (BusContract c : list) out.add(toResponse(c));
        return out;
    }

    @Override
    public void update(Long userId, Long id, ContractUpdateRequest req) {
        if (userId == null) throw new ApiException(401, "未登录");
        BusContract c = new BusContract();
        c.setId(id);
        c.setUserId(userId);
        c.setTitle(emptyToNull(req.getTitle()));
        c.setContractType(emptyToNull(req.getContractType()));
        c.setPartyA(emptyToNull(req.getPartyA()));
        c.setPartyB(emptyToNull(req.getPartyB()));
        c.setProductName(emptyToNull(req.getProductName()));
        c.setQuantity(req.getQuantity());
        c.setUnit(emptyToNull(req.getUnit()));
        c.setUnitPrice(req.getUnitPrice());
        c.setTotalAmount(calcTotal(req.getQuantity(), req.getUnitPrice()));
        c.setDeliveryDate(req.getDeliveryDate());
        c.setDeliveryAddress(emptyToNull(req.getDeliveryAddress()));
        c.setPaymentMethod(emptyToNull(req.getPaymentMethod()));
        c.setTerms(emptyToNull(req.getTerms()));

        if (req.getStatus() != null) {
            String s = normalizeStatus(req.getStatus());
            c.setStatus(s);
            if ("signed".equalsIgnoreCase(s)) {
                c.setSignTime(LocalDateTime.now());
            }
        }

        int rows = contractMapper.update(c);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    @Override
    public void delete(Long userId, Long id) {
        if (userId == null) throw new ApiException(401, "未登录");
        int rows = contractMapper.logicalDelete(id, userId);
        if (rows != 1) throw new ApiException(ResultCode.NOT_FOUND);
    }

    @Override
    public byte[] generatePdf(Long viewerUserId, Long id) {
        if (viewerUserId == null) throw new ApiException(401, "未登录");
        BusContract c = contractMapper.selectById(id);
        if (c == null) throw new ApiException(ResultCode.NOT_FOUND);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 56;
                float y = page.getMediaBox().getHeight() - margin;
                float x = margin;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(x, y);
                cs.showText(safeAscii(c.getTitle()));
                cs.endText();

                y -= 28;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(x, y);
                cs.showText("CONTRACT_NO: " + safeAscii(c.getContractNo()));
                cs.endText();

                y -= 18;
                y = writeBlock(cs, x, y, "Party A: " + safeAscii(c.getPartyA()));
                y = writeBlock(cs, x, y, "Party B: " + safeAscii(c.getPartyB()));
                y = writeBlock(cs, x, y, "Product: " + safeAscii(nvl(c.getProductName(), "-")));

                String qty = c.getQuantity() != null ? c.getQuantity().toPlainString() : "-";
                String unit = nvl(c.getUnit(), "-");
                y = writeBlock(cs, x, y, "Quantity: " + qty + " " + safeAscii(unit));
                y = writeBlock(cs, x, y, "Unit Price: " + (c.getUnitPrice() != null ? c.getUnitPrice().toPlainString() : "-"));
                y = writeBlock(cs, x, y, "Total Amount: " + (c.getTotalAmount() != null ? c.getTotalAmount().toPlainString() : "-"));
                y = writeBlock(cs, x, y, "Delivery: " + (c.getDeliveryAddress() != null ? safeAscii(c.getDeliveryAddress()) : "-"));
                y = writeBlock(cs, x, y, "Payment: " + (c.getPaymentMethod() != null ? safeAscii(c.getPaymentMethod()) : "-"));

                y -= 10;
                y = writeMultiline(cs, x, y, "Terms:", nvl(c.getTerms(), "N/A"));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            byte[] bytes = baos.toByteArray();

            String hash = sha256Hex(bytes);
            String tag = "SHA256:" + hash;
            // 记录存证 hash（MVP：仅合同创建者可更新）
            contractMapper.updatePdfHash(c.getId(), c.getUserId(), tag);
            return bytes;
        } catch (Exception e) {
            throw new ApiException(ResultCode.SERVER_ERROR.getCode(), "PDF 生成失败");
        }
    }

    private static String normalizeStatus(String s) {
        if (s == null) return "draft";
        String v = s.trim().toLowerCase();
        return switch (v) {
            case "draft", "pending", "signed", "executing", "completed", "cancelled" -> v;
            default -> "draft";
        };
    }

    private static BigDecimal calcTotal(BigDecimal qty, BigDecimal unitPrice) {
        if (qty == null || unitPrice == null) return null;
        return qty.multiply(unitPrice);
    }

    private static String emptyToNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private static ContractResponse toResponse(BusContract c) {
        ContractResponse o = new ContractResponse();
        o.setId(c.getId());
        o.setCompanyId(c.getCompanyId());
        o.setUserId(c.getUserId());
        o.setContractNo(c.getContractNo());
        o.setContractType(c.getContractType());
        o.setTitle(c.getTitle());
        o.setPartyA(c.getPartyA());
        o.setPartyB(c.getPartyB());
        o.setProductName(c.getProductName());
        o.setQuantity(c.getQuantity());
        o.setUnit(c.getUnit());
        o.setUnitPrice(c.getUnitPrice());
        o.setTotalAmount(c.getTotalAmount());
        o.setDeliveryDate(c.getDeliveryDate());
        o.setDeliveryAddress(c.getDeliveryAddress());
        o.setPaymentMethod(c.getPaymentMethod());
        o.setTerms(c.getTerms());
        o.setStatus(c.getStatus());
        o.setSignTime(c.getSignTime());
        o.setPdfHash(c.getPdfHash());
        o.setCreateTime(c.getCreateTime());
        o.setUpdateTime(c.getUpdateTime());
        return o;
    }

    private static float writeBlock(PDPageContentStream cs, float x, float y, String line) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(x, y);
        cs.showText(safeAscii(line));
        cs.endText();
        return y - 14;
    }

    private static float writeMultiline(PDPageContentStream cs, float x, float y, String title, String content) throws Exception {
        y = writeBlock(cs, x, y, title);
        String[] lines = content.split("\\r?\\n");
        for (String ln : lines) {
            y = writeBlock(cs, x + 12, y, ln);
        }
        return y;
    }

    // PDFBox Type1 字体对中文支持有限，这里先做 MVP：替换为可打印 ASCII（避免生成失败）
    private static String safeAscii(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char ch : s.toCharArray()) {
            out.append(ch <= 0x7E ? ch : ' ');
        }
        return out.toString().trim();
    }

    private static String nvl(String s, String d) {
        return (s == null || s.isBlank()) ? d : s;
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(bytes);
        try (Formatter f = new Formatter()) {
            for (byte b : dig) f.format("%02x", b);
            return f.toString();
        }
    }
}


