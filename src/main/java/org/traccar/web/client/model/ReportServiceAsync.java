package org.traccar.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.traccar.web.shared.model.Report;

import java.util.List;

public interface ReportServiceAsync {
    void getReports(AsyncCallback<List<Report>> async);

    void addReport(Report report, AsyncCallback<Report> async);

    void updateReport(Report report, AsyncCallback<Report> async);

    void removeReport(Report report, AsyncCallback<Void> async);
}
