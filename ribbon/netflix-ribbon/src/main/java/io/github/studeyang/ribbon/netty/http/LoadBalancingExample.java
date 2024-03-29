package io.github.studeyang.ribbon.netty.http;

import com.google.common.collect.Lists;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.Server;
import com.netflix.ribbon.transport.netty.RibbonTransport;
import com.netflix.ribbon.transport.netty.http.LoadBalancingHttpClient;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observer;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LoadBalancingExample {

    public static void main(String[] args) throws Exception {
        List<Server> servers = Lists.newArrayList(
                new Server("www.baidu.com", 80),
                new Server("www.163.com", 80),
                new Server("www.qq.com", 80));
        BaseLoadBalancer lb = LoadBalancerBuilder.newBuilder()
                .buildFixedServerListLoadBalancer(servers);

        LoadBalancingHttpClient<ByteBuf, ByteBuf> client = RibbonTransport.newHttpClient(lb);
        final CountDownLatch latch = new CountDownLatch(servers.size());
        Observer<HttpClientResponse<ByteBuf>> observer = new Observer<HttpClientResponse<ByteBuf>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(HttpClientResponse<ByteBuf> args) {
                latch.countDown();
                System.out.println("Got response: " + args.getStatus());
            }
        };
        for (int i = 0; i < servers.size(); i++) {
            HttpClientRequest<ByteBuf> request = HttpClientRequest.createGet("/");
            client.submit(request).subscribe(observer);
        }
        latch.await();
        System.out.println(lb.getLoadBalancerStats());
    }

}
