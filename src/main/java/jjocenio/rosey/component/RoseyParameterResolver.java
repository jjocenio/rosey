package jjocenio.rosey.component;

import com.beust.jcommander.JCommander;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.ValueResult;
import org.springframework.shell.jcommander.JCommanderParameterResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoseyParameterResolver extends JCommanderParameterResolver {

    @Override
    public boolean supports(MethodParameter parameter) {
        return super.supports(parameter);
    }

    @Override
    public ValueResult resolve(MethodParameter methodParameter, List<String> words) {
        JCommander jCommander = createJCommander(methodParameter);
        jCommander.parse(words.toArray(new String[words.size()]));
        return new ValueResult(methodParameter, jCommander.getObjects().get(0));
    }

    private JCommander createJCommander(MethodParameter methodParameter) {
        Object pojo = BeanUtils.instantiateClass(methodParameter.getParameterType());
        JCommander jCommander = new JCommander(pojo);
        jCommander.setExpandAtSign(false);
        jCommander.setAcceptUnknownOptions(true);
        return jCommander;
    }
}
