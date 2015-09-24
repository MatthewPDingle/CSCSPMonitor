package data.downloaders.okcoin.websocket;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;

public class OKCoinWebSocketBase {
	private OKCoinWebSocketService service = null;
	private TimerTask moniter = null;
	private EventLoopGroup group = null;
	private Bootstrap bootstrap = null;
	private Channel channel = null;
	private String url = null;
	private ChannelFuture future = null;
	private int siteFlag = 0;
	private Set<String> subscriptionChannels = new HashSet<String>();

	public OKCoinWebSocketBase(String url, OKCoinWebSocketService serivce) {
		this.url = url;
		this.service = serivce;
	}

	public boolean start() {
		if (url == null) {
			return false;
		}
		if (service == null) {
			return false;
		}
		
		moniter = new TimerTask() {
			@Override
			public void run() {
			}
		};
		return this.connect();
	}

	public void addChannel(String channel) {
		if (channel == null) {
			return;
		}
		String dataMsg = "{'event':'addChannel','channel':'" + channel + "','binary':'true'}";
		this.sendMessage(dataMsg);
		subscriptionChannels.add(channel);
	}

	public void removeChannel(String channel) {
		if (channel == null) {
			return;
		}
		String dataMsg = "{'event':'removeChannel','channel':'" + channel + "'}";
		this.sendMessage(dataMsg);
		subscriptionChannels.remove(channel);
	}

	public void removeAllChannels() {
		for (String channel : subscriptionChannels) {
			String dataMsg = "{'event':'removeChannel','channel':'" + channel + "'}";
			this.sendMessage(dataMsg);
		}
		subscriptionChannels.clear();
	}
	
