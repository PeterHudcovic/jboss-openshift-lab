package tech.peterhudcovic.donation;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(name = "DonateServlet", urlPatterns = "/donate")
public class DonateServlet extends HttpServlet {

    @Resource(lookup = "java:/jdbc/DonationDS")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/views/donate.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        final BigDecimal amount;

        try {
            amount = DonationAmount.validate(
                    request.getParameter("preset"),
                    request.getParameter("custom")
            );
        } catch (IllegalArgumentException exception) {
            request.setAttribute(
                    "error",
                    "Choose a preset or enter an amount from EUR 0.01 to EUR 10,000.00 with up to two decimal places."
            );
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/WEB-INF/views/donate.jsp")
                    .forward(request, response);
            return;
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                "INSERT INTO donations (amount) VALUES (?)"
                        )
        ) {
            statement.setBigDecimal(1, amount);
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new ServletException(
                    "Unable to store donation in PostgreSQL.",
                    exception
            );
        }

        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader(
                "Location",
                request.getContextPath() + "/success"
        );
    }
}