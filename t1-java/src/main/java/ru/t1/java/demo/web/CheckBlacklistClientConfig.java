package ru.t1.java.demo.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class CheckBlacklistClientConfig {
    @Value("${web.base-url}")
    private String baseURL;

    /*private final ConnectionProvider connProvider = ConnectionProvider
            .builder("webclient-conn-pool")
            .maxConnections(80)
            .maxIdleTime(Duration.ofMillis(10))
            .maxLifeTime(Duration.ofMillis(10000))
            .pendingAcquireMaxCount(10)
            .pendingAcquireTimeout(Duration.ofMillis(40000))
            .build();*/

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(baseURL).build();
    }

   /* @Bean
    public CheckBlacklistWebClient checkWebClient(ClientHttp clientHttp) {
        WebClient.Builder webClient = WebClient.builder();
        webClient
                .baseUrl(url)
                .clientConnector(clientHttp.getClientHttp(CheckBlacklistWebClient.class.getName()));
        return new CheckBlacklistWebClient(webClient.build());
    }


    @Bean
    ClientHttp getClientHttp() {
        return new ClientHttp();
    }

    public class ClientHttp {
        @SneakyThrows
        public ClientHttpConnector getClientHttp(String nameLogClass) {
            SslContext sslContext = SslContextBuilder
                    .forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            return new ReactorClientHttpConnector(HttpClient
                    .create()
                    .create(connProvider)
                    .secure(t -> t.sslContext(sslContext))
                    .resolver(DefaultAddressResolverGroup.INSTANCE));
        }
    }*/
}
