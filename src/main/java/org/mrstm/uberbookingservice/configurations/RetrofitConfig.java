package org.mrstm.uberbookingservice.configurations;

import com.netflix.discovery.EurekaClient;
import okhttp3.OkHttpClient;
import org.mrstm.uberbookingservice.apis.LocationServiceApi;
import org.mrstm.uberbookingservice.apis.SocketApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitConfig {
    private EurekaClient eurekaClient;
    public RetrofitConfig(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    private String getServiceUrl(String serviceName) {
        return eurekaClient.getNextServerFromEureka(serviceName, false).getHomePageUrl();
    }

    @Bean
    public LocationServiceApi locationServiceApi() {
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("UBERLOCATIONSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build()
                .create(LocationServiceApi.class);
    }

    @Bean
    public SocketApi socketApi() {
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("UBERCLIENTSOCKETSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build()
                .create(SocketApi.class);
    }
}
