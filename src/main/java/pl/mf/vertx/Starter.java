package pl.mf.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Starter {

	public static void main(String[] args) {
		startClusteredVerticle();
	}

	private static void startClusteredVerticle() {
		String clusterHost = PropertiesReaderUtil.getProperty("cluster-host");
		Integer clusterPort = Integer.parseInt(PropertiesReaderUtil.getProperty("cluster-port"));
		String clusterPublicHost = PropertiesReaderUtil.getProperty("public-cluster-host");
		Integer clusterPublicPort = Integer.parseInt(PropertiesReaderUtil.getProperty("public-cluster-port"));
		ClusterManager mgr = new HazelcastClusterManager();

		/*
		 * After enabling clustering we need specifying cluster hostname & port - not
		 * necessary only when we run local instances. Default port is randomly picked
		 * by app.
		 * 
		 */
		VertxOptions options = new VertxOptions().setClusterManager(mgr).setClusterHost(clusterHost)
				.setClusterPort(clusterPort).setClusterPublicHost(clusterPublicHost)
				.setClusterPublicPort(clusterPublicPort);

		Vertx.clusteredVertx(options, res -> {
			if (res.succeeded()) {
				Vertx vertx = res.result();
				vertx.deployVerticle(DemoVerticle.class.getName());
			} else {
				System.err.println("FAILED starting cluster");
			}
		});
	}
}
