package com.amehta.proxy.redis.interact.health;

import com.codahale.metrics.health.HealthCheck;

public class TemplateHealthCheck extends HealthCheck {

    // TODO: Figure out how to use this

    public TemplateHealthCheck() {
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
