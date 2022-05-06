package biz.daich.vocalzoom.experiment.nativeimage.common;

import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.eventbus.EventBus;

public interface IGlobals {

	public static final EventBus bus = new EventBus();

	/**
	 * executor to be used all over the application
	 */
	public static final ExecutorService executor = Executors.newFixedThreadPool(5);

	public static final Timer timer = new Timer("Common Schedule Timer", true);
	public static final Random RANDOM = new Random();
}
