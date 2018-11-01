package net.corda.businessnetworks.ticketing.test.support

import net.corda.businessnetworks.ticketing.contracts.Ticket
import net.corda.businessnetworks.ticketing.contracts.TicketStatus
import net.corda.businessnetworks.ticketing.flows.bno.ActivateTicketByLinearIdFlow
import net.corda.businessnetworks.ticketing.flows.bno.RevokeTicketByLinearIdFlow
import net.corda.businessnetworks.ticketing.flows.member.RequestTicketFlow
import net.corda.businessnetworks.ticketing.test.flows.TestInitiator
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.StartedMockNode
import java.time.Instant
import kotlin.test.assertEquals


abstract class TicketingServiceTestsSupport(threadPerNode : Boolean = false) : BusinessNetworksTestsSupport(listOf("net.corda.businessnetworks.ticketing.contracts",
                                                                                    "net.corda.businessnetworks.ticketing.flows.member",
                                                                                     "net.corda.businessnetworks.ticketing.test.flows"),threadPerNode) {

    @CordaSerializable
    enum class TestTicketSubject {
        SUBJECT_1,
        SUBJECT_2
    }

    protected fun <T> acquirePartiesTargetedTicket(memberNode : StartedMockNode, target : Party, subject : T) {
        val ticket = Ticket.PartiesTargetedTicket(memberNode.party(), bnoNode.party(), subject, listOf(target))
        acquireATicket(memberNode, ticket)
    }

    protected fun <T> acquireAWideTicketAndConfirmAssertions(memberNode : StartedMockNode, subject : T) : String {
        val ticket = Ticket.WideTicket(memberNode.party(), bnoNode.party(), subject)
        return acquireATicket(memberNode, ticket)
    }

    protected fun <T> acquireAnExpiringWideTicketAndConfirmAssertions(memberNode : StartedMockNode, subject : T, expiresAt : Instant) : String {
        val ticket = Ticket.ExpiringWideTicket(memberNode.party(), bnoNode.party(), subject, expiresAt)
        return acquireATicket(memberNode, ticket)
    }

    protected fun <T> requestATicket(memberNode: StartedMockNode, ticket : Ticket.State<T>) : String {
        val future = memberNode.startFlow(RequestTicketFlow(ticket))
        runNetwork()
        future.getOrThrow()

        lateinit var ticketId : String

        memberNode.transaction {
            val tickets = memberNode.services.vaultService.queryBy<Ticket.State<*>>().states
            assertEquals(1, tickets.size)
            assertEquals(TicketStatus.PENDING, tickets.single().state.data.status)
            ticketId = tickets.single().state.data.linearId.toString()
        }

        bnoNode.transaction {
            val tickets = bnoNode.services.vaultService.queryBy<Ticket.State<*>>().states
            assertEquals(1, tickets.size)
            assertEquals(TicketStatus.PENDING, tickets.single().state.data.status)
            assertEquals(ticketId, tickets.single().state.data.linearId.toString())
        }

        return ticketId
    }

    protected fun activateATicket(memberNode: StartedMockNode, ticketLinearId : String) {
        val future = bnoNode.startFlow(ActivateTicketByLinearIdFlow(ticketLinearId))
        runNetwork()
        future.getOrThrow()

        //confirm it's in the vaults in active state
        memberNode.transaction {
            val tickets = memberNode.services.vaultService.queryBy<Ticket.State<*>>().states
            assertEquals(1, tickets.size)
            assertEquals(TicketStatus.ACTIVE, tickets.single().state.data.status)
        }

        bnoNode.transaction {
            val tickets = bnoNode.services.vaultService.queryBy<Ticket.State<*>>().states
            assertEquals(1, tickets.size)
            assertEquals(TicketStatus.ACTIVE, tickets.single().state.data.status)
        }
    }

    protected fun revokeATicketAndConfirmAssertions(memberNode: StartedMockNode, ticketLinearId : String) {
        val future = bnoNode.startFlow(RevokeTicketByLinearIdFlow(ticketLinearId))
        runNetwork()
        future.getOrThrow()

        //confirm it's gone from the vaults
        memberNode.transaction {
            val tickets = memberNode.services.vaultService.queryBy<Ticket.State<*>>().states
            assertEquals(0, tickets.size)
        }

        bnoNode.transaction {
            val tickets = bnoNode.services.vaultService.queryBy<Ticket.State<*>>().states
            assertEquals(0, tickets.size)
        }
    }

    protected fun <T> acquireATicket(memberNode : StartedMockNode, ticket : Ticket.State<T>) : String {
        val ticketId = requestATicket(memberNode, ticket)
        activateATicket(memberNode, ticketId)
        return ticketId
    }

    protected fun runGuineaPigFlow(initiator : StartedMockNode, initiatee : StartedMockNode) : String {
        val future = initiator.startFlow(TestInitiator(initiatee.party()))
        runNetwork()
        return future.getOrThrow()
    }

}