package biz.daich.vocalzoom.experiment.nativeimage.graph;

import java.io.Serializable;
import java.util.Random;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * this is parsed form of the Esp32RawDataMsg.
 * it has v- velocity d - distance and m - mask separated into separate arrays
 * ts - a time stamp as milliseconds since epoch. see U.buildEsp32Timestamp()
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SensorRawDataMsg implements Serializable {
	
	public static final Random RANDOM = new Random();
	
	private static final long serialVersionUID = 1L;
	public String id;
	public int c;
	public int[] v;
	public int[] d;
	public byte[] m;
	public long ts = 0;
	public double intervalMicro = 0;
	
	public static SensorRawDataMsg genRandomRawMsg(int size) {

		SensorRawDataMsg z = new SensorRawDataMsg();
		z.id = "id" + RANDOM.nextDouble();
		z.c = RANDOM.nextInt();
		z.d = new int[size];
		z.v = new int[size];
		z.m = new byte[size];

		for (int i = 0; i < size; i++) {
			z.d[i] = RANDOM.nextInt(256);
			z.v[i] = RANDOM.nextInt(64);
			z.m[i] = (byte) RANDOM.nextInt(4);
		}
		return z;
	}
}