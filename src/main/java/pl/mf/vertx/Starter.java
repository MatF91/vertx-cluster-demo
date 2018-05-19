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

		ClusterManager mgr = new HazelcastClusterManager();
		VertxOptions options = new VertxOptions().setClusterManager(mgr);

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
