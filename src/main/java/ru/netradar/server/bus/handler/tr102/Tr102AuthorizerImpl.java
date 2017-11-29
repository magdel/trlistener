package ru.netradar.server.bus.handler.tr102;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.netradar.server.bus.domain.DeviceIden;
import ru.netradar.server.http.SiteClient;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by rfk on 16.11.2017.
 */
@Component
public class Tr102AuthorizerImpl implements Tr102Authorizer {

    private static final Logger log = LoggerFactory.getLogger(Tr102AuthorizerImpl.class);
    private final LoadingCache<Tr102Iden, DeviceIden> trIdenCache;

    public Tr102AuthorizerImpl(@Autowired SiteClient siteClient) {
        this.trIdenCache = CacheBuilder.newBuilder()
                .initialCapacity(100)
                .concurrencyLevel(10)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<Tr102Iden, DeviceIden>() {
                    @Override
                    public DeviceIden load(Tr102Iden key) throws Exception {
                        Optional<DeviceIden> tr102IdenOptional = siteClient.findTr102Iden(key);
                        return tr102IdenOptional
                                .orElseThrow(() -> new IllegalArgumentException("Not authorized"));
                    }
                });
    }

    @Override
    @Nonnull
    public Optional<DeviceIden> identify(@Nonnull Tr102Iden tr102Iden) {
        try {
            return Optional.of(trIdenCache.get(tr102Iden));
        } catch (Exception e) {
            log.warn("Not identified: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
