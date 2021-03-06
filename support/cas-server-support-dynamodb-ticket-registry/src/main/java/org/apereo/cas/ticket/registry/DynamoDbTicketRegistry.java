package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.Ticket;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * This is {@link DynamoDbTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DynamoDbTicketRegistry extends AbstractTicketRegistry {
    private final DynamoDbTicketRegistryFacilitator dbTableService;

    public DynamoDbTicketRegistry(final CipherExecutor cipher, final DynamoDbTicketRegistryFacilitator dbTableService) {
        setCipherExecutor(cipher);
        this.dbTableService = dbTableService;
        LOGGER.info("Setting up DynamoDb Ticket Registry instance");
    }

    @Override
    public void addTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(), ticket.getExpirationPolicy().getTimeToLive());
            val encTicket = encodeTicket(ticket);
            this.dbTableService.put(ticket, encTicket);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        val encTicketId = encodeTicketId(ticketId);
        if (StringUtils.isNotBlank(encTicketId)) {
            LOGGER.debug("Retrieving ticket [{}] ", ticketId);
            val ticket = this.dbTableService.get(ticketId, encTicketId);
            val decodedTicket = decodeTicket(ticket);
            if (decodedTicket == null || decodedTicket.isExpired()) {
                LOGGER.warn("The expiration policy for ticket id [{}] has expired the ticket", ticketId);
                return null;
            }
            return decodedTicket;
        }
        return null;
    }

    @Override
    public long deleteAll() {
        return this.dbTableService.deleteAll();
    }

    @Override
    public Collection<Ticket> getTickets() {
        return decodeTickets(this.dbTableService.getAll());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        return this.dbTableService.delete(ticketIdToDelete, ticketId);
    }
}
