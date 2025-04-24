package com.vocational.researchfund.controller;

import com.vocational.researchfund.common.Result;
import com.vocational.researchfund.dto.FundTransferDTO;
import com.vocational.researchfund.dto.PageDTO;
import com.vocational.researchfund.service.FundTransferService;
import com.vocational.researchfund.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 经费结转控制器
 */
@RestController
@RequestMapping("/api/transfers")
public class FundTransferController {

    @Autowired
    private FundTransferService fundTransferService;

    /**
     * 获取经费结转列表（分页）
     * 
     * @param title     结转标题
     * @param projectId 所属项目ID
     * @param status    状态
     * @param fromYear  结转年度（从）
     * @param toYear    结转年度（到）
     * @param pageNum   页码，默认1
     * @param pageSize  每页大小，默认10
     * @return 分页后的经费结转列表
     */
    @GetMapping
    public ResponseEntity<Result<PageDTO<FundTransferDTO>>> getTransferList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromYear,
            @RequestParam(required = false) String toYear,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> params = new HashMap<>();
        if (title != null)
            params.put("title", title);
        if (projectId != null)
            params.put("projectId", projectId);
        if (status != null)
            params.put("status", status);
        if (fromYear != null)
            params.put("fromYear", fromYear);
        if (toYear != null)
            params.put("toYear", toYear);

        PageDTO<FundTransferDTO> pagedTransfers = fundTransferService.getAllTransfers(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedTransfers));
    }

    /**
     * 获取当前用户的经费结转列表（分页）
     * 
     * @param title     结转标题
     * @param projectId 所属项目ID
     * @param status    状态
     * @param fromYear  结转年度（从）
     * @param toYear    结转年度（到）
     * @param pageNum   页码，默认1
     * @param pageSize  每页大小，默认10
     * @return 分页后的经费结转列表
     */
    @GetMapping("/my")
    public ResponseEntity<Result<PageDTO<FundTransferDTO>>> getMyTransfers(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromYear,
            @RequestParam(required = false) String toYear,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Map<String, Object> params = new HashMap<>();
        if (title != null && !title.isEmpty())
            params.put("title", title);
        if (projectId != null && !projectId.isEmpty())
            params.put("projectId", projectId);
        if (status != null && !status.isEmpty())
            params.put("status", status);
        if (fromYear != null && !fromYear.isEmpty())
            params.put("fromYear", fromYear);
        if (toYear != null && !toYear.isEmpty())
            params.put("toYear", toYear);

        PageDTO<FundTransferDTO> pagedTransfers = fundTransferService.getCurrentUserTransfers(params, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(pagedTransfers));
    }

    /**
     * 获取经费结转详情
     * 
     * @param id 经费结转ID
     * @return 经费结转详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<FundTransferDTO>> getTransferDetail(@PathVariable Long id) {
        FundTransferDTO transfer = fundTransferService.getTransferById(id);
        if (transfer == null) {
            return ResponseEntity.badRequest().body(Result.error("经费结转不存在"));
        }
        return ResponseEntity.ok(Result.success(transfer));
    }

    /**
     * 创建经费结转
     * 
     * @param transferDTO 经费结转DTO
     * @return 创建后的经费结转
     */
    @PostMapping
    public ResponseEntity<Result<FundTransferDTO>> createTransfer(@RequestBody FundTransferDTO transferDTO) {
        try {
            FundTransferDTO createdTransfer = fundTransferService.createTransfer(transferDTO);
            return ResponseEntity.ok(Result.success(createdTransfer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新经费结转
     * 
     * @param id          经费结转ID
     * @param transferDTO 经费结转DTO
     * @return 更新后的经费结转
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<FundTransferDTO>> updateTransfer(@PathVariable Long id, @RequestBody FundTransferDTO transferDTO) {
        try {
            FundTransferDTO updatedTransfer = fundTransferService.updateTransfer(id, transferDTO);
            return ResponseEntity.ok(Result.success(updatedTransfer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 审核经费结转
     * 
     * @param id      经费结转ID
     * @param status  审核状态：approved-已通过,rejected-已拒绝
     * @param comment 审核意见
     * @return 审核后的经费结转
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<Result<FundTransferDTO>> auditTransfer(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comment) {
        try {
            // 审核状态校验
            if (!"approved".equals(status) && !"rejected".equals(status)) {
                return ResponseEntity.badRequest().body(Result.error("审核状态参数错误"));
            }

            FundTransferDTO auditedTransfer = fundTransferService.auditTransfer(id, status, comment);
            return ResponseEntity.ok(Result.success(auditedTransfer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 删除经费结转
     * 
     * @param id 经费结转ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteTransfer(@PathVariable Long id) {
        try {
            // 获取当前经费结转
            FundTransferDTO transfer = fundTransferService.getTransferById(id);
            if (transfer == null) {
                return ResponseEntity.badRequest().body(Result.error("经费结转不存在"));
            }


            fundTransferService.deleteTransfer(id);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
} 