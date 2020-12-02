package jjocenio.rosey.service.text;

import freemarker.template.Template;
import jjocenio.rosey.component.TemplateHelper;
import jjocenio.rosey.persistence.Row;
import jjocenio.rosey.service.AbstractProcessService;
import jjocenio.rosey.service.ProcessContext;
import jjocenio.rosey.service.RowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class TextProcessService extends AbstractProcessService {

    private final TemplateHelper templateHelper;

    @Autowired
    public TextProcessService(RowService rowService, ExecutorService executorService, TemplateHelper templateHelper) {
        super(rowService, executorService);
        this.templateHelper = templateHelper;
    }

    @Override
    protected Row process(Row row, ProcessContext context) {
        try {
            TextProcessContext textProcessContext = (TextProcessContext) context;
            updateRowStatus(row, Row.Status.PROCESSING, null);

            Template compiledOutputTemplate = templateHelper.getCompiledTemplate(textProcessContext::getCompiledOutputTemplate,
                    () -> textProcessContext.getTextConfig().getOutputTemplate(), textProcessContext::setCompiledOutputTemplate);

            String output = templateHelper.merge(compiledOutputTemplate, getParams(textProcessContext, row));
            updateRowStatus(row, Row.Status.PROCESSED, null, output);
        } catch (Exception e) {
            updateRowStatus(row, Row.Status.FAILED, e.getMessage());
        }

        return row;
    }
}
