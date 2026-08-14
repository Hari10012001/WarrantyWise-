package com.warrantywise.service;

import com.warrantywise.dto.report.ProductLifecycleReportResponse;
import com.warrantywise.dto.report.ProductReportResponse;
import com.warrantywise.dto.report.ReportFilterRequest;
import com.warrantywise.dto.report.ServiceReportResponse;
import com.warrantywise.dto.report.WarrantyReportResponse;
import com.warrantywise.security.UserPrincipal;

import java.util.List;

public interface ReportService {
    List<ProductReportResponse> getProductReport(ReportFilterRequest filter, UserPrincipal currentUser);
    String exportProductReportCsv(ReportFilterRequest filter, UserPrincipal currentUser);
    
    List<WarrantyReportResponse> getWarrantyReport(ReportFilterRequest filter, UserPrincipal currentUser);
    String exportWarrantyReportCsv(ReportFilterRequest filter, UserPrincipal currentUser);
    
    List<ServiceReportResponse> getServiceReport(ReportFilterRequest filter, UserPrincipal currentUser);
    String exportServiceReportCsv(ReportFilterRequest filter, UserPrincipal currentUser);
    
    List<ProductLifecycleReportResponse> getProductLifecycleReport(ReportFilterRequest filter, UserPrincipal currentUser);
    String exportProductLifecycleReportCsv(ReportFilterRequest filter, UserPrincipal currentUser);
}
