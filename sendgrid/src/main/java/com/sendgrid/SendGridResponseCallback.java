package com.sendgrid;

public interface SendGridResponseCallback {
    void onMailSendSuccess(SendGrid.Response response);

    void onMailSendFailed(SendGrid.Response response);
}
