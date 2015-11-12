/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web.server.reports;

import org.apache.commons.io.IOUtils;
import org.traccar.web.shared.model.Report;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class ReportRenderer {
    final HttpServletResponse response;
    final PrintWriter writer;

    public ReportRenderer(HttpServletResponse response) throws IOException {
        this.response = response;
        response.setCharacterEncoding("UTF-8");
        this.writer = response.getWriter();
    }

    public String getFilename(Report report) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        return report.getName() +
                "_" +
                dateFormat.format(report.getFromDate()) +
                "_" +
                dateFormat.format(report.getToDate()) +
                ".html";
    }

    public void start(Report report) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + getFilename(report));

        line("<!DOCTYPE html>");
        line("<html>");
        line("<head>");
        line("<title>" + report.getName() + "</title>");
        line("<meta charset=\"utf-8\">");
        // include bootstrap JS
        line("<script type=\"text/javascript\">");
        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/traccar/web/server/reports/bootstrap.min.js"),
                writer, "UTF-8");
        line("</script>");
        // include bootstrap CSS
        line("<style type=\"text/css\">");
        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/traccar/web/server/reports/bootstrap.min.css"),
                writer, "UTF-8");
        line("</style>");

        line("</head>").line("<body>").line("<div class=\"container\">");
    }

    public void end(Report report) throws IOException {
        line("</div>").line("</body>").line("</html>");
    }

    public void h1(String text) {
        line("<h1>" + text + "</h1>");
    }

    public void h2(String text) {
        line("<h2>" + text + "</h2>");
    }

    public void h3(String text) {
        line("<h3>" + text + "</h3>");
    }

    public void text(String text) {
        writer.write(text);
    }

    public void bold(String text) {
        writer.write("<strong>");
        writer.write(text);
        writer.write("</strong>");
    }

    public void panelStart() {
        line("<div class=\"panel panel-default\">");
    }

    public void panelEnd() {
        line("</div>");
    }

    public void panelHeadingStart() {
        line("<div class=\"panel-heading\">");
    }

    public void panelHeadingEnd() {
        line("</div>");
    }

    public void panelBodyStart() {
        line("<div class=\"panel-body\">");
    }

    public void panelBodyEnd() {
        line("</div>");
    }

    public void paragraphStart() {
        writer.write("<p>");
    }

    public void paragraphEnd() {
        line("</p>");
    }

    static class TableStyle {
        private boolean hover;
        private boolean condensed;

        TableStyle hover() {
            this.hover = true;
            return this;
        }

        TableStyle condensed() {
            this.condensed = true;
            return this;
        }

        @Override
        public String toString() {
            return "class=\"table" +
                    (hover ? " table-hover" : "") +
                    (condensed ? " table-condensed" : "") + "\"";
        }
    }

    public void tableStart(TableStyle style) {
        if (style == null) {
            line("<table>");
        } else {
            line("<table " + style + ">");
        }
    }

    public void tableEnd() {
        line("</table>");
    }

    public void tableHeadStart() {
        line("<thead>");
    }

    public void tableHeadEnd() {
        line("</thead>");
    }

    static class CellStyle {
        int colspan;
        int rowspan;

        CellStyle colspan(int colspan) {
            this.colspan = colspan;
            return this;
        }

        CellStyle rowspan(int rowspan) {
            this.rowspan = rowspan;
            return this;
        }

        @Override
        public String toString() {
            return (colspan == 0 ? "" : ("colspan=\"" + colspan + "\"")) +
                   (rowspan == 0 ? "" : (" rowspan=\"" + rowspan + "\""));
        }
    }

    public void tableHeadCellStart(CellStyle style) {
        if (style == null) {
            line("<th>");
        } else {
            line("<th " + style + ">");
        }
    }

    public void tableHeadCellEnd() {
        line("</th>");
    }

    public void tableBodyStart() {
        line("<tbody>");
    }

    public void tableBodyEnd() {
        line("</tbody>");
    }

    public void tableRowStart() {
        line("<tr>");
    }

    public void tableRowEnd() {
        line("</tr>");
    }

    public void tableCellStart(CellStyle style) {
        if (style == null) {
            line("<td>");
        } else {
            line("<td " + style + ">");
        }
    }

    public void tableCellEnd() {
        line("</td>");
    }

    public void link(String url, String target, String text) {
        writer.write("<a href=\"");
        writer.write(url);
        writer.write("\"");
        if (target != null) {
            writer.write(" target=\"");
            writer.write(target);
            writer.write("\"");
        }
        writer.write(">");
        writer.write(text);
        writer.write("</a>");
    }

    private ReportRenderer line(String html) {
        writer.println(html);
        return this;
    }
}
