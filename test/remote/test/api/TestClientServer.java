package remote.test.api;

import static org.junit.Assert.*;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import remote.api.ClientProtocol;
import remote.api.Packet;
import remote.api.ServerProtocol;
import remote.api.ServerProtocol.AuthenticationCheck;
import remote.api.ServerProtocol.CommandHandler;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;

/**
 * Test class to handle client server interaction.
 */
public class TestClientServer {
	/**
	 * Executor to run in another thread.
	 */
	private ExecutorService es = Executors.newFixedThreadPool(1);
	/**
	 * Time to let pool thread run.
	 */
	private static final int HELP_SLEEP = 50;

	/**
	 * The authentication check that always passes.
	 */
	private AuthenticationCheck authentication = new AuthenticationCheck() {
		@Override
		public boolean check(String user, String password) {
			return true; // Accept all
		}
	};
	/**
	 * The command handler.
	 */
	private CommandHandler commandHandler = new CommandHandler() {
		@Override
		public void handle(Command command) {
		}
	};

	/**
	 * Tests client server interaction.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test(timeout = 10000)
	public void test() throws Exception {
		PipedOutputStream clientOutput = new PipedOutputStream();
		PipedOutputStream serverOutput = new PipedOutputStream();
		// Redirect server output to client
		PipedInputStream clientInput = new PipedInputStream(serverOutput);
		// Redirect client output to server
		PipedInputStream serverInput = new PipedInputStream(clientOutput);

		final ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				clientInput, clientOutput);
		final ServerProtocol sp = new ServerProtocol(authentication,
				commandHandler, Misc.privateKey, serverInput, serverOutput);

		// Authenticate
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					cp.authenticate("user", "password");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(HELP_SLEEP);

		final Packet authenticationPacket = sp.nextPacket();
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					sp.process(authenticationPacket);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(HELP_SLEEP);

		Packet p = cp.nextPacket();
		cp.process(p); // Does not write anything
		assertEquals(Message.AUTHENTICATION_RESPONSE, p.decode(null).getType());

		// Send a command
		final MouseMove mm = new MouseMove((short) 123, (short) 456);
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					cp.commandRequest(mm);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(HELP_SLEEP);

		p = sp.nextPacket();
		sp.process(p); // Does not write anything
		assertEquals(0,
				mm.compareTo(((CommandRequest) p.decode(null)).getCommand()));

		// Ping in both directions
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					cp.ping(null);
					sp.ping(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(HELP_SLEEP);

		// Respond to ping
		final Packet clientPing = cp.nextPacket();
		final Packet serverPing = sp.nextPacket();
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					cp.process(clientPing);
					sp.process(serverPing);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(HELP_SLEEP);

		// Read ping response
		p = cp.nextPacket();
		cp.process(p);
		assertEquals(Message.PING, p.decode(null).getType());
		p = sp.nextPacket();
		sp.process(p);
		assertEquals(Message.PING, p.decode(null).getType());

		// Make sure thread has stopped
		es.shutdown();
		assertEquals(true,
				es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS));

		clientOutput.close();
		serverOutput.close();
		clientInput.close();
		serverInput.close();
	}
}
