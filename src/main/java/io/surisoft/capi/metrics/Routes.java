package io.surisoft.capi.metrics;

import io.surisoft.capi.processor.MetricsProcessor;
import io.surisoft.capi.schema.RouteDetailsEndpointInfo;
import io.surisoft.capi.schema.RouteEndpointInfo;
import io.surisoft.capi.schema.Service;
import io.surisoft.capi.schema.StickySession;
import io.surisoft.capi.utils.RouteUtils;
import io.surisoft.capi.utils.ServiceUtils;
import org.apache.camel.CamelContext;
import org.cache2k.Cache;
import org.cache2k.CacheEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Endpoint(id = "routes")
public class Routes {

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private Cache<String, Service> serviceCache;

    @Autowired
    private Cache<String, StickySession> stickySessionCache;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private RouteUtils routeUtils;

    @Autowired
    private MetricsProcessor metricsProcessor;

    @ReadOperation
    public Service getCachedService(@Selector String serviceName) {
        if(serviceCache.containsKey(serviceName)) {
            return serviceCache.get(serviceName);
        }
        return null;
    }

    @ReadOperation
    public List<RouteDetailsEndpointInfo> getAllRoutesInfo() {
        List<RouteDetailsEndpointInfo> detailInfoList = new ArrayList<>();
        List<RouteEndpointInfo> routeEndpointInfoList = camelContext.getRoutes().stream()
                .map(RouteEndpointInfo::new)
                .toList();
        for(RouteEndpointInfo routeEndpointInfo : routeEndpointInfoList) {
            if(routeEndpointInfo.getId().startsWith("rd_"))  {
                detailInfoList.add(new RouteDetailsEndpointInfo(camelContext, camelContext.getRoute(routeEndpointInfo.getId())));
            }
        }
        for(RouteDetailsEndpointInfo detailsEndpointInfo : detailInfoList) {
            detailsEndpointInfo.setId(detailsEndpointInfo.getId().replaceAll("rd_", ""));
        }
        return detailInfoList;
    }

   /*@Operation(summary = "Get all cached Sticky Sessions")
    @GetMapping(path = "/sticky-session")
    public ResponseEntity<Iterable<StickySession>> getCachedStickySession() {
        List<StickySession> stickySessionList = new ArrayList<>();
        for (CacheEntry<String, StickySession> stringServiceCacheEntry : stickySessionCache.entries()) {
            stickySessionList.add(stringServiceCacheEntry.getValue());
        }
        return new ResponseEntity<>(stickySessionList, HttpStatus.OK);
    }*/
}