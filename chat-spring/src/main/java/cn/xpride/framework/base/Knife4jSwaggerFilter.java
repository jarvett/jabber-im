package cn.xpride.framework.base;

import com.github.xiaoymin.knife4j.spring.filter.BasicFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class Knife4jSwaggerFilter extends BasicFilter implements Filter {

    @Value("${knife4j.production}")
    private boolean production;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (this.production) {
            String uri = httpServletRequest.getRequestURI();
            if (!this.match(uri)) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                httpServletResponse.setStatus(404);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }


}
