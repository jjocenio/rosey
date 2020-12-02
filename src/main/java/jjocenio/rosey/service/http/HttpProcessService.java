package jjocenio.rosey.service.http;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.AbstractProcessService;
import jjocenio.rosey.service.ProcessContext;
import jjocenio.rosey.service.RowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;

@Component
@Qualifier("http")
public class HttpProcessService extends AbstractProcessService {

    private final TemplateHelper templateHelper;
    private final RestTemplate restTemplate;

    @Autowired
    public HttpProcessService(RowService rowService, ExecutorService executorService,
                              RestTemplate restTemplate, TemplateHelper templateHelper) {

        super(rowService, executorService);
        this.restTemplate = restTemplate;
        this.templateHelper = templateHelper;
    }

    @Override
    protected Row process(Row row, ProcessContext context) {
        try {
            HttpProcessContext httpProcessContext = (HttpProcessContext) context;
            updateRowStatus(row, Row.Status.PROCESSING, null);
            String url = getUrl((HttpProcessContext) context, row);

            RequestEntity.HeadersBuilder requestEntityBuilder = getHeadersBuilder(httpProcessContext, url);
            requestEntityBuilder = applyHeaders(row, httpProcessContext, requestEntityBuilder);
            requestEntityBuilder = applyBody(row, httpProcessContext, requestEntityBuilder);

            ResponseEntity<String> response = restTemplate.exchange(requestEntityBuilder.build(), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                updateRowStatus(row, Row.Status.PROCESSED, null, response.getBody());
            } else {
                updateRowStatus(row, Row.Status.FAILED, "Http Error: " + response.getStatusCodeValue(), response.getBody());
            }
        } catch (Exception e) {
            updateRowStatus(row, Row.Status.FAILED, e.getMessage());
        }

        return row;
    }

    private RequestEntity.HeadersBuilder applyBody(Row row, HttpProcessContext httpProcessContext, RequestEntity.HeadersBuilder requestEntityBuilder) throws IOException, TemplateException {
        if (httpProcessContext.getHttpConfig().getBody() != null) {
            String body = getBody(httpProcessContext, row);
            requestEntityBuilder = (RequestEntity.HeadersBuilder) ((RequestEntity.BodyBuilder) requestEntityBuilder).body(body, String.class);
        }
        return requestEntityBuilder;
    }

    private RequestEntity.HeadersBuilder applyHeaders(Row row, HttpProcessContext httpProcessContext, RequestEntity.HeadersBuilder requestEntityBuilder) throws IOException, TemplateException {
        if (httpProcessContext.getHttpConfig().getHeaders() != null) {
            String headers = getHeaders(httpProcessContext, row);
            for (String header: headers.split("|")) {
                String[] headerKeyValue = header.split(":");
                requestEntityBuilder = requestEntityBuilder.header(headerKeyValue[0], headerKeyValue[1]);
            }
        }
        return requestEntityBuilder;
    }

    private RequestEntity.HeadersBuilder getHeadersBuilder(HttpProcessContext httpProcessContext, String url) throws URISyntaxException {
        RequestEntity.HeadersBuilder requestEntityBuilder = null;
        switch (httpProcessContext.getHttpConfig().getHttpMethod()) {
            case POST:
                requestEntityBuilder = RequestEntity.post(new URI(url));
                break;
            case PUT:
                requestEntityBuilder = RequestEntity.put(new URI(url));
                break;
            case GET:
                requestEntityBuilder = RequestEntity.get(new URI(url));
                break;
            case DELETE:
                requestEntityBuilder = RequestEntity.delete(new URI(url));
                break;
            default:
                throw new IllegalArgumentException("Invalid Http Method");
        }
        return requestEntityBuilder;
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
