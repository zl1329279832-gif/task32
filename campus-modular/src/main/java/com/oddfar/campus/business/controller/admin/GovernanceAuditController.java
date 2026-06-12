package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.AppealEntity;
import com.oddfar.campus.business.domain.entity.GovernanceBatchEntity;
import com.oddfar.campus.business.domain.entity.InteractionSnapshotEntity;
import com.oddfar.campus.business.domain.entity.ModerationRecordEntity;
import com.oddfar.campus.business.mapper.AppealMapper;
import com.oddfar.campus.business.service.*;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 治理审计查询
 */
@RestController
@RequestMapping("/admin/governance/audit")
@ApiResource(name = "治理审计查询", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class GovernanceAuditController {

    @Autowired
    private ModerationRecordService moderationRecordService;
    @Autowired
    private AppealMapper appealMapper;
    @Autowired
    private GovernanceBatchService governanceBatchService;
    @Autowired
    private InteractionSnapshotService snapshotService;

    /**
     * 按内容查询审核记录
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/records/content/{contentId}", name = "按内容查询审核记录")
    public R recordsByContent(@PathVariable Long contentId) {
        return R.ok(moderationRecordService.getByContentId(contentId));
    }

    /**
     * 按管理员查询审核记录
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/records/admin/{adminId}", name = "按管理员查询审核记录")
    public R recordsByAdmin(@PathVariable Long adminId) {
        return R.ok(moderationRecordService.getByAdminId(adminId));
    }

    /**
     * 按批次查询审核记录
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/records/batch/{batchId}", name = "按批次查询审核记录")
    public R recordsByBatch(@PathVariable Long batchId) {
        return R.ok(moderationRecordService.getByBatchId(batchId));
    }

    /**
     * 查询申诉历史（含快照）
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/appeals/{contentId}", name = "查询申诉历史")
    public R appealHistory(@PathVariable Long contentId) {
        List<AppealEntity> appeals = appealMapper.selectByContentId(contentId);
        InteractionSnapshotEntity snapshot = snapshotService.getLatestSnapshot(contentId);
        Map<String, Object> result = new HashMap<>();
        result.put("appeals", appeals);
        result.put("latestSnapshot", snapshot);
        return R.ok(result);
    }

    /**
     * 查询批次详情
     */
    @PreAuthorize("@ss.resourceAuth()")
    @GetMapping(value = "/batch/{batchId}", name = "查询批次详情")
    public R batchDetail(@PathVariable Long batchId) {
        GovernanceBatchEntity batch = governanceBatchService.getById(batchId);
        List<ModerationRecordEntity> records = moderationRecordService.getByBatchId(batchId);
        Map<String, Object> result = new HashMap<>();
        result.put("batch", batch);
        result.put("records", records);
        return R.ok(result);
    }
}
