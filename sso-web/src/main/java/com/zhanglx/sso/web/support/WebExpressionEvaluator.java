package com.zhanglx.sso.web.support;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebExpressionEvaluator {

    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public boolean matchesCondition(String condition, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (!StringUtils.hasText(condition)) {
            return true;
        }
        Object value = evaluate(condition, joinPoint, request);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    public String evaluateAsString(String expression, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        Object value = evaluate(expression, joinPoint, request);
        if (value == null) {
            return null;
        }
        String normalized = String.valueOf(value).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Object evaluate(String expression, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        StandardEvaluationContext context = new StandardEvaluationContext(joinPoint.getTarget());
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        context.setVariable("args", args);
        context.setVariable("method", method);
        context.setVariable("request", request);
        Expression parsedExpression = expressionCache.computeIfAbsent(expression, expressionParser::parseExpression);
        return parsedExpression.getValue(context);
    }
}
