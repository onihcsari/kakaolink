package com.example.myapplication3;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.share.ShareClient;
import com.kakao.sdk.share.WebSharerClient;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.ItemContent;
import com.kakao.sdk.template.model.ItemInfo;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.Social;
import com.kakao.sdk.template.model.TextTemplate;
import com.kakao.sdk.user.UserApi;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.user.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
//    Link link = new Link("https://qr.kakaopay.com/FWjwn6E1u");
//    TextTemplate feedTemplate = new TextTemplate(
//            "10000원을 보내세요", link);
    FeedTemplate feedTemplate = new FeedTemplate(
            new Content("오늘의 디저트",
                    "http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png",
                    new Link("https://qr.kakaopay.com/FWjwn6E1u",
                            "https://qr.kakaopay.com/FWjwn6E1u"),
                    "#케익 #딸기 #삼평동 #카페 #분위기 #소개팅"
            ),
            new ItemContent("Kakao",
                    "http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png",
                    "Cheese cake",
                    "http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png",
                    "Cake",
                    Arrays.asList(new ItemInfo("cake1", "1000원")),
                    "Total",
                    "15000원"
            ),
            new Social(286, 45, 845),
            Arrays.asList(new com.kakao.sdk.template.model.Button("웹으로 보기", new Link("https://qr.kakaopay.com/FWjwn6E1u", "https://qr.kakaopay.com/FWjwn6E1u"))
    ));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        KakaoSdk.init(this, "3b1ae21cab0f8f7c5b233ce351d102e4");
        UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, (oAuthToken, throwable) -> {
            if (throwable != null){
                Log.e(TAG, "로그인 실패", throwable);
            } else if(oAuthToken != null){
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
            }
            return null;
        });

        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Log.e(TAG, "사용자 정보 요청 실패", meError);
            } else {
                Log.i(TAG, "사용자 정보 요청 성공" +
                        "\n회원번호: "+user.getId() +
                        "\n이메일: "+user.getKakaoAccount().getEmail());
            }
            return null;
        });



        Button kakao_link_msg_button = (Button) findViewById(R.id.kakao_link_msg_button);
        kakao_link_msg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) {
                    kakaoLink();
                } else {
                    webKakaoLink();
                }
            }
        });

    }

    public void kakaoLink() {
        String TAG = "kakaoLink()";
        // 카카오톡으로 카카오링크 공유 가능
        ShareClient.getInstance().shareDefault(MainActivity.this, feedTemplate, null, (linkResult, error) -> {
            if (error != null) {
                Log.e("TAG", "카카오링크 보내기 실패", error);
            } else if (linkResult != null) {
                Log.d(TAG, "카카오링크 보내기 성공 ${linkResult.intent}");
                MainActivity.this.startActivity(linkResult.getIntent());

                // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                Log.w("TAG", "Warning Msg: " + linkResult.getWarningMsg());
                Log.w("TAG", "Argument Msg: " + linkResult.getArgumentMsg());
            }
            return null;
        });
    }

    public void webKakaoLink() {
        String TAG = "webKakaoLink()";

        // 카카오톡 미설치: 웹 공유 사용 권장
        // 웹 공유 예시 코드
        Uri sharerUrl = WebSharerClient.getInstance().makeDefaultUrl(feedTemplate);

        // CustomTabs으로 웹 브라우저 열기
        // 1. CustomTabs으로 Chrome 브라우저 열기
        try {
            KakaoCustomTabsClient.INSTANCE.openWithDefault(MainActivity.this, sharerUrl);
        } catch (UnsupportedOperationException e) {
            // Chrome 브라우저가 없을 때 예외처리
        }

        // 2. CustomTabs으로 디바이스 기본 브라우저 열기
        try {
            KakaoCustomTabsClient.INSTANCE.open(MainActivity.this, sharerUrl);
        } catch (ActivityNotFoundException e) {
            // 인터넷 브라우저가 없을 때 예외처리
        }
    }
}