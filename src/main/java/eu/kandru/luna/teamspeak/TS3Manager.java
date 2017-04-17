package eu.kandru.luna.teamspeak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TS3Manager {
	private TS3Config ts3config;
	private TS3Query ts3query;
	@Getter
	private TS3ApiAsync ts3api;
	private final int RECONNECTION_TIMEOUT = 10000;

	@Autowired
	public TS3Manager(TS3Properties ts3props) {
		ts3config = new TS3Config();
		ts3config.setHost(ts3props.getIp()).setQueryPort(ts3props.getPort())
				.setReconnectStrategy(ReconnectStrategy.constantBackoff(RECONNECTION_TIMEOUT));
		ts3query = new TS3Query(ts3config);
		try {
			ts3query.connect();

			ts3api = ts3query.getAsyncApi();
			ts3api.selectVirtualServerById(ts3props.getServerId());

			CommandFuture<Boolean> loginCommand = ts3api.login(ts3props.getLogin(), ts3props.getPassword());
			loginCommand.onSuccess(result -> {
				log.debug("Connected to ts3 server.");
			});
			loginCommand.onFailure(result -> {
				log.debug("Failed to connect to ts3 server.");
			});

			CommandFuture<Boolean> registerCommand = ts3api.registerAllEvents();
			registerCommand.onFailure(result -> {
				log.error("Couldnt register all ts3 events.");
			});
		} catch (TS3ConnectionFailedException e) {
			log.error("Couldn't connect to ts3 server.", e);
			// TODO: reconnect !!!! FATAL
		}
	}

}
