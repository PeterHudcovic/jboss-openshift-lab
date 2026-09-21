package tech.peterhudcovic.donation;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DonateServlet", urlPatterns = "/donate")
public class DonateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/donate.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            DonationAmount.validate(request.getParameter("preset"), request.getParameter("custom"));
        } catch (IllegalArgumentException exception) {
            // Only a fixed application message is rendered, never submitted input.
            request.setAttribute("error", "Choose a preset or enter an amount from EUR 0.01 to EUR 10,000.00 with up to two decimal places.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/WEB-INF/views/donate.jsp").forward(request, response);
            return;
        }

        // Demo only: no payment provider, database, session, or donation record.
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", request.getContextPath() + "/success");
    }
}
