package org.apereo.cas.support.oauth.web;

import org.apereo.cas.category.MemcachedCategory;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link OAuth20AccessTokenControllerMemcachedTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Import(MemcachedTicketRegistryConfiguration.class)
@TestPropertySource(properties = {
    "cas.ticket.registry.memcached.servers=localhost:11211",
    "cas.ticket.registry.memcached.failureMode=Redistribute",
    "cas.ticket.registry.memcached.locatorType=ARRAY_MOD",
    "cas.ticket.registry.memcached.hashAlgorithm=FNV1A_64_HASH"
})
@Category(MemcachedCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 11211)
public class OAuth20AccessTokenControllerMemcachedTests extends AbstractOAuth20Tests {

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Before
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyTicketRegistry() {
        assertTrue(this.ticketRegistry instanceof MemcachedTicketRegistry);
    }

    @Test
    public void verifyOAuthCodeIsAddedToMemcached() {
        val p = createPrincipal();
        val code = addCode(p, addRegisteredService());
        val ticket = this.ticketRegistry.getTicket(code.getId(), OAuthCode.class);
        assertNotNull(ticket);
    }
}
