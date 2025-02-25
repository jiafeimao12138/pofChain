package com.example.web.rpc;


import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface BlockRpcService {

    @GET("/common/getChainLatestBlock")
    Call<JsonVo<Block>> getLocalChainLatestBlock();

    @GET("/common/getLocalChainHeight")
    Call<JsonVo<Long>> getLongestChainHeight();

    @GET("/common/getBlocks")
    Call<JsonVo<List<Block>>> getBlocks(@Query("start") long start, @Query("end") long end);

    @GET("/common/getBlockByHash")
    Call<JsonVo<Block>> getBlockByHash(@Query("hash") String hash);

    @GET("/common/getBlockByHeight")
    Call<JsonVo<Block>> getBlockByHeight(@Query("height") long height);


}
