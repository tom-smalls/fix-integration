package uk.co.real_logic.fix_gateway.environments;

import uk.co.real_logic.agrona.collections.Int2ObjectHashMap;
import uk.co.real_logic.fix_gateway.FixGateway;
import uk.co.real_logic.fix_gateway.quickfix.TestConnection;
import uk.co.real_logic.fix_gateway.session.Session;
import uk.co.real_logic.fix_gateway.system_tests.FakeOtfAcceptor;
import uk.co.real_logic.fix_gateway.system_tests.FakeSessionHandler;

import java.io.IOException;

import static uk.co.real_logic.fix_gateway.TestFixtures.unusedPort;
import static uk.co.real_logic.fix_gateway.Timing.assertEventuallyTrue;
import static uk.co.real_logic.fix_gateway.session.SessionState.DISCONNECTED;
import static uk.co.real_logic.fix_gateway.system_tests.SystemTestUtil.launchAcceptingGateway;

public class QuickFixToGatewayEnvironment implements Environment
{
    private final Int2ObjectHashMap<TestConnection> connections = new Int2ObjectHashMap<>();
    private final Int2ObjectHashMap<Session> acceptors = new Int2ObjectHashMap<>();

    private final FakeOtfAcceptor acceptingOtfAcceptor = new FakeOtfAcceptor();
    private final FakeSessionHandler acceptingSessionHandler = new FakeSessionHandler(acceptingOtfAcceptor);

    private final FixGateway acceptingGateway;
    private final int port;

    public QuickFixToGatewayEnvironment()
    {
        port = unusedPort();
        acceptingGateway = launchAcceptingGateway(port, acceptingSessionHandler);
    }

    public void close() throws Exception
    {
        if (acceptingGateway != null)
        {
            acceptingGateway.close();
        }
    }

    public void connect(final int clientId) throws IOException
    {
        final TestConnection connection = new TestConnection();
        connection.connect(clientId, port);
        connections.put(clientId, connection);
        acceptors.put(clientId, acceptingSessionHandler.session());
    }

    public void initiateMessage(final int clientId, final String message) throws IOException
    {
        connections.get(clientId).sendMessage(clientId, message);
    }

    public void expectDisconnect(final int clientId) throws Exception
    {
        final Session session = acceptors.get(clientId);

        connections.get(clientId).waitForClientDisconnect(clientId);

        assertEventuallyTrue("Failed to disconnect",
            () ->
            {
                // TODO: figure out why this alters things
                session.poll(System.currentTimeMillis());
                return session.state() == DISCONNECTED;
            });
    }
}
