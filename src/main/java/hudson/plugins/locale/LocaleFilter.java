package hudson.plugins.locale;

import hudson.model.User;
import hudson.plugins.locale.user.UserLocaleProperty;
import jenkins.model.Jenkins;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Locale;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class LocaleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
        // nop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            PluginImpl plugin = (PluginImpl) Jenkins.getActiveInstance().getPlugin("locale");
            final Locale locale;
            String currentUserLocale = getCurrentUserLocale();

            if (plugin == null) {
                chain.doFilter(request, response);
                return;
            } else if (plugin.isUserPrefer() && currentUserLocale != null) {
                locale = PluginImpl.parse(currentUserLocale);
            } else if (plugin.isIgnoreAcceptLanguage()) {
                locale = Locale.getDefault();
            } else {
                locale = null;
            }

            if(locale != null) {
                request = new HttpServletRequestWrapper((HttpServletRequest) request) {
                    @Override
                    public Locale getLocale() {
                        // Force locale to configured default, ignore request' Accept-Language header
                        return locale;
                    }
                };
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nop
    }

    private String getCurrentUserLocale() {
        User user = User.current();
        if(user != null) {
            UserLocaleProperty userLocaleProperty = user.getProperty(UserLocaleProperty.class);
            return userLocaleProperty.getUserLocale();
        }
        return null;
    }

}
