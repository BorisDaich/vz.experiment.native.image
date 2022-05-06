package biz.daich.vocalzoom.experiment.nativeimage.common;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import de.undercouch.bson4jackson.BsonFactory;

public class SerializationTools implements IGlobals {
	private static final Logger l = LogManager.getLogger(SerializationTools.class.getName());

	/**
	 * Read and write BSON. Used mainly for the Sensor Raw Data sending / receiving over MQTT /[projectID]/[deviceId]/data topic
	 */
	public static final ObjectMapper bson;
	public static final ObjectMapper prettyJson;
	public static final ObjectMapper conciseJson;
	static {
		JsonFactory f = JsonFactory.builder()
				.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
				.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
				.enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
				.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
				.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
				.enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
				.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
				.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
				.build();

		ObjectMapper om = new ObjectMapper(f);//

		om.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)//
				.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)//
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.setSerializationInclusion(Include.ALWAYS)//
				.setDateFormat(new SimpleDateFormat(StdDateFormat.DATE_FORMAT_STR_ISO8601))
				.setVisibility(
						om.getSerializationConfig()
								.getDefaultVisibilityChecker()
								.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
								.withGetterVisibility(JsonAutoDetect.Visibility.NONE));

		om.findAndRegisterModules();
		conciseJson = om;
		prettyJson = om.copy().enable(SerializationFeature.INDENT_OUTPUT);

		bson = new ObjectMapper(new BsonFactory());
		bson.findAndRegisterModules();

	};

	public static String toPrettyJson(Object o) {
		try {
			return prettyJson.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			l.debug("", e);
			return null;
		}
	}

	public static String toConciseJson(Object o) {
		try {
			return conciseJson.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			l.debug("", e);
			return null;
		}
	}

	public static <T> T fromJson(String s, Class<T> t) {
		try {
			return prettyJson.readValue(s, t);
		} catch (JsonProcessingException e) {
			l.error("", e);
			throw new RuntimeException(e);
		}
	}

	public static class ByteArrayToStrOfLenJsonSerializer extends JsonSerializer<byte[]> {

		@Override
		public void serialize(byte[] value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
			String str = "" + value.length;
			jsonGenerator.writeObject(str);

		}

	}

	public static class StrOfLenJsonToByteArrayDeSerializer extends StdDeserializer<byte[]> {
		public static final String DATA_LOST = "DATA LOST";

		public StrOfLenJsonToByteArrayDeSerializer() {
			this(null);
		}

		public StrOfLenJsonToByteArrayDeSerializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
			return DATA_LOST.getBytes();
		}

	}

	public static ObjectMapper objectMapperWithBytesReplacer;
	static {
		SimpleModule module = new SimpleModule();
		module.addSerializer(byte[].class, new SerializationTools.ByteArrayToStrOfLenJsonSerializer());
		module.addDeserializer(byte[].class, new SerializationTools.StrOfLenJsonToByteArrayDeSerializer());

		objectMapperWithBytesReplacer = prettyJson.copy().registerModule(module);
	}

}
