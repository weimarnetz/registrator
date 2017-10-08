package de.weimarnetz.registrator.metrics;

import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMetrics
public class MetricsConfiguration extends MetricsConfigurerAdapter {

    @Override
    public void configureReporters(MetricRegistry metricRegistry) {

    }
}
