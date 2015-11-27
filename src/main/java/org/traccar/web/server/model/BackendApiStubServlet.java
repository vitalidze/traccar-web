package org.traccar.web.server.model;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class BackendApiStubServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo() != null && req.getPathInfo().startsWith("/command")) {
            handleCommand(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_FOUND);
        }
    }

    void handleCommand(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String line;
        while ((line = req.getReader().readLine()) != null) {
            resp.getWriter().println(line);
        }
    }
}
