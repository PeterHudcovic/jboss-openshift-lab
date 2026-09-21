<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ include file="/WEB-INF/views/header.jspf" %>
<p class="eyebrow">A small step</p>
<h1>Choose a donation amount</h1>
<p>This is a demonstration. Continue validates your selection without taking a payment.</p>
<p class="error" role="alert">${requestScope.error}</p>
<form method="post" action="${pageContext.request.contextPath}/donate">
    <fieldset>
        <legend>Choose an amount in EUR</legend>
        <div class="amounts">
            <label class="amount"><input type="radio" name="preset" value="5"> <span>&euro;5</span></label>
            <label class="amount"><input type="radio" name="preset" value="10" checked> <span>&euro;10</span></label>
            <label class="amount"><input type="radio" name="preset" value="25"> <span>&euro;25</span></label>
        </div>
    </fieldset>
    <label class="input-label" for="custom">Or enter a custom amount (EUR)</label>
    <input id="custom" name="custom" type="number" min="0.01" max="10000" step="0.01"
           inputmode="decimal" placeholder="e.g. 15.00" aria-describedby="amount-help">
    <p class="hint" id="amount-help">Optional. A custom amount overrides the selected preset. Maximum &euro;10,000.</p>
    <div class="actions">
        <button class="button" type="submit">Continue</button>
        <a href="${pageContext.request.contextPath}/cancel">Cancel</a>
    </div>
</form>
<%@ include file="/WEB-INF/views/footer.jspf" %>
