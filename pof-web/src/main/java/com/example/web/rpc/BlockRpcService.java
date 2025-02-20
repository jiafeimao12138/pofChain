package com.example.web.rpc;


import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import retrofit2.Call;
import retrofit2.http.*;

public interface BlockRpcService {

    @GET("/common/getLocalChainLatestBlock")
    Call<JsonVo<Block>> getLocalChainLatestBlock();

    @GET("/common/getLongestChainHeight")
    Call<JsonVo<Long>> getLongestChainHeight();

    @GET("/common/syncBlockChain")
    Call<Void> syncBlockChain();

}
