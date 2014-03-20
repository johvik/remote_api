package remote.test.api;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import remote.api.ClientProtocol;
import remote.api.Packet;
import remote.api.ServerProtocol;
import remote.api.ServerProtocol.ConnectionHandler;
import remote.api.ServerProtocol.Handler;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;
import remote.api.messages.TerminateRequest;

/**
 * Test class to handle client server interaction.
 */
public class TestClientServer {
	/**
	 * The handler.
	 */
	private Handler handler = new Handler() {
		@Override
		public boolean authentication(String user, String password) {
			return true; // Always accept
		}

		@Override
		public void command(Command command) {
		}

		@Override
		public void terminate(boolean shutdown) {
		}
	};
	/**
	 * The connection handler.
	 */
	private ConnectionHandler connectionHandler = new ConnectionHandler() {
		@Override
		public void onAuthenticated() {
		}
	};

	/**
	 * Tests client server interaction.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void test() throws Exception {
		ByteArrayOutputStream clientOutput = new ByteArrayOutputStream();
		ByteArrayOutputStream serverOutput = new ByteArrayOutputStream();
		// Redirect server output to client
		MagicInputStream clientInput = new MagicInputStream(serverOutput);
		// Redirect client output to server
		MagicInputStream serverInput = new MagicInputStream(clientOutput);

		ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				Misc.iv, clientInput, clientOutput);
		ServerProtocol sp = new ServerProtocol(handler, connectionHandler,
				Misc.privateKey, serverInput, serverOutput);

		// Authenticate
		cp.authenticate("user", "password");

		serverInput.update();
		Packet p = sp.nextPacket();
		sp.process(p);

		clientInput.update();
		p = cp.nextPacket();
		cp.process(p);
		assertEquals(Message.AUTHENTICATION_RESPONSE, p.decode(null).getType());

		// Send a command
		MouseMove mm = new MouseMove((short) 123, (short) 456);
		cp.commandRequest(mm);

		serverInput.update();
		p = sp.nextPacket();
		sp.process(p);
		assertEquals(0,
				mm.compareTo(((CommandRequest) p.decode(null)).getCommand()));

		// Send terminate
		boolean shutdown = false;
		cp.terminateRequest(shutdown);

		serverInput.update();
		p = sp.nextPacket();
		sp.process(p);
		assertEquals(shutdown, ((TerminateRequest) p.decode(null)).isShutdown());

		// Ping in both directions
		cp.ping(null);
		sp.ping(null);

		// Respond to ping
		clientInput.update();
		Packet clientPing = cp.nextPacket();
		serverInput.update();
		Packet serverPing = sp.nextPacket();

		cp.process(clientPing);
		sp.process(serverPing);

		// Read ping response
		clientInput.update();
		p = cp.nextPacket();
		cp.process(p);
		assertEquals(Message.PING, p.decode(null).getType());
		serverInput.update();
		p = sp.nextPacket();
		sp.process(p);
		assertEquals(Message.PING, p.decode(null).getType());

		clientOutput.close();
		serverOutput.close();
		clientInput.close();
		serverInput.close();
	}
}
