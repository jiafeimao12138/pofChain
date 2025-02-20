package com.example.web.rpc.Impl;

import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import com.example.web.exception.ApiError;
import com.example.web.exception.ApiException;
import com.example.web.rpc.BlockRpcService;
import com.example.web.rpc.BlockService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;

public class BlockServiceImpl implements BlockService {
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static final Retrofit.Builder builder = new Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create());

    private static Retrofit retrofit;

    private static BlockRpcService rpcService;

    public BlockServiceImpl(String baseUrl, boolean debug)
    {
        // open debug log model
        if (debug) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
        }

        builder.baseUrl(baseUrl);
        builder.client(httpClient.build());
        builder.addConverterFactory(JacksonConverterFactory.create());
        retrofit = builder.build();
        rpcService = retrofit.create(BlockRpcService.class);
    }

    /**
     * Invoke the remote API Synchronously
     */
    public static <T> T executeSync(Call<T> call)
    {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                ApiError apiError = getApiError(response);
                throw new ApiException(apiError);
            }
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    private static ApiError getApiError(Response<?> response) throws IOException, ApiException
    {
        assert response.errorBody() != null;
        return (ApiError) retrofit.responseBodyConverter(ApiError.class, new Annotation[0]).convert(response.errorBody());
    }

    /**
     * 获取本地链最新区块
     * @return
     */
    @Override
    public JsonVo<Block> getLocalChainLatestBlock() {
        return executeSync(rpcService.getLocalChainLatestBlock());
    }

    /**
     * 获取最长链高度
     * @return
     */
    @Override
    public JsonVo<Long> getLongestChainHeight() {
        return executeSync(rpcService.getLongestChainHeight());
    }

    @Override
    public void syncBlockChain() {
        executeSync(rpcService.syncBlockChain());
    }
}
