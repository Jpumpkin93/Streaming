package com.example.ju.streaming.Kakao;

import android.app.Activity;
import android.content.Context;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;

//sdk와 application 연결, kakaoadapter 사용
public class KaKaoSDKAdapter extends KakaoAdapter {

    @Override
    public ISessionConfig getSessionConfig() {
        return new ISessionConfig() {

            //인증받을타입
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[]{
                        AuthType.KAKAO_LOGIN_ALL
                };
            }
            //웹뷰 타이머 설정
            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }
            //token 암호화
            @Override
            public boolean isSecureMode() {
                return false;
            }
            //kakao와 제휴된 앱에서 사용되는 값
            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }
            //웹뷰에서 email 입력폼에 data를 save할지
            @Override
            public boolean isSaveFormData() {
                return false;
            }
        };
    }

    //Application 이 가지고있는 정보를 얻기위한 interface
    @Override
    public IApplicationConfig getApplicationConfig() {
        return new IApplicationConfig() {
            @Override
            public Context getApplicationContext() {
                return GlobalApplication.getGlobalApplicationContext();
            }
        };
    }
}
