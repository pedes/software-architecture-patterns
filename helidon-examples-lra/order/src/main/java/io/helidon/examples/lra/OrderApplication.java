package io.helidon.examples.lra;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import io.narayana.lra.filter.FilterRegistration;

@ApplicationScoped
@ApplicationPath("/")
public class OrderApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<>();
        s.add(FilterRegistration.class);
        s.add(OrderResource.class);
        return s;
    }

}
