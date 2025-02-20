package com.example.web.rpc;

import com.example.base.entities.wallet.Wallet;
import com.example.base.vo.JsonVo;
import retrofit2.Call;
import retrofit2.http.GET;

public interface WalletRpcService {
    @GET("/wallet/createNewWallet")
    Call<JsonVo<Wallet>> createNewWallet();

    @GET("/wallet/sendCoin")
    Call<JsonVo> sendCoin();

}
