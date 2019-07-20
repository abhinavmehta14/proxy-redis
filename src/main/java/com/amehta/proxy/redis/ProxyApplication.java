package com.amehta.proxy.redis;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ProxyApplication extends Application<ProxyConfiguration> {

    public static void main(final String[] args) throws Exception {
        new ProxyApplication().run(args);
    }

    @Override
    public String getName() {
        return "proxy";
    }

    @Override
    public void initialize(final Bootstrap<ProxyConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final ProxyConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
