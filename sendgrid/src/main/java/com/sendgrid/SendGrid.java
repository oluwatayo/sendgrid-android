package com.sendgrid;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sendgrid.smtpapi.SMTPAPI;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendGrid {
    private SendGridResponseCallback sendGridResponseCallback;
    private static final String VERSION = "2.2.2";
    private static final String USER_AGENT = "sendgrid/" + VERSION + ";java";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    private String username;
    private String password;
    private String url;
    private String port;
    private String endpoint;
    private OkHttpClient okHttpClient;

    /**
     * Constructor for using a username and password
     *
     * @param username SendGrid username
     * @param password SendGrid password
     */
    /*public SendGrid(String username, String password) {
        this.username = username;
        this.password = password;
        this.url = "https://api.sendgrid.com/v3/mail/send";
        this.endpoint = "";
        OkHttpClient.Builder builder;
        builder = new OkHttpClient.Builder();
        builder.readTimeout(15, TimeUnit.SECONDS);
        builder.connectTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(15, TimeUnit.SECONDS);
        builder.networkInterceptors().add(new UserAgentInterceptor(USER_AGENT));
        okHttpClient = builder.build();
    }*/

    /**
     * Constructor for using an API key
     *
     * @param apiKey SendGrid api key
     */
    public SendGrid(String apiKey) {
        this.password = apiKey;
        this.username = null;
        this.url = "https://api.sendgrid.com/v3/mail/send";
        this.endpoint = "";
        OkHttpClient.Builder builder;
        builder = new OkHttpClient.Builder();
        builder.readTimeout(15, TimeUnit.SECONDS);
        builder.connectTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(15, TimeUnit.SECONDS);
        builder.networkInterceptors().add(new UserAgentInterceptor(USER_AGENT));
        okHttpClient = builder.build();
    }

    public void setSendgridResponseCallbacks(SendGridResponseCallback callbacks) {
        this.sendGridResponseCallback = callbacks;
    }

    public SendGrid setUrl(String url) {
        this.url = url;
        return this;
    }

    public SendGrid setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getVersion() {
        return VERSION;
    }


    public String buildBody(Email email) throws JSONException {
        JSONObject rootObject = new JSONObject();
        JSONArray personalizationsArray = new JSONArray();
        JSONObject personalizationsObject = new JSONObject();
        JSONArray tosArray = new JSONArray();
        JSONArray ccsArray = new JSONArray();
        JSONArray bccsArray = new JSONArray();
        for (int i = 0; i < email.getTos().length; i++) {
            JSONObject tosObject = new JSONObject();
            tosObject.put("email", email.getTos()[i]);
            try {
                tosObject.put("name", email.getToNames()[i]);
            } catch (IndexOutOfBoundsException e) {
                sendGridResponseCallback.onMailSendFailed(new SendGrid.Response(500, "to's names lenght is less that to'e email length"));
            }
            tosArray.put(tosObject);
        }

        personalizationsObject.put("subject", email.getSubject());
        personalizationsObject.put("to", tosArray);
        if (email.getCcs().length > 0) {
            for (int i = 0; i < email.getCcs().length; i++) {
                JSONObject ccsObject = new JSONObject();
                ccsObject.put("email", email.getCcs()[i]);
                ccsArray.put(ccsObject);
            }
            personalizationsObject.put("cc", ccsArray);
        }
        if (email.getBccs().length > 0) {
            for (int i = 0; i < email.getBccs().length; i++) {
                JSONObject bccsObject = new JSONObject();
                bccsObject.put("email", email.getBccs()[i]);
                bccsArray.put(bccsObject);
            }
            personalizationsObject.put("bcc", bccsArray);
        }
        personalizationsArray.put(personalizationsObject);
        rootObject.put("personalizations", personalizationsArray);
        JSONObject fromObject = new JSONObject();
        fromObject.put("email", email.getFrom());
        fromObject.put("name", email.getFromName());
        rootObject.put("from", fromObject);
        JSONObject replyToObject = new JSONObject();
        replyToObject.put("email", email.getReplyTo());
        replyToObject.put("name", email.getReplyToName());
        rootObject.put("reply_to", replyToObject);
        JSONArray contentArray = new JSONArray();
        for (int i = 0; i < 2; i++) {
            JSONObject contentObject = new JSONObject();
            if (i == 0) {
                contentObject.put("type", "text/plain");
                contentObject.put("value", email.getText());
            } else {
                contentObject.put("type", "text/html");
                contentObject.put("value", email.getHtml());
            }

            contentArray.put(contentObject);
        }

        rootObject.put("content", contentArray);


        /*String mailBody = "{\n" +
                "  \"personalizations\": [\n" +
                "    {\n" +
                "      \"to\": [\n" +
                "        {\n" +
                "          \"email\": \"" + email.getTos()[0] + "\",\n" +
                "          \"name\": \"" + email.getToNames()[0] + "\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"subject\": \"" + email.getSubject() + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"from\": {\n" +
                "    \"email\": \"" + email.getFrom() + "\",\n" +
                "    \"name\": \"" + email.getFromName() + "\"\n" +
                "  },\n" +
                "  \"reply_to\": {\n" +
                "    \"email\": \"" + email.getReplyTo() + "\",\n" +
                "    \"name\": \"" + email.getReplyToName() + "\"\n" +
                "  },\n" +
                "  \"content\": [\n" +
                "    {\n" +
                "      \"type\": \"text/plain\",\n" +
                "      \"value\": \"" + email.getText() + "\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"text/html\",\n" +
                "      \"value\": \"" + email.getHtml() + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";*/
        return rootObject.toString();
    }

    public void getResponse(String body) {
        Request.Builder request = new Request.Builder();
        final Handler handler = new Handler(Looper.getMainLooper());
        request.header("Authorization", "Bearer " + this.password);
        if (body != null && !TextUtils.isEmpty(body))
            request.post(RequestBody.create(JSON, body));
        request.url(url);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> sendGridResponseCallback.onMailSendFailed(new Response(500, e.getLocalizedMessage())));
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                handler.post(() -> {
                    try {
                        String res = response.body().string();
                        Log.e("LOG", "response" + res);
                        SendGrid.Response sendgridResponse = new Response(response.code(), res);
                        if (String.valueOf(sendgridResponse.code).startsWith("2"))
                            sendGridResponseCallback.onMailSendSuccess(sendgridResponse);
                        else
                            sendGridResponseCallback.onMailSendFailed(sendgridResponse);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }

    public void send(Email email) throws SendGridException {
        if (email.getHtml() == null) {
            email.setHtml("");
        }
        try {
            getResponse(buildBody(email));
        } catch (JSONException e) {
            throw new SendGridException(e.getLocalizedMessage());
        }
    }

    public class UserAgentInterceptor implements Interceptor {

        private final String userAgent;

        public UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", userAgent)
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }

    public static class Email {
        private SMTPAPI smtpapi;
        private ArrayList<String> to;
        private ArrayList<String> toname;
        private ArrayList<String> cc;
        private String from;
        private String fromname;
        private String replyto;
        private String replyToName;
        private String subject;
        private String text;
        private String html;
        private ArrayList<String> bcc;
        private Map<String, InputStream> attachments;
        private Map<String, String> contents;
        private Map<String, String> headers;

        public Email() {
            this.smtpapi = new SMTPAPI();
            this.to = new ArrayList<String>();
            this.toname = new ArrayList<String>();
            this.cc = new ArrayList<String>();
            this.bcc = new ArrayList<String>();
            this.attachments = new HashMap<String, InputStream>();
            this.contents = new HashMap<String, String>();
            this.headers = new HashMap<String, String>();
        }

        /*public Email addTo(String to) {
            this.to.add(to);
            return this;
        }*/

        public String getReplyToName() {
            return replyToName;
        }

        public Email addTo(String[] tos) {
            this.to.addAll(Arrays.asList(tos));
            return this;
        }

        public Email addTo(String to, String name) {
            this.to.add(to);
            this.toname.add(name);
            return this;
        }

        public Email setTo(String[] tos) {
            this.to = new ArrayList<String>(Arrays.asList(tos));
            return this;
        }

        public String[] getTos() {
            return this.to.toArray(new String[this.to.size()]);
        }

        public Email addSmtpApiTo(String to) throws JSONException {
            this.smtpapi.addTo(to);
            return this;
        }

        public Email addSmtpApiTo(String[] to) throws JSONException {
            this.smtpapi.addTos(to);
            return this;
        }

        /*public Email addToName(String toname) {
            this.toname.add(toname);
            return this;
        }*/

        public Email addToName(String[] tonames) {
            this.toname.addAll(Arrays.asList(tonames));
            return this;
        }

        public Email setToName(String[] tonames) {
            this.toname = new ArrayList<String>(Arrays.asList(tonames));
            return this;
        }

        public String[] getToNames() {
            return this.toname.toArray(new String[this.toname.size()]);
        }

        public Email addCc(String cc) {
            this.cc.add(cc);
            return this;
        }

        public Email addCc(String[] ccs) {
            this.cc.addAll(Arrays.asList(ccs));
            return this;
        }

        public Email setCc(String[] ccs) {
            this.cc = new ArrayList<String>(Arrays.asList(ccs));
            return this;
        }

        public String[] getCcs() {
            return this.cc.toArray(new String[this.cc.size()]);
        }

        public Email setFrom(String from, String fromname) {
            this.from = from;
            this.fromname = fromname;
            return this;
        }

        public String getFrom() {
            return this.from;
        }

        public Email setFromName(String fromname) {
            this.fromname = fromname;
            return this;
        }

        public String getFromName() {
            return this.fromname;
        }

        public Email setReplyTo(String replyto, String replyToName) {
            this.replyto = replyto;
            this.replyToName = replyToName;
            return this;
        }

        public String getReplyTo() {
            return this.replyto;
        }

        public Email addBcc(String bcc) {
            this.bcc.add(bcc);
            return this;
        }

        public Email addBcc(String[] bccs) {
            this.bcc.addAll(Arrays.asList(bccs));
            return this;
        }

        public Email setBcc(String[] bccs) {
            this.bcc = new ArrayList<String>(Arrays.asList(bccs));
            return this;
        }

        public String[] getBccs() {
            return this.bcc.toArray(new String[this.bcc.size()]);
        }

        public Email setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public String getSubject() {
            return this.subject;
        }

        public Email setText(String text) {
            this.text = text;
            return this;
        }

        public String getText() {
            return this.text;
        }

        public Email setHtml(String html) {
            this.html = html;
            return this;
        }

        public String getHtml() {
            return this.html;
        }

        /*public Email addSubstitution(String key, String[] val) throws JSONException {
            this.smtpapi.addSubstitutions(key, val);
            return this;
        }

        public JSONObject getSubstitutions() throws JSONException {
            return this.smtpapi.getSubstitutions();
        }

        public Email addUniqueArg(String key, String val) throws JSONException {
            this.smtpapi.addUniqueArg(key, val);
            return this;
        }

        public JSONObject getUniqueArgs() throws JSONException {
            return this.smtpapi.getUniqueArgs();
        }

        public Email addCategory(String category) throws JSONException {
            this.smtpapi.addCategory(category);
            return this;
        }

        public String[] getCategories() throws JSONException {
            return this.smtpapi.getCategories();
        }

        public Email addSection(String key, String val) throws JSONException {
            this.smtpapi.addSection(key, val);
            return this;
        }

        public JSONObject getSections() throws JSONException {
            return this.smtpapi.getSections();
        }

        public Email addFilter(String filter_name, String parameter_name, String parameter_value) throws JSONException {
            this.smtpapi.addFilter(filter_name, parameter_name, parameter_value);
            return this;
        }

        public JSONObject getFilters() throws JSONException {
            return this.smtpapi.getFilters();
        }

        public Email setASMGroupId(int val) throws JSONException {
            this.smtpapi.setASMGroupId(val);
            return this;
        }

        public Integer getASMGroupId() throws JSONException {
            return this.smtpapi.getASMGroupId();
        }

        public Email setSendAt(int sendAt) throws JSONException {
            this.smtpapi.setSendAt(sendAt);
            return this;
        }

        public int getSendAt() throws JSONException {
            return this.smtpapi.getSendAt();
        }*/

        /**
         * Convenience method to set the template
         *
         * @param templateId The ID string of your template
         * @return this
         */
        /*public Email setTemplateId(String templateId) throws JSONException {
            this.getSMTPAPI().addFilter("templates", "enable", 1);
            this.getSMTPAPI().addFilter("templates", "template_id", templateId);
            return this;
        }

        public Email addAttachment(String name, File file) throws IOException, FileNotFoundException {
            return this.addAttachment(name, new FileInputStream(file));
        }

        public Email addAttachment(String name, String file) throws IOException {
            return this.addAttachment(name, new ByteArrayInputStream(file.getBytes()));
        }

        public Email addAttachment(String name, InputStream file) throws IOException {
            this.attachments.put(name, file);
            return this;
        }

        public Map getAttachments() {
            return this.attachments;
        }

        public Email addContentId(String attachmentName, String cid) {
            this.contents.put(attachmentName, cid);
            return this;
        }

        public Map getContentIds() {
            return this.contents;
        }

        public Email addHeader(String key, String val) {
            this.headers.put(key, val);
            return this;
        }

        public Map getHeaders() {
            return this.headers;
        }

        public SMTPAPI getSMTPAPI() {
            return this.smtpapi;
        }*/
    }

    public static class Response {
        private int code;
        private boolean success;
        private String message;

        public Response(int code, String msg) {
            this.code = code;
            this.success = String.valueOf(code).startsWith("2");
            this.message = msg;
        }

        public int getCode() {
            return this.code;
        }

        public boolean getStatus() {
            return this.success;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
