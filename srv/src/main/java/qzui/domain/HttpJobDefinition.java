package qzui.domain;

import com.github.kevinsawicki.http.HttpRequest;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;
import restx.http.HttpStatus;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.quartz.JobBuilder.newJob;

@Component
public class HttpJobDefinition extends AbstractJobDefinition {
    private static final Logger logger = LoggerFactory.getLogger(HttpJobDefinition.class);

    @Override
    public boolean acceptJobClass(Class<? extends Job> jobClass) {
        return jobClass.getName().equals(HttpJob.class.getName());
    }

    @Override
    public JobDescriptor buildDescriptor(JobDetail jobDetail, List<? extends Trigger> triggersOfJob) {
        HttpJobDescriptor jobDescriptor = setupDescriptorFromDetail(new HttpJobDescriptor(), jobDetail, triggersOfJob);

        return jobDescriptor
                .setUrl((String) jobDescriptor.getData().remove("url"))
                .setMethod((String) jobDescriptor.getData().remove("method"))
                .setBody((String) jobDescriptor.getData().remove("body"));
    }

    public static class HttpJobDescriptor extends JobDescriptor {

        private String url;
        private String method = "POST";
        private String body;
        private String contentType;
        private String login;
        private String pwdHash;

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public String getBody() {
            return body;
        }

        public String getContentType() {
            return contentType;
        }

        public String getLogin() {
            return login;
        }

        public String getPwdHash() {
            return pwdHash;
        }

        public HttpJobDescriptor setBody(final String body) {
            this.body = body;
            return this;
        }

        public HttpJobDescriptor setMethod(final String method) {
            this.method = method;
            return this;
        }

        public HttpJobDescriptor setUrl(final String url) {
            this.url = url;
            return this;
        }

        public HttpJobDescriptor setContentType(final String contentType) {
            this.contentType = contentType;
            return this;
        }

        public HttpJobDescriptor setPwdHash(final String pwdHash) {
            this.pwdHash = pwdHash;
            return this;
        }

        public HttpJobDescriptor setLogin(final String login) {
            this.login = login;
            return this;
        }

        @Override
        public JobDetail buildJobDetail() {
            JobDataMap dataMap = new JobDataMap(getData());
            dataMap.put("url", url);
            dataMap.put("method", method);
            dataMap.put("body", body);
            dataMap.put("contentType", contentType);
            dataMap.put("login", login);
            dataMap.put("pwd", pwdHash);
            return newJob(HttpJob.class)
                    .withIdentity(getName(), getGroup())
                    .usingJobData(dataMap)
                    .build();
        }

        @Override
        public String toString() {
            return "HttpJobDescriptor{" +
                    "url='" + url + '\'' +
                    ", method='" + method + '\'' +
                    ", body='" + body + '\'' +
                    ", contentType='" + contentType + '\'' +
                    '}';
        }

    }

    public static class HttpJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {

            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            String url = jobDataMap.getString("url");
            String method = jobDataMap.getString("method");
            HttpRequest request = new HttpRequest(url, method);

            String body = "";
            if (!isNullOrEmpty(jobDataMap.getString("body"))) {
                body = jobDataMap.getString("body");
            }

            setContentType(jobDataMap, request);
            setCrendentials(jobDataMap, request);

            try {
                request.send(body);
                int code = request.code();
                if (code == HttpStatus.OK.getCode()) {
                    logger.info("[SUCCESS] {} {} {} => {}", method, url, body, code);
                } else {
                    logger.error("[FAILED] {} {} => {}", method, url, code);
                }

            } catch (HttpRequest.HttpRequestException e) {
                //e.printStackTrace();
                logger.error("[FAILED] {} {} => E", method, url);
            }
        }

        private void setCrendentials(JobDataMap jobDataMap, HttpRequest request) {
            String login = jobDataMap.getString("login");
            String pwd = jobDataMap.getString("pwd");
            if (!isNullOrEmpty(login) && !isNullOrEmpty(pwd)) {
                request.basic(login, pwd);
            }
        }

        private void setContentType(JobDataMap jobDataMap, HttpRequest request) {
            String contentType = jobDataMap.getString("contentType");
            if (!isNullOrEmpty(contentType)) {
                request.contentType(contentType);
            }
        }
    }

}
