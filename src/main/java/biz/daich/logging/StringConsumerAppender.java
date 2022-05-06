package biz.daich.logging;

import static org.apache.logging.log4j.core.config.Property.EMPTY_ARRAY;
import static org.apache.logging.log4j.core.layout.PatternLayout.createDefaultLayout;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "StringConsumerAppender", category = "Core", elementType = "appender", printObject = true)
public class StringConsumerAppender extends AbstractAppender {
	private static volatile ArrayList<Consumer<String>> stringConsumers = new ArrayList<>();

	private StringConsumerAppender(String name, Layout<?> layout, Filter filter, boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions, EMPTY_ARRAY);
	}

	@SuppressWarnings("unused")
	@PluginFactory
	public static StringConsumerAppender createAppender(@PluginAttribute("name") String name,
			@PluginAttribute("maxLines") int maxLines,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
			@PluginElement("Layout") Layout<?> layout,
			@PluginElement("Filters") Filter filter) {
		if (name == null) {
			LOGGER.error("No name provided for StringConsumerAppender");
			return null;
		}

		if (layout == null) {
			layout = createDefaultLayout();
		}
		return new StringConsumerAppender(name, layout, filter, ignoreExceptions);
	}

	/// Add the target Consumer<String> to be populated and updated by the logging information.
	public static boolean addStringConsumer(final Consumer<String> stringConsumer) {
		return StringConsumerAppender.stringConsumers.add(stringConsumer);
	}

	/// Add the target Consumer<String> to be populated and updated by the logging information.
	public static boolean removeStringConsumer(final Consumer<String> stringConsumer) {
		return StringConsumerAppender.stringConsumers.remove(stringConsumer);
	}

	@Override
	public void append(LogEvent event) {
		String message = new String(this.getLayout().toByteArray(event));

		// Append formatted message to text area using the Thread.
		try {
			for (Consumer<String> stringConsumer : stringConsumers) {
				try {
					if (stringConsumer != null) {
						stringConsumer.accept(message);
					}
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
			}
		} catch (IllegalStateException exception) {
			exception.printStackTrace();
		}
	}
}