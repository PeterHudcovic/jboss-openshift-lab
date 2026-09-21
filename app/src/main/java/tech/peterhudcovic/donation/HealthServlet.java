package tech.peterhudcovic.donation;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().print("OK");
    }
}
