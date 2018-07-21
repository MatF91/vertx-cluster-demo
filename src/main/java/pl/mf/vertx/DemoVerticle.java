package pl.mf.vertx;

import java.util.Date;
import java.util.Set;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.AsyncMap;

public class DemoVerticle extends AbstractVerticle {
	private static String ASYNCMAP_ADDRESS = "VerticlesSharedMap";
	private static String HTTP_SERVER_ADD_TO_MAP = "/add";
	private static String HTTP_SERVER_MAP_ENTRIES = "/entries";
	private static String HTTP_SERVER_MAP_EVENTBUS_MSG = "/eventbusMessage";
	private static String EVENTBUS_ADDRESS = "eventbus_address";
	private static String EVENTBUS_MESSAGE = "Eventbus message";
	private AsyncMap<Integer, String> verticleData;
	private int httpServerPort;
	private String httpServerHost;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		logSomeClusterInformations();
		init();

		vertx.createHttpServer().requestHandler(handler -> {
			LogUtil.printMessageWithDate("PATH " + handler.path());
			HttpServerResponse response = handler.response();
			response.setChunked(true);
			if (handler.path().equals(HTTP_SERVER_ADD_TO_MAP)) {
				verticleData.put(httpServerPort, new Date().toString(), completionHandler -> {
					if (completionHandler.succeeded()) {
						response.write("New entry added: SUCCESS");
					} else {
						response.write("New entry added: FAILED");
					}
					handler.response().end();
				});
			} else if (handler.path().equals(HTTP_SERVER_MAP_ENTRIES)) {
				verticleData.entries(resultHandler -> {
					if (resultHandler.succeeded()) {
						response.write("ENTRIES: " + resultHandler.result());
					} else {
						response.write("ENTRIES: null");
					}
					handler.response().end();
				});
			} else if (handler.path().equals(HTTP_SERVER_MAP_EVENTBUS_MSG)) {
				response.write("Publishing eventbus message... Check logs.");
				vertx.eventBus().publish(EVENTBUS_ADDRESS, EVENTBUS_MESSAGE);
				handler.response().end();
			} else {
				handler.response().end();
			}
		}).listen(httpServerPort, httpServerHost);
	}

	/**
	 * Log some informations about working Hazelcast cluster
	 */
	private void logSomeClusterInformations() {
		Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
		LogUtil.printMessageWithDate("Hazelcast instances on current JVM: " + instances.size());
		for (HazelcastInstance hazelcastInstance : instances) {
			Cluster hazelcastCluster = hazelcastInstance.getCluster();
			LogUtil.printMessageWithDate("Local member: " + hazelcastCluster.getLocalMember());
			Set<Member> members = hazelcastCluster.getMembers();
			LogUtil.printMessageWithDate("All hazelcast instance members: " + members.size());
		}
		LogUtil.printMessageWithDate("vertx.isClustered: " + vertx.isClustered());
	}

	/**
	 * Initializing fields and consumers for this demo
	 */
	private void init() {
		httpServerPort = Integer.parseInt(PropertiesReaderUtil.getProperty("http-server-port"));
		httpServerHost = PropertiesReaderUtil.getProperty("http-server-host");
		vertx.sharedData().<Integer, String>getAsyncMap(ASYNCMAP_ADDRESS, resultHandler -> {
			if (resultHandler.succeeded()) {
				verticleData = resultHandler.result();
				LogUtil.printMessageWithDate("Access to map: SUCCESS");
			} else if (resultHandler.failed())
				LogUtil.printMessageWithDate("Access to map: FAILED");
		});
		vertx.eventBus().consumer(EVENTBUS_ADDRESS, messageConsumerHandler -> {
			LogUtil.printMessageWithDate(
					"Consumer " + httpServerHost + ":" + httpServerPort + ": " + messageConsumerHandler.body());
		});
	}

}
