package pl.mf.vertx;

import java.util.Date;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.AsyncMap;

public class DemoVerticle extends AbstractVerticle {
	private static String ASYNCMAP_ADDRESS = "VerticlesSharedMap";
	private static String HTTP_SERVER_ADD_TO_MAP = "/add";
	private static String HTTP_SERVER_MAP_ENTRIES = "/entries";
	private AsyncMap<Integer, String> verticleData;
	private int httpServerPort;
	private String httpServerHost;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		initializeVerticleFields();

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
			} else {
				handler.response().end();
			}
		}).listen(httpServerPort, httpServerHost);
	}

	private void initializeVerticleFields() {
		httpServerPort = Integer.parseInt(PropertiesReaderUtil.getProperty("http-server-port"));
		httpServerHost = PropertiesReaderUtil.getProperty("http-server-host");
		vertx.sharedData().<Integer, String>getAsyncMap(ASYNCMAP_ADDRESS, resultHandler -> {
			if (resultHandler.succeeded()) {
				verticleData = resultHandler.result();
				LogUtil.printMessageWithDate("Access to map: SUCCESS");
			} else if (resultHandler.failed())
				LogUtil.printMessageWithDate("Access to map: FAILED");
		});
	}

}
