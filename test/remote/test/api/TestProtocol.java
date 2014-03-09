package remote.test.api;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.startsWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.crypto.SecretKey;

import org.junit.Test;

import remote.api.Packet;
import remote.api.Protocol;
import remote.api.exceptions.PacketException;
import remote.api.exceptions.ProtocolException;

/**
 * Test class for {@link Protocol}.
 */
public class TestProtocol {
	/**
	 * Test method for blockCipherInit in {@link Protocol}.
	 * 
	 * @throws Exception
	 *             If something went wrong.
	 */
	@Test
	public void testBlockCipherInit() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			new Protocol(Misc.privateKey, input, output) {
				@Override
				public void process(Packet packet) throws PacketException,
						IOException, ProtocolException {
				}

				public void exposedBlockCipherInit(SecretKey secretKey)
						throws ProtocolException {
					// Expose blockCipherInit to be able to produce exception
					super.blockCipherInit(secretKey);
				}
			}.exposedBlockCipherInit(null);
			fail("Did not throw an exception");
		} catch (ProtocolException e) {
			ProtocolException ex = new ProtocolException(
					"Failed to set block cipher");
			assertThat(e.getMessage(), startsWith(ex.getMessage()));
		}
	}
}
