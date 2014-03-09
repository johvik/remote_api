package remote.test.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import remote.api.ClientProtocol;
import remote.api.Packet;
import remote.api.PacketScanner;
import remote.api.ServerProtocol;
import remote.api.ServerProtocol.AuthenticationCheck;
import remote.api.ServerProtocol.CommandHandler;
import remote.api.commands.Command;
import remote.api.commands.MouseMove;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;
import remote.api.messages.CommandRequest;
import remote.api.messages.Message;

public class TestClientServer {
	private ExecutorService es = Executors.newFixedThreadPool(1);
	private static final int HELP_SLEEP = 50; // Time to let pool thread run

	private AuthenticationCheck authentication = new AuthenticationCheck() {
		@Override
		public boolean check(String user, String password) {
			return true; // Accept all
		}
	};
	private CommandHandler commandHandler = new CommandHandler() {
		@Override
		public void handle(Command command) {
		}
	};

	@Test(timeout = 10000)
	public void test() throws GeneralSecurityException, ProtocolException,
			PacketException, IOException, InterruptedException {
		PipedOutputStream clientOutput = new PipedOutputStream();
		PipedOutputStream serverOutput = new PipedOutputStream();
		// Redirect server output to client
		PipedInputStream clientInput = new PipedInputStream(serverOutput);
		// Redirect client output to server
		PipedInputStream serverInput = new PipedInputStream(clientOutput);
		PacketScanner clientPs = new PacketScanner(clientInput);
		PacketScanner serverPs = new PacketScanner(serverInput);

		final ClientProtocol cp = new ClientProtocol(Misc.publicKey, Misc.key,
				clientOutput);
		final ServerProtocol sp = new ServerProtocol(authentication,
				commandHandler, Misc.privateKey, serverOutput);

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

		final Packet authenticationPacket = serverPs.nextPacket();
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

		Packet p = clientPs.nextPacket();
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

		p = serverPs.nextPacket();
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
		final Packet clientPing = clientPs.nextPacket();
		final Packet serverPing = serverPs.nextPacket();
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
		p = clientPs.nextPacket();
		cp.process(p);
		assertEquals(Message.PING, p.decode(null).getType());
		p = serverPs.nextPacket();
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
