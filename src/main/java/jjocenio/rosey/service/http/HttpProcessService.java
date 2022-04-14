package jjocenio.rosey.service.http;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import jjocenio.rosey.component.ExecutorServiceProvider;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.AbstractProcessService;
import jjocenio.rosey.service.ProcessContext;
import jjocenio.rosey.service.RowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Component
@Qualifier("http")
public class HttpProcessService extends AbstractProcessService {

    private final TemplateHelper templateHelper;
    private final RestTemplate restTemplate;

    @Autowired
    public HttpProcessService(RowService rowService, ExecutorServiceProvider executorServiceProvider,
                              RestTemplate restTemplate, TemplateHelper templateHelper) {

        super(rowService, executorServiceProvider);
        this.restTemplate = restTemplate;
        this.templateHelper = templateHelper;
    }

    @Override
    @SuppressWarnings("java:S2142")
    protected Row process(Row row, ProcessContext context) {
        HttpProcessContext httpProcessContext = (HttpProcessContext) context;

        try {
            updateRowStatus(row, Row.Status.PROCESSING, null);
            String url = getUrl((HttpProcessContext) context, row);

            HttpHeaders headers = applyHeaders(row, httpProcessContext);
            String body = applyBody(row, httpProcessContext);

            RequestEntity<String> requestEntity = createRequestEntity(httpProcessContext, url, headers, body);

            ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                updateRowStatus(row, Row.Status.PROCESSED, null, response.getBody());
            } else {
                updateRowStatus(row, Row.Status.FAILED, "Http Error: " + response.getStatusCodeValue(), response.getBody());
            }
        } catch (Exception e) {
            updateRowStatus(row, Row.Status.FAILED, e);
        }

        if (httpProcessContext.getHttpConfig().getWait() > 0L) {
            try {
                TimeUnit.SECONDS.sleep(httpProcessContext.getHttpConfig().getWait());
            } catch (InterruptedException ignored) {
            }
        }

        return row;
    }

    private String applyBody(Row row, HttpProcessContext httpProcessContext) throws IOException, TemplateException {
        String body = null;
        if (httpProcessContext.getHttpConfig().getBody() != null) {
            body = getBody(httpProcessContext, row);
        }
        return body;
    }

    private HttpHeaders applyHeaders(Row row, HttpProcessContext httpProcessContext) throws IOException, TemplateException {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (httpProcessContext.getHttpConfig().getHeaders() != null) {
            String headers = getHeaders(httpProcessContext, row);
            for (String header: headers.split("\\|")) {
                String[] headerKeyValue = header.split(":");
                httpHeaders.add(headerKeyValue[0], headerKeyValue[1]);
            }
        }
        return httpHeaders;
    }

    private RequestEntity<String> createRequestEntity(HttpProcessContext httpProcessContext, String url, HttpHeaders headers, String body) throws URISyntaxException {
        HttpMethod httpMethod = HttpMethod.resolve(httpProcessContext.getHttpConfig().getHttpMethod().name());
        return new RequestEntity<>(body, headers, httpMethod, new URI(url));
    }

    private String getUrl(HttpProcessContext context, Row row) throws IOException, TemplateException {
        Template urlCompiledTemplate = templateHelper.getCompiledTemplate(context::getCompiledUrlTemplate, () -> context.getHttpConfig().getUrl(), context::setCompiledUrlTemplate);
        return templateHelper.merge(urlCompiledTemplate, getParams(context, row));
    }

    private String getBody(HttpProcessContext context, Row row) throws IOException, TemplateException {
        Template bodyTemplate = templateHelper.getCompiledTemplate(context::getCompiledBodyTemplate, () -> context.getHttpConfig().getBody(), context::setCompiledBodyTemplate);
        return templateHelper.merge(bodyTemplate, getParams(context, row));
    }

    private String getHeaders(HttpProcessContext context, Row row) throws IOException, TemplateException {
        Template headersTemplate = templateHelper.getCompiledTemplate(context::getCompiledHeaderTemplate, () -> context.getHttpConfig().getHeaders(), context::setCompiledHeaderTemplate);
        return templateHelper.merge(headersTemplate, getParams(context, row));
    }
}
