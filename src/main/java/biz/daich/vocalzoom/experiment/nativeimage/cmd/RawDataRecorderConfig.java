package biz.daich.vocalzoom.experiment.nativeimage.cmd;

import java.io.Serializable;

import biz.daich.vocalzoom.experiment.nativeimage.common.CommonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import picocli.CommandLine.Option;

@Data
@EqualsAndHashCode(callSuper = true)
public class RawDataRecorderConfig extends CommonUtils.AbstractBaseCmdConfig implements Serializable {

	@Option(names = {
			"--stopAfterSec"
	}, description = "will stop recording after this number of seconds. If not set or set to 0 will not stop", defaultValue = "0")
	public long stopAfterSec = 0;

	@Option(names = "--split", description = "how many messages to write to each file before starting a new one", defaultValue = "1000")
	public long splitCount = 1000;

	// mqtt config

	@Option(names = "--username", description = "mqtt broker login username defaults to null - no authentication", defaultValue = Option.NULL_VALUE)
	public String user = null;

	@Option(names = "--password", description = "mqtt broker login password defaults to null - no authentication", defaultValue = Option.NULL_VALUE)
	public String password = null;

	@Option(names = {
			"-host", "--host"
	}, description = "mqtt broker host name or IP", defaultValue = "localhost")
	public String host = "localhost";

	@Option(names = {
			"-p", "--port"
	}, description = "mqtt broker port", defaultValue = "1883")
	public int port = 1883;

	@Option(names = {
			"-t", "--topic"
	}, description = "mqtt broker topic to listen to. Wildcards accepted", defaultValue = "esp32/test")
	@NonNull
	public String topic = "esp32/test";

	@Option(names = "--clientId", description = "clientId to use when connecting to mqtt broker", defaultValue = "VzWatcher")
	public String clientId = "VzWatcher";

	@Option(names = "--qos", description = "QoS to use when connecting to mqtt broker", defaultValue = "0")
	public int qos = 0;

	
}