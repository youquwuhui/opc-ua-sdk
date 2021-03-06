/*
 * digitalpetri OPC-UA SDK
 *
 * Copyright (C) 2015 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.opcua.sdk.client.fsm.states;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.ServiceFaultHandler;
import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.fsm.SessionState;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateContext;
import com.digitalpetri.opcua.sdk.client.fsm.SessionStateEvent;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.types.structured.ServiceFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Active implements SessionState {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile ServiceFaultHandler faultHandler;

    private final UaSession session;
    private final CompletableFuture<UaSession> sessionFuture;

    public Active(UaSession session, CompletableFuture<UaSession> sessionFuture) {
        this.session = session;
        this.sessionFuture = sessionFuture;
    }

    @Override
    public void activate(SessionStateEvent event, SessionStateContext context) {
        OpcUaClient client = context.getClient();

        client.addFaultHandler(faultHandler = new ServiceFaultHandler() {
            @Override
            public void handle(ServiceFault serviceFault) {
                long statusCode = serviceFault.getResponseHeader().getServiceResult().getValue();

                if (statusCode == StatusCodes.Bad_SessionIdInvalid) {
                    logger.warn("ServiceFault: {}", serviceFault.getResponseHeader().getServiceResult());
                    client.removeFaultHandler(this);
                    context.handleEvent(SessionStateEvent.CREATE_AND_ACTIVATE_REQUESTED);
                }
            }
        });

        sessionFuture.complete(session);
    }

    @Override
    public SessionState transition(SessionStateEvent event, SessionStateContext context) {
        OpcUaClient client = context.getClient();

        switch (event) {
            case CREATE_AND_ACTIVATE_REQUESTED:
                client.removeFaultHandler(faultHandler);

                return new CreateAndActivate(new CompletableFuture<>(), true);

            case CLOSE_SESSION_REQUESTED:
                client.removeFaultHandler(faultHandler);

                return new ClosingSession(sessionFuture);

            case ERR_CONNECTION_LOST:
                client.removeFaultHandler(faultHandler);

                return new Reactivate(session, 0);
        }

        return this;
    }

    @Override
    public CompletableFuture<UaSession> getSessionFuture() {
        return sessionFuture;
    }


}
