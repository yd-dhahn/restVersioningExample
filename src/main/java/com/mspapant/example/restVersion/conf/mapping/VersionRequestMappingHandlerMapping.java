package com.mspapant.example.restVersion.conf.mapping;

import com.mspapant.example.restVersion.conf.annotation.Versionable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class VersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private List<Context> contextList = new ArrayList<Context>();

    protected class Context {

        private String apiContext;

        private String versionContext;

        public Context(String apiContext, String versionContext) {
            this.apiContext = apiContext;
            this.versionContext = versionContext;
        }

        private String getApiAndVersionContext() {
            return "/" + apiContext + "/" + versionContext;
        }
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        Versionable versionable = AnnotationUtils.findAnnotation(handlerType, Versionable.class);
        if(versionable != null) {
            contextList.add(new Context(versionable.apiContext(),versionable.versionContext()));
        }
        return super.getMappingForMethod(method, handlerType);
    }

    @Override
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        HandlerMethod method = super.lookupHandlerMethod(lookupPath, request);
        for(Context context : contextList) {
            String contextPath = context.getApiAndVersionContext();
            if (method == null && lookupPath.contains(contextPath)) {
                String afterAPIURL = lookupPath.substring(lookupPath.indexOf(contextPath) + contextPath.length());
                String version = afterAPIURL.substring(0, afterAPIURL.indexOf("/"));
                if(!StringUtils.isNumeric(version)) {
                    break;
                }
                String path = afterAPIURL.substring(version.length() + 1);

                int previousVersion = getPreviousVersion(version);
                if (previousVersion != 0) {
                    lookupPath = contextPath + previousVersion + "/" + path;
                    final String lookupFinal = lookupPath;
                    return lookupHandlerMethod(lookupPath, new HttpServletRequestWrapper(request) {
                        @Override
                        public String getRequestURI() {
                            return lookupFinal;
                        }

                        @Override
                        public String getServletPath() {
                            return lookupFinal;
                        }});
                }
            }
        }
        return method;
    }

    private int getPreviousVersion(final String version) {
        return new Integer(version) - 1 ;
    }
}