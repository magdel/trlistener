package ru.netradar.web.web.resolvers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;
import ru.netradar.web.web.domain.CommonResponse;
import ru.netradar.web.web.domain.ErrorMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CommonExceptionResolver implements HandlerExceptionResolver {

    private final static Logger LOG = LoggerFactory.getLogger(CommonExceptionResolver.class);

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex instanceof NoHandlerFoundException) {
            return null;
        }

        final long errorId = System.currentTimeMillis(); //todo some hash
        LOG.error("Capturing exception " + errorId, ex);

        HandlerMethod method = null;
        if (handler != null) {
            method = (HandlerMethod) handler;
        }

        ModelAndView res;
        String contentType = getContentType(method, request);
        if (contentType != null) {
            if (contentType.contains(MediaType.TEXT_HTML_VALUE)) {
                res = new ModelAndView("error");
            } else if (contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                MappingJackson2JsonView view = new MappingJackson2JsonView();
                view.setExtractValueFromSingleKeyModel(true);
                res = new ModelAndView(view);
            } else if (contentType.contains(MediaType.APPLICATION_XML_VALUE)) {
                MappingJackson2XmlView view = new MappingJackson2XmlView();
                res = new ModelAndView(view);
            } else {
                res = new ModelAndView("error");
            }
        } else {
            res = new ModelAndView("error");
        }

        CommonResponse commonCommonResponse = new CommonResponse();
        commonCommonResponse.setError(new ErrorMessage());
        commonCommonResponse.getError().setErrorId(errorId);

        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            commonCommonResponse.getError().setTechMessage(ex.getMessage());
        /*} else if (ex instanceof ValidationException) {

            StringBuilder sb = new StringBuilder("Validation error: \n");
            for (FieldError fieldError : ((ValidationException) ex).getFieldErrors()) {
                sb.append(fieldError.getField()).append(" - ").append(fieldError.getDefaultMessage()).append("\n");
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            commonCommonResponse.getError().setTechMessage(sb.toString());*/
        } else if (ex instanceof HttpStatusCodeException) {
            String responseBody = ((HttpStatusCodeException) ex).getResponseBodyAsString();
            HttpStatus statusCode = ((HttpStatusCodeException) ex).getStatusCode();
            LOG.error("HTTP request error: code {} , body {}", statusCode, responseBody);
      /*  } else if (ex instanceof SomeAppException) {
            commonCommonResponse.getError().setTechMessage(ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);*/
       /* } else if (ex instanceof AccessDeniedException) {
            commonCommonResponse.getError().setTechMessage("Access denied");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);*/
        } else {
            commonCommonResponse.getError().setTechMessage("Internal error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        res.addObject(commonCommonResponse);
        return res;

    }

    private String getContentType(HandlerMethod method, HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getContentType() != null) {
            return httpServletRequest.getContentType();
        }

        if (httpServletRequest.getHeader(HttpHeaders.ACCEPT) != null) {
            return httpServletRequest.getHeader(HttpHeaders.ACCEPT);
        }

        if (method != null) {
            RequestMapping requestMapping = method.getMethod().getAnnotation(RequestMapping.class);
            if (requestMapping != null && requestMapping.produces().length > 0) {
                return requestMapping.produces()[0];
            }

            requestMapping = method.getBeanType().getAnnotation(RequestMapping.class);
            if (requestMapping != null && requestMapping.produces().length > 0) {
                return requestMapping.produces()[0];
            }
        }

        return null;
    }

}
