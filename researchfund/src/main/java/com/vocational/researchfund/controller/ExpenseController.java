package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.dto.AttachmentDTO;
import com.vocational.researchfund.dto.ExpenseDTO;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.entity.User;
import com.vocational.researchfund.mapper.AttachmentMapper;
import com.vocational.researchfund.mapper.ExpenseMapper;
import com.vocational.researchfund.mapper.ProjectMapper;
import com.vocational.researchfund.service.ExpenseService;
import com.vocational.researchfund.service.UserService;
import com.vocational.researchfund.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 经费申请控制器
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 获取经费申请列表（分页）
     * 
     * @param title     申请标题
     * @param projectId 所属项目ID
     * @param type      申请类型
     * @param status    状态
     * @param category  类别
     * @param pageNum   页码，默认1
     * @param pageSize  每页大小，默认10
     * @return 分页后的经费申请列表
     */
    @GetMapping
    public ResponseEntity<Result<PageDTO<ExpenseDTO>>> getExpenseList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> params = new HashMap<>();
        if (title != null)
            params.put("title", title);
        if (projectId != null)
            params.put("projectId", projectId);
        if (type != null)
            params.put("type", type);
        if (status != null)
            params.put("status", status);
        if (category != null)
            params.put("category", category);

        PageDTO<ExpenseDTO> pagedExpenses = expenseService.getAllExpenses(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedExpenses));
    }

    /**
     * 获取当前用户的经费申请列表（分页）
     * 
     * @param title     申请标题
     * @param projectId 所属项目ID
     * @param type      申请类型
     * @param status    状态
     * @param category  类别
     * @param pageNum   页码，默认1
     * @param pageSize  每页大小，默认10
     * @return 分页后的经费申请列表
     */
    @GetMapping("/my")
    public ResponseEntity<Result<PageDTO<ExpenseDTO>>> getMyExpenses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> params = new HashMap<>();
        if (title != null)
            params.put("title", title);
        if (projectId != null)
            params.put("projectId", projectId);
        if (type != null)
            params.put("type", type);
        if (status != null)
            params.put("status", status);
        if (category != null)
            params.put("category", category);

        PageDTO<ExpenseDTO> pagedExpenses = expenseService.getCurrentUserExpenses(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedExpenses));
    }

    /**
     * 获取经费申请详情
     * 
     * @param id 经费申请ID
     * @return 经费申请详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<ExpenseDTO>> getExpenseDetail(@PathVariable Long id) {
        ExpenseDTO expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(Result.success(expense));
    }

    /**
     * 创建经费申请
     * 
     * @param expenseDTO 经费申请DTO
     * @return 创建后的经费申请
     */
    @PostMapping
    public ResponseEntity<Result<ExpenseDTO>> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        try {
            // 设置当前用户作为申请人
            String username = SecurityUtils.getCurrentUsername();
            String realName = SecurityUtils.getCurrentUserRealName();
            Long userId = SecurityUtils.getCurrentUserId();

            expenseDTO.setApplyUserId(userId);
            expenseDTO.setApplyUserName(realName);

            ExpenseDTO createdExpense = expenseService.createExpense(expenseDTO);
            return ResponseEntity.ok(Result.success(createdExpense));
        } catch (Exception e) {
            // 检查是否是预算不足异常
            if (e.getMessage() != null && e.getMessage().contains("申请金额超过项目剩余预算")) {
                return ResponseEntity.ok(Result.error(1001, e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新经费申请
     * 
     * @param id         经费申请ID
     * @param expenseDTO 经费申请DTO
     * @return 更新后的经费申请
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<ExpenseDTO>> updateExpense(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO) {
        try {
            ExpenseDTO updatedExpense = expenseService.updateExpense(id, expenseDTO);
            return ResponseEntity.ok(Result.success(updatedExpense));
        } catch (Exception e) {
            // 检查是否是预算不足异常
            if (e.getMessage() != null && e.getMessage().contains("申请金额超过项目剩余预算")) {
                return ResponseEntity.ok(Result.error(1001, e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 审核经费申请
     * 
     * @param id      经费申请ID
     * @param status  审核状态：approved-已批准,rejected-已拒绝
     * @param comment 审核意见
     * @return 审核后的经费申请
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<Result<ExpenseDTO>> auditExpense(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comment) {
        try {
            // 审核状态校验
            if (!"approved".equals(status) && !"rejected".equals(status)) {
                return ResponseEntity.badRequest().body(Result.error("审核状态参数错误"));
            }

            ExpenseDTO auditedExpense = expenseService.auditExpense(id, status, comment);
            return ResponseEntity.ok(Result.success(auditedExpense));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 删除经费申请
     * 
     * @param id 经费申请ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteExpense(@PathVariable Long id) {
        try {
            // 获取当前经费申请
            ExpenseDTO expense = expenseService.getExpenseById(id);
            if (expense == null) {
                return ResponseEntity.badRequest().body(Result.error("经费申请不存在"));
            }


            expenseService.deleteExpense(id);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 支付经费申请
     * 
     * @param id 经费申请ID
     * @return 支付后的经费申请
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<Result<ExpenseDTO>> payExpense(@PathVariable Long id) {
        try {
            ExpenseDTO paidExpense = expenseService.payExpense(id);
            return ResponseEntity.ok(Result.success(paidExpense));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 上传附件
     * 
     * @param file 附件文件
     * @return 附件信息
     */
    @PostMapping("/upload")
    public ResponseEntity<Result<AttachmentDTO>> uploadAttachment(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error("上传文件不能为空"));
        }

        try {
            // 创建上传目录（如果不存在）
            String uploadDir = "uploads/expenses";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 创建并返回附件信息
            AttachmentDTO attachment = AttachmentDTO.builder()
                    .name(originalFilename)
                    .url("/uploads/expenses/" + filename)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .build();

            return ResponseEntity.ok(Result.success(attachment));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Result.error("文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 负责人自行还款接口
     * 
     * @param id 经费申请ID
     * @return 操作结果
     */
    @PostMapping("/{id}/repay")
    public Result repayExpense(@PathVariable Long id) {
        try {
            // 获取当前经费申请
            ExpenseDTO expense = expenseMapper.selectById(id);
            if (expense == null) {
                return Result.error("经费申请不存在");
            }
            
            // 检查是否为待自行还款状态
            if (!"repayment_pending".equals(expense.getStatus())) {
                return Result.error("只有待自行还款状态的申请才能执行此操作");
            }
            
            // 检查当前用户是否为申请人
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            if (!expense.getApplyUserId().equals(currentUser.getId())) {
                return Result.error("只有申请人才能执行还款操作");
            }
            
            // 获取项目ID和经费金额
            Long projectId = expense.getProjectId();
            BigDecimal amount = expense.getAmount();
            
            // 更新状态为已还款
            expenseMapper.updateStatus(id, "repaid", null, null, "项目负责人已完成还款");
            
            // 减少项目已使用预算
            // 因为updateUsedBudget方法是增加预算，所以这里传入负值来减少
            projectMapper.updateUsedBudget(projectId, amount.negate());
            
            return Result.success("还款操作已完成，项目预算已更新");
        } catch (Exception e) {
            return Result.error("还款失败: " + e.getMessage());
        }
    }

    /**
     * 提交报销凭证接口
     * 
     * @param id 经费申请ID
     * @param attachments 报销凭证附件
     * @return 操作结果
     */
    @PostMapping("/{id}/submit-receipt")
    public Result submitReceipt(@PathVariable Long id, @RequestBody List<AttachmentDTO> attachments) {
        try {
            // 获取当前经费申请
            ExpenseDTO expense = expenseMapper.selectById(id);
            if (expense == null) {
                return Result.error("经费申请不存在");
            }
            
            // 检查是否为待提交报销凭证状态
            if (!"receipt_pending".equals(expense.getStatus())) {
                return Result.error("只有待提交报销凭证状态的申请才能执行此操作");
            }
            
            // 检查当前用户是否为申请人
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            if (!expense.getApplyUserId().equals(currentUser.getId())) {
                return Result.error("只有申请人才能提交报销凭证");
            }
            
            // 检查是否提供了附件
            if (attachments == null || attachments.isEmpty()) {
                return Result.error("请提供报销凭证附件");
            }
            
            // 保存附件
            for (AttachmentDTO attachment : attachments) {
                attachment.setExpenseId(id);
            }
            attachmentMapper.batchInsert(attachments, id);
            
            // 更新状态为报销凭证待审核
            expenseMapper.updateStatus(id, "receipt_audit", null, null, "已提交报销凭证，等待审核");
            
            return Result.success("报销凭证提交成功，等待审核");
        } catch (Exception e) {
            return Result.error("提交报销凭证失败: " + e.getMessage());
        }
    }

    /**
     * 管理员审核报销凭证接口
     * 
     * @param id 经费申请ID
     * @param status 审核结果（approved-通过,rejected-拒绝）
     * @param comment 审核意见
     * @return 操作结果
     */
    @PostMapping("/{id}/audit-receipt")
    public Result auditReceipt(@PathVariable Long id, 
                              @RequestParam String status,
                              @RequestParam(required = false) String comment) {
        try {
            // 获取当前经费申请
            ExpenseDTO expense = expenseMapper.selectById(id);
            if (expense == null) {
                return Result.error("经费申请不存在");
            }
            
            // 检查是否为报销凭证待审核状态
            if (!"receipt_audit".equals(expense.getStatus())) {
                return Result.error("只有报销凭证待审核状态的申请才能执行此操作");
            }
            
            // 获取当前用户信息
            String username = SecurityUtils.getCurrentUsername();
            User currentUser = userService.getUserByUsername(username);
            
            // 设置新状态
            String newStatus;
            if ("approved".equals(status)) {
                newStatus = "completed";
            } else if ("rejected".equals(status)) {
                newStatus = "repayment_pending"; // 拒绝则需要负责人自行还款
            } else {
                return Result.error("无效的审核结果");
            }
            
            // 更新状态
            expenseMapper.updateStatus(id, newStatus, currentUser.getId(), currentUser.getRealName(), comment);
            
            return Result.success("审核报销凭证成功");
        } catch (Exception e) {
            return Result.error("审核报销凭证失败: " + e.getMessage());
        }
    }
} 