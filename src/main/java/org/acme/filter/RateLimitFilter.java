package org.acme.filter;

import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@Priority(1)
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, long[]> requestCounts
            = new ConcurrentHashMap<>();

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext ctx) {
        // only rate limit the register endpoint
        if (!ctx.getUriInfo().getPath().contains("/auth/register")) return;

        String ip = request.remoteAddress().host();
        long now = System.currentTimeMillis();

        requestCounts.compute(ip, (key, val) -> {
            if (val == null || now - val[1] > WINDOW_MILLIS) {
                return new long[]{1, now};
            }
            val[0]++;
            return val;
        });

        long[] data = requestCounts.get(ip);
        if (data[0] > MAX_REQUESTS) {
            long retryAfter = (WINDOW_MILLIS - (now - data[1])) / 1000;
            LOG.warnf("Rate limit exceeded for IP: %s", ip);
            throw new WebApplicationException(
                    Response.status(429)
                            .header("Retry-After", retryAfter)
                            .entity(Map.of(
                                    "status", 429,
                                    "error", "Too Many Requests",
                                    "message", "Too many attempts. Try again in "
                                            + retryAfter + " seconds"
                            ))
                            .build()
            );
        }
    }
}