	public boolean isNettyChannelNull() {
		if (channel == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param apiKey
	 * @param secretKey
	 * @param symbol
	 * @param orderId
	 * @param contractType
	 */
	public void cancleFutureOrder(String apiKey, String secretKey, String symbol, long orderId, String contractType) {
		Map<String, String> preMap = new HashMap<String, String>();
		preMap.put("api_key", apiKey);
		preMap.put("symbol", symbol);
		preMap.put("order_id", String.valueOf(orderId));
		preMap.put("contract_type", contractType);
		String preStr = OKCoinMD5Util.createLinkString(preMap);
		preStr = preStr + "&secret_key=" + secretKey;
		String signStr = OKCoinMD5Util.getMD5String(preStr);
		preMap.put("sign", signStr);
		String params = OKCoinMD5Util.getParams(preMap);
		StringBuilder tradeStr = new StringBuilder(
				"{'event': 'addChannel','channel': 'ok_futuresusd_cancel_order','parameters': ").append(params).append("}");
		this.sendMessage(tradeStr.toString());
	}

	/**
	 * 
	 * @param apiKey
	 * @param secretKey
	 * @param symbol
	 * @param orderId
	 */
	public void cancleOrder(String apiKey, String secretKey, String symbol, Long orderId) {
		Map<String, String> preMap = new HashMap<String, String>();
		preMap.put("api_key", apiKey);
		preMap.put("symbol", symbol);
		preMap.put("order_id", orderId.toString());
		String preStr = OKCoinMD5Util.createLinkString(preMap);
		StringBuilder preBuilder = new StringBuilder(preStr);
		preBuilder.append("&secret_key=").append(secretKey);
		String signStr = OKCoinMD5Util.getMD5String(preBuilder.toString());
		preMap.put("sign", signStr);
		String params = OKCoinMD5Util.getParams(preMap);
		String channel = "ok_spotcny_cancel_order";
		if (siteFlag == 1) {
			channel = "ok_spotusd_cancel_order";
		}
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel', 'channel':'" + channel + "', 'parameters':").append(params).append("}");
		this.sendMessage(tradeStr.toString());
	}

	/**
	 * 
	 * @param apiKey
	 * @param secretKey
	 */
	public void futureRealtrades(String apiKey, String secretKey) {
		StringBuilder preStr = new StringBuilder("api_key=");
		preStr.append(apiKey).append("&secret_key=").append(secretKey);
		String signStr = OKCoinMD5Util.getMD5String(preStr.toString());
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel','channel':'ok_usd_future_realtrades','parameters':{'api_key':'").append(apiKey).append("','sign':'").append(signStr).append("'},'binary':'true'}");
		this.sendMessage(tradeStr.toString());
	}

	/**
	 * 
	 * @param apiKey
	 * @param secretKey
	 * @param symbol
	 * @param contractType
	 * @param price
	 * @param amount
	 * @param type
	 * @param matchPrice
	 * @param leverRate
	 */
	public void futureTrade(String apiKey, String secretKey, String symbol, String contractType, double price,
			int amount, int type, double matchPrice, int leverRate) {
		Map<String, String> preMap = new HashMap<String, String>();

		preMap.put("api_key", apiKey);
		preMap.put("symbol", symbol);
		preMap.put("contract_type", contractType);
		preMap.put("price", String.valueOf(price));
		preMap.put("amount", String.valueOf(amount));
		preMap.put("type", String.valueOf(type));
		preMap.put("match_price", String.valueOf(matchPrice));
		preMap.put("lever_rate", String.valueOf(leverRate));
		String preStr = OKCoinMD5Util.createLinkString(preMap);
		preStr = preStr + "&secret_key=" + secretKey;
		String signStr = OKCoinMD5Util.getMD5String(preStr);

		preMap.put("sign", signStr);
		String params = OKCoinMD5Util.getParams(preMap);

		StringBuilder tradeStr = new StringBuilder("{'event': 'addChannel','channel':'ok_futuresusd_trade','parameters':").append(params).append("}");
		this.sendMessage(tradeStr.toString());

	}

	/**
	 * user_info
	 */
	public void getUserInfo(String apiKey, String secretKey) {
		StringBuilder preStr = new StringBuilder("api_key=");
		preStr.append(apiKey).append("&secret_key=").append(secretKey);
		String signStr = OKCoinMD5Util.getMD5String(preStr.toString());
		String channel = "ok_spotcny_userinfo";
		if (siteFlag == 1) {
			channel = "ok_spotusd_userinfo";
		}
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel','channel':'").append(channel).append("','parameters':{'api_key':'").append(apiKey).append("','sign':'").append(signStr).append("'},'binary':'true'}");
		this.sendMessage(tradeStr.toString());
	}

	/**
	 * 
	 * @param apiKey
	 * @param secretKey
	 */
	public void realTrades(String apiKey, String secretKey) {
		StringBuilder preStr = new StringBuilder("api_key=");
		preStr.append(apiKey).append("&secret_key=").append(secretKey);
		String signStr = OKCoinMD5Util.getMD5String(preStr.toString());
		String channel = "ok_cny_realtrades";
		if (siteFlag == 1) {
			channel = "ok_usd_realtrades";
		}
		StringBuilder tradeStr = new StringBuilder(
				"{'event':'addChannel','channel':'" + channel + "','parameters':{'api_key':'").append(apiKey).append("','sign':'").append(signStr).append("'},'binary':'true'}");
		this.sendMessage(tradeStr.toString());
	}

	/**
	 * 
	 * @param apiKey
	 * @param symbol
	 * @param secretKey
	 * @param price
	 * @param amount
	 * @param type
	 */
	public void spotTrade(String apiKey, String secretKey, String symbol, String price, String amount, String type) {
		Map<String, String> signPreMap = new HashMap<String, String>();
		signPreMap.put("api_key", apiKey);
		signPreMap.put("symbol", symbol);
		if (price != null) {
			signPreMap.put("price", price);
		}
		if (amount != null) {
			signPreMap.put("amount", amount);
		}
		signPreMap.put("type", type);
		String preStr = OKCoinMD5Util.createLinkString(signPreMap);
		StringBuilder preBuilder = new StringBuilder(preStr);
		preBuilder.append("&secret_key=").append(secretKey);
		String signStr = OKCoinMD5Util.getMD5String(preBuilder.toString());
		String channel = "ok_spotcny_trade";
		if (siteFlag == 1) {
			channel = "ok_spotusd_trade";
		}
		StringBuilder tradeStr = new StringBuilder("{'event':'addChannel','channel':'" + channel + "','parameters':");
		signPreMap.put("sign", signStr);
		String params = OKCoinMD5Util.getParams(signPreMap);
		tradeStr.append(params).append("}");
		this.sendMessage(tradeStr.toString());
	}

	public boolean connect() {
		try {
			System.out.println("Connect");
			final URI uri = new URI(url);
			if (uri == null) {
				return false;
			}
			if (uri.getHost().contains("com")) {
				siteFlag = 1;
			}
			group = new NioEventLoopGroup(1);
			bootstrap = new Bootstrap();
			final SslContext sslCtx = SslContext.newClientContext();
			final OKCoinWebSocketClientHandler handler = new OKCoinWebSocketClientHandler(WebSocketClientHandshakerFactory
					.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), Integer.MAX_VALUE), service, moniter);
			bootstrap.group(group).option(ChannelOption.TCP_NODELAY, true).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel ch) {
					ChannelPipeline p = ch.pipeline();
					if (sslCtx != null) {
						p.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
					}
					p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
				}
			});

			future = bootstrap.connect(uri.getHost(), uri.getPort());
			future.addListener(new ChannelFutureListener() {
				public void operationComplete(final ChannelFuture future) throws Exception {
				}
			});
			channel = future.sync().channel();
			handler.handshakeFuture().sync();
		} 
		catch (Exception e) {
			e.printStackTrace();
			group.shutdownGracefully();
			return false;
		}
		return true;
	}

	public void sendMessage(String message) {
		if (channel != null) {
			channel.writeAndFlush(new TextWebSocketFrame(message));
		}
	}

	public void sentPing() {
		String dataMsg = "{'event':'ping'}";
		this.sendMessage(dataMsg);
	}

	public void setUrl(String url) {
		this.url = url;
	}
}