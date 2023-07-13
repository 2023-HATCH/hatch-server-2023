package hatch.hatchserver2023.global.common.response.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FilterExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            filterChain.doFilter(request, response);
        }catch (AuthException authException){
            setErrorResponse(response, authException.getCode());
        }catch (Exception e) {
            e.printStackTrace();
            setErrorResponse(response, e.getMessage());
        }
    }

    private void setErrorResponse(HttpServletResponse servletResponse, StatusCode code){
        CommonResponse errorResponse = CommonResponse.toErrorResponse(code);
        servletResponse.setStatus(code.getStatus().value());
        servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE); //"application/json"

        //생성한 errorResponse 를 servletResponse 에 write
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.getWriter().write(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setErrorResponse(HttpServletResponse servletResponse, String message){
        CommonCode code = CommonCode.INTERNAL_SERVER_ERROR;
        CommonResponse errorResponse = CommonResponse.toErrorResponse(code, message);
        servletResponse.setStatus(code.getStatus().value());
        servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE); //"application/json"

        //생성한 errorResponse 를 servletResponse 에 write
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.getWriter().write(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
