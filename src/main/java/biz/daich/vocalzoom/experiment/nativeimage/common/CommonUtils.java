package biz.daich.vocalzoom.experiment.nativeimage.common;

import java.awt.Window;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class CommonUtils extends SerializationTools {
	static final Logger l = LoggerFactory.getLogger(CommonUtils.class.getName());

	public final static SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

	public static String getFormattedDateTimeFileNameCompat(Clock clock) {
		if (clock == null) {
			clock = Clock.systemDefaultZone();
		}
		return FILE_DATE_FORMAT.format(Date.from(clock.instant()));
	}

	/**
	 * format long number of milliseconds since epoch to a human readable date of the form yyyy-MM-dd-HH-mm-ss-SSS
	 * the string generated is compatible to be part of a filename
	 *
	 * @param timestamp
	 *            - milliseconds since epoch
	 */
	public static String getFormattedDateTimeFileNameCompat(long timestamp) {
		return FILE_DATE_FORMAT.format(new Date(timestamp));
	}

	/**
	 * does the same like getFormattedDateTimeFileNameCompat(long timestamp) but first converts the String representation of the timestamp to long
	 */
	public static String getFormattedDateTimeFileNameCompat(String timestamp) {
		long x = 0;
		try {
			x = Long.parseLong(timestamp);
		} catch (NumberFormatException e) {
			l.error("strMilliEpochToStrDateTime(String)", e); //$NON-NLS-1$
		}
		return CommonUtils.getFormattedDateTimeFileNameCompat(x);

	}

	public static <T> T fromMqttMessage(MqttMessage mm, Class<T> c) {
		return CommonUtils.fromJson(new String(mm.getPayload(), StandardCharsets.UTF_8), c);
	}

	public static class ToJson<D> implements Function<D, byte[]> {

		@Override
		public byte[] apply(D t) {

			String json = toConciseJson(t);
			// l.trace(json);
			return json.getBytes(StandardCharsets.UTF_8);
		}
	}

	public static byte[] toPrettyJsonBytes(Object o) {
		return CommonUtils.toPrettyJson(o).getBytes(StandardCharsets.UTF_8);
	}

	public static byte[] toConciseJsonBytes(Object o) {
		return CommonUtils.toConciseJson(o).getBytes(StandardCharsets.UTF_8);
	}

	@Data
	public abstract static class AbstractBaseCmdConfig implements Serializable {
		@Option(names = {
				"-?", "--help", "-help", "/?", "\\?", "?", "-h"
		}, usageHelp = true, description = "display a help message", defaultValue = "false")

		public boolean helpRequested = false;
	}

	/**
	 * parses the command line options and fills in the T t instance that should be provided.
	 *
	 * Tolerates unmatched arguments so can be used as part of something bigger with additional options
	 *
	 * if printHelpIfRequested = true and there was a request for help - prints it and returns null;
	 *
	 * Returns null if there was a request for help and printHelpIfRequested = true
	 * else returns the T t instance filled.
	 *
	 */
	public static <T extends AbstractBaseCmdConfig> CommandLine processCmd(String[] args, T t, boolean printHelpIfRequested) {

		CommandLine cmdLine = new CommandLine(t).setUnmatchedArgumentsAllowed(true);
		if (args == null) {
			args = new String[] {};
		}
		cmdLine.parseArgs(args);
		if (l.isInfoEnabled()) {
			l.info("CmdArgs Obj = {}", toPrettyJson(t));
		}
		if (printHelpIfRequested && t.helpRequested) {
			cmdLine.usage(System.out);
		}
		return cmdLine;
	}

	public static void busyWaitMicros(long micros) {
		long waitUntil = System.nanoTime() + (micros * 1_000);
		while (waitUntil > System.nanoTime()) {
			;
		}
	}

	/**
	 * see here https://stackoverflow.com/questions/144892/how-to-center-a-window-in-java
	 */
	public static void centreWindow(Window frame) {
		frame.setLocationRelativeTo(null);
		// Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		// int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		// int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		// frame.setLocation(x, y);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * used by byteArrayToHexString(byte[] bytes)
	 */
	static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

	/**
	 * convert byte[] to a string of the form "AA43FF" every byte 2 letters.
	 *
	 * from here https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	/**
	 * bind changes of a Swing JTextField with something that wants to know about it
	 * sample use
	 *
	 * Binder.bind(getTxtUnitId(), (s) -> panelSeahMqttForwarding.setUnitId(s));
	 *
	 */
	public static class Binder {
		public static void bind(JTextField txt, Consumer<String> consumer) {
			txt.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					a();
				}

				public void removeUpdate(DocumentEvent e) {
					a();
				}

				public void insertUpdate(DocumentEvent e) {
					a();
				}

				public void a() {
					consumer.accept(txt.getText());
				}
			});
		}
	}
}